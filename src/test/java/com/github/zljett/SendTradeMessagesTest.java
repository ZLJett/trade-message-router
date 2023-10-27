package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RoutingSlipDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.nio.file.Files.readString;
import static java.util.Map.entry;
import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Test for the complete message processing system and its associated message data persistence SEDA route.
 * This tests the full system from sender message pickup to final delivery, including message and trade data persistence.
 */
@SpringBootTest(properties =
    {"fullMessagePersistenceFolderFilePath=file:src/test/resources/fullMessagePersistenceFolder",
     "spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"})
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SendTradeMessagesTest {

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TradeRepository tradeRepository;

  private final Map<String, String> expectedMessageNames = Map.ofEntries(
      // Name of each sent test message and the corresponding name of its resulting expected message
      entry("ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_BOC_987654321.xml"),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", "ZSE_TRD_MSG_ZSE_0123456789.xml")
  );

  private final Map<String, Long> expectedMessagesFileLengths  = Map.ofEntries(
      // Name of each sent test message and the corresponding size of the test file in bytes
      entry("ZSE_TRD_MSG_BOC_987654321.xml", 3281L),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", 2184L)
  );

  private final Map<String, Integer> expectedMessagesTradeCount  = Map.ofEntries(
      // Name of each sent test message and the corresponding number of trades in the message
      entry("ZSE_TRD_MSG_BOC_987654321.xml", 6),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", 6)
  );

  @ParameterizedTest(name = "Test {index}: Should Produce Correct Output and Persist Correct Data For Sent Message: {0}")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldProduceCorrectOutputAndPersistCorrectDataForEachSentMessage(String testMessageName) throws Exception {
    String expectedMessageName = expectedMessageNames.get(testMessageName);
    // Date/time is variable in production so is set to a fixed value for the purposes of this test
    String expectedMessageFormattedDate = "2023-10-10 09:37:52";
    long expectedMessageFileLength = expectedMessagesFileLengths.get(testMessageName);
    int expectedMessageNumTrades = expectedMessagesTradeCount.get(testMessageName);
    String recipientAddress = "file:src/test/resources/testToFolder";
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=" + testMessageName + "&noop=true");
          // Change destination address header to point to test recipient directory
          r.weaveByType(RoutingSlipDefinition.class).before().setHeader("RecipientAddress", constant(recipientAddress));
          r.weaveByType(RoutingSlipDefinition.class).before().setHeader("DateReceived", constant(expectedMessageFormattedDate));
        }
    );
    AdviceWith.adviceWith(camelContext, "persist-trade-data-route", r -> {
          r.weaveAddFirst().to("mock:tradesBeforeDataPersistence");
          r.weaveAddLast().to("mock:tradesAfterDataPersistence");
        }
    );
    AdviceWith.adviceWith(camelContext, "exit-route", r -> {
          r.weaveAddLast().to("mock:completeRouteResult");
        }
    );
    camelContext.start();
    MockEndpoint tradesBeforeDataPersistenceMock = camelContext.getEndpoint("mock:tradesBeforeDataPersistence", MockEndpoint.class);
    MockEndpoint tradesAfterDataPersistenceMock = camelContext.getEndpoint("mock:tradesAfterDataPersistence", MockEndpoint.class);
    MockEndpoint completeRouteResultMock = camelContext.getEndpoint("mock:completeRouteResult", MockEndpoint.class);
    completeRouteResultMock.expectedMessageCount(1);
    completeRouteResultMock.assertIsSatisfied();
    tradesAfterDataPersistenceMock.expectedMessageCount(expectedMessageNumTrades);
    tradesAfterDataPersistenceMock.assertIsSatisfied();

    // Check if correct test message is in test full message persistence directory
    final File persistedTestMessage = new File("src/test/resources/fullMessagePersistenceFolder/" + testMessageName);
    assertTrue(persistedTestMessage.exists());

    // Check if persisted message metadata matches the correct metadata for the test message
    MessageEntity correctExpectedMessageEntity = createExpectedMessageEntity(testMessageName, expectedMessageFormattedDate,expectedMessageFileLength, expectedMessageNumTrades);
    // As the Message Entity is always the first item in the test database its primary key is always 1
    Optional<MessageEntity> persistedMessage = messageRepository.findById(1L);
    MessageEntity persistedMessageEntity = persistedMessage.orElse(new MessageEntity());
    assertTrue(persistedMessageEntity.equals(correctExpectedMessageEntity));

    // Check if persisted data of each trade in the message matches the correct trade data for the test message
    ArrayList<TradeEntity> correctPersistedTradeEntities = createExpectedTradeEntitiesList(tradesBeforeDataPersistenceMock);
    // The messageId field of the expected Trade Entities is set to a Message Entity representing the parent message of the
    // persisted trades. The actual value contained in the messageId field is normally set by the .getReferenceByID method
    // in the AddForeignKeyToTradeEntityBean which returns an entity proxy and not the message entity itself. But to test that
    // a value is added to the field in the persisted entity, the property 'hibernate.enable_lazy_load_no_trans' is set to
    // 'true', which allows a copy of the entity refenced in the proxy to be pulled and placed into the persisted entity
    // Note: this 'field' of the expected Trade Entities is kept separate from the actual Trade Entity objects as the messageId
    // field in both persisted and expected should be the same for all trades
    MessageEntity expectedMessageIdField = correctExpectedMessageEntity;
    // Iterate through each persisted Trade. As the message metadata was persisted first, the first primary key for the trades is 2
    for (long primaryKey = 2; primaryKey < correctPersistedTradeEntities.size() + 2 ; primaryKey++) {
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
      assertTrue(compareTrades(persistedTradeEntity, correctPersistedTradeEntities));
    }

    // Check if correct test message is in test recipient directory
    final File receivedTestMessage = new File("src/test/resources/testToFolder/" + expectedMessageName);
    assertTrue(receivedTestMessage.exists());

    // Check if the XML string in the message's body matches the correct XML for test message
    Path receivedMessageBodyFilepath = Paths.get(receivedTestMessage.toString());
    String receivedMessageBody = readString(receivedMessageBodyFilepath);
    Path correctExpectedMessageBodyFilepath = Paths.get("src/test/resources/testCorrectExpectedMessages/" + expectedMessageName);
    String correctExpectedMessageBody = readString(correctExpectedMessageBodyFilepath);
    assertTrue(receivedMessageBody.equals(correctExpectedMessageBody));

    // Clear out test directories
    removeTestMessagesFromTestDirectories(persistedTestMessage, testMessageName, receivedTestMessage, expectedMessageName);
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

  public void removeTestMessagesFromTestDirectories(File persistedTestMessage, String testMessageName, File receivedTestMessage, String expectedMessageName) {
    Logger logger = Logger.getLogger((SendTradeMessagesTest.class.getName()));
    // Delete test message from full message persistence directory
    boolean persistedTestMessageDeleted = persistedTestMessage.delete();
    if (persistedTestMessageDeleted) {
      logger.info("Test message: " + testMessageName + " has been deleted from test full message persistence directory");
    } else {
      logger.info("Failed to delete test message: " + testMessageName + " from test full message persistence directory");
    }
    // Delete test message from recipient directory
    boolean receivedTestMessageDeleted = receivedTestMessage.delete();
    if (receivedTestMessageDeleted) {
      logger.info("Test message: " + expectedMessageName + " has been deleted from test recipient directory");
    } else {
      logger.info("Failed to delete test message: " + expectedMessageName + " from test recipient directory");
    }
  }
}