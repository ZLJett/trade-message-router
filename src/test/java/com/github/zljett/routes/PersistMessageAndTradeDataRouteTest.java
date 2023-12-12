package com.github.zljett.routes;

import com.github.zljett.entitiesandrepositories.MessageEntity;
import com.github.zljett.entitiesandrepositories.MessageRepository;
import com.github.zljett.entitiesandrepositories.TradeEntity;
import com.github.zljett.entitiesandrepositories.TradeRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for whole message data persistence asynchronous system. This tests both routes for persisting
 * a message's metadata and trades in the database.
 */
@TestPropertySource("file:src/test/resources/test.properties")
@SpringBootTest(properties = {"spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"})
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistMessageAndTradeDataRouteTest {

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TradeRepository tradeRepository;

  private final Map<String, String> testMessagesFilenames = Map.ofEntries(
      // Name of each sent test message and the corresponding file to use for the test
      entry("ZSE_TRD_MSG_BOC_987654321.xml", "TestMessageInInternalXmlFormat_fromZSE.xml"),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", "TestMessageInInternalXmlFormat_fromBoc.xml")
  );

  private final Map<String, Long> testMessagesFileLengths  = Map.ofEntries(
      // Name of each sent test message and the corresponding size of the test file in bytes
      entry("ZSE_TRD_MSG_BOC_987654321.xml", 1886L),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", 1865L)
  );

  private final Map<String, Integer> testMessagesTradeCount  = Map.ofEntries(
      // Name of each sent test message and the corresponding number of trades in the message
      entry("ZSE_TRD_MSG_BOC_987654321.xml", 6),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", 6)
  );

  @ParameterizedTest(name = "Test {index}: Should Persist Correct Data For Sent Message: {0}")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldPersistCorrectDataForEachSentMessage(String testMessageName) throws Exception {
    String testMessageFilename = testMessagesFilenames.get(testMessageName);
    // Date/time is variable in production so is set to a fixed value for the purposes of this test
    String testFormattedDate = "2023-10-10 09:37:52";
    long testMessageFileLength = testMessagesFileLengths.get(testMessageName);
    int numTradesInTestMessage = testMessagesTradeCount.get(testMessageName);
    AdviceWith.adviceWith(camelContext, "persist-message-and-trade-data-route", r -> {
          r.replaceFromWith("file:src/test/resources/TestInternalXmlFormatMessages?fileName=" + testMessageFilename + "&noop=true");
          // Add headers needed for message data persistence
          r.weaveAddFirst().setHeader("CamelFileName", constant(testMessageName));
          r.weaveAddFirst().setHeader("DateReceived", constant(testFormattedDate));
        }
    );
    AdviceWith.adviceWith(camelContext, "asynchronous-persist-message-and-trade-data-route", r -> {
          r.weaveByType(ToDefinition.class).before().to("mock:TradesBeforePersistence");
          r.weaveAddLast().to("mock:RouteResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertions are run
    MockEndpoint tradesBeforePersistenceMock = camelContext.getEndpoint("mock:TradesBeforePersistence", MockEndpoint.class);
    MockEndpoint tradesAfterPersistenceMock = camelContext.getEndpoint("mock:RouteResult", MockEndpoint.class);
    tradesAfterPersistenceMock.expectedMessageCount(1);
    tradesAfterPersistenceMock.assertIsSatisfied();
    // Check if persisted message metadata matches the correct data in the expected Message Entity
    MessageEntity expectedMessageEntity = createExpectedMessageEntity(testMessageName, testFormattedDate,testMessageFileLength, numTradesInTestMessage);
    // As the Message Entity is always the first item in the test database its primary key is always 1
    Optional<MessageEntity> persistedMessage = messageRepository.findById(1L);
    MessageEntity persistedMessageEntity = persistedMessage.orElse(new MessageEntity());
    assertTrue(persistedMessageEntity.equals(expectedMessageEntity));
    /* The messageId field of the expected Trade Entities is set to a Message Entity representing the parent message of the
       persisted trades. The actual value contained in the messageId field is normally set by the .getReferenceByID method
       in the AddForeignKeyToTradeEntityBean which returns an entity proxy and not the message entity itself. But to test that
       a value is added to the field in the persisted entity, the property 'hibernate.enable_lazy_load_no_trans' is set to
       'true', which allows a copy of the entity refenced in the proxy to be pulled and placed into the persisted entity
       Note: this 'field' of the expected Trade Entities is kept separate from the actual Trade Entity objects as the messageId
       field in both persisted and expected should be the same for all trades. */
    MessageEntity expectedMessageIdField = expectedMessageEntity;
    ArrayList<TradeEntity> expectedTradeEntities = createExpectedTradeEntitiesList(tradesBeforePersistenceMock);
    // Iterate through each persisted Trade. As the message metadata was persisted first, the first primary key for the trades is 2
    for (long primaryKey = 2; primaryKey < expectedTradeEntities.size() + 2 ; primaryKey++) {
      Optional<TradeEntity> persistedTrade = tradeRepository.findById(primaryKey);
      TradeEntity persistedTradeEntity = persistedTrade.orElse(new TradeEntity());
      // Check if persisted trade has the correct parent Message Entity referenced in it messageId field
      MessageEntity persistedTradeMessageIdField = persistedTradeEntity.getMessageId();
      assertTrue(persistedTradeMessageIdField.equals(expectedMessageIdField));
      persistedTradeEntity.setMessageId(null);
      // Primary key of persisted trade is irrelevant to testing the trade's data
      persistedTradeEntity.setTradeId(null);
      // Check if each trade's data (i.e. not the primary or foreign keys) of the persisted Trade Entities matches the
      // correct data in the expected Trade Entities
      assertTrue(compareTrades(persistedTradeEntity, expectedTradeEntities));
    }
  }

  private Boolean compareTrades(TradeEntity currentPersistedTradeEntity, ArrayList<TradeEntity> expectedTradeEntities) {
    for (TradeEntity expectedTradeEntity: expectedTradeEntities) {
      if (expectedTradeEntity.equals(currentPersistedTradeEntity)) {
        return true;
      }
    }
    return false;
  }

  private ArrayList<TradeEntity> createExpectedTradeEntitiesList(MockEndpoint tradesBeforePersistenceMock) {
    ArrayList<TradeEntity> expectedTradeEntities = new ArrayList<>();
    for (Exchange tradeExchange : tradesBeforePersistenceMock.getExchanges()) {
      TradeEntity expectedTradeEntity = (TradeEntity) tradeExchange.getIn().getBody();
      // MessageId field is set to null as the persisted Trade Entity's messageId field is tested separately
      expectedTradeEntity.setMessageId(null);
      expectedTradeEntities.add(expectedTradeEntity);
    }
    return expectedTradeEntities;
  }

  private static MessageEntity createExpectedMessageEntity(String testMessageName, String testFormattedDate, long testFileLength, int numTradesInTestMessage) {
    MessageEntity expectedMessageEntity = new MessageEntity();
    // The MessageId field's actual value is generated automatically when the entity is persisted, the value below is
    // what it should be given that this entity is the first item in the test database and is used to make an accurate
    // comparison with the Message Entity pulled out of the persisted Trade Entity later.
    expectedMessageEntity.setMessageId(1L);
    String[] splitMessageName = testMessageName.split("\\.");
    String expectedMessageName = splitMessageName[0];
    expectedMessageEntity.setMessageName(expectedMessageName);
    expectedMessageEntity.setDateReceived(testFormattedDate);
    expectedMessageEntity.setNumberOfTrades(numTradesInTestMessage);
    expectedMessageEntity.setFileSizeInBytes(testFileLength);
    return expectedMessageEntity;
  }
}