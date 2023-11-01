package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistMessageDataNodeTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private CamelContext camelContext;

  @ParameterizedTest(name = "Test {index}: Should Have Persisted Message Metadata: {0}, into Database")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldHavePersistedMessageData(String testMessageName) throws Exception {
    String testFormattedDate = "2023-10-10 09:37:52";
    AdviceWith.adviceWith(camelContext, "persist-message-data-route", r -> {
          // Pulls a message in Internal XML format
          r.replaceFromWith("file:src/test/resources/testInternalXmlFormatMessages?fileName=TestMessageInInternalXmlFormat.xml&noop=true");
          // Add headers needed for MessageDataPersistenceBean
          r.weaveAddFirst().setHeader("CamelFileName", constant(testMessageName));
          r.weaveAddFirst().setHeader("DateReceived", constant(testFormattedDate));
          r.weaveByToUri("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1").replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.assertIsSatisfied();
    // Create expected Message Entity to compare to entity pulled from database
    Long testFileLength = (Long) mock.getExchanges().get(0).getIn().getHeader("CamelFileLength");
    MessageEntity expectedMessageEntity = createExpectedMessageEntity(testMessageName, testFormattedDate, testFileLength);
    Optional<MessageEntity> persistedMessageEntity = messageRepository.findById(1L);
    // The orElse method pulls the Message Entity out of its Optional object wrapper, or if null a blank Message Entity
    assertTrue((persistedMessageEntity.orElse(new MessageEntity())).equals(expectedMessageEntity));
  }

  @Test
  @DisplayName("Should Have Attached Primary Key Header to Message")
  public void shouldHaveAttachedPrimaryKeyHeader() throws Exception {
    // Both the filename and date/time are variable in production so are set to these fixed values for the purposes of this test
    String testMessageName = "ZSE_TRD_MSG_BOC_987654321.xml";
    String testFormattedDate = "2023-10-10 09:37:52";
    AdviceWith.adviceWith(camelContext, "persist-message-data-route", r -> {
          // Pulls a message in Internal XML format
          r.replaceFromWith("file:src/test/resources/testInternalXmlFormatMessages?fileName=TestMessageInInternalXmlFormat.xml&noop=true");
          // Add headers needed for MessageDataPersistenceBean
          r.weaveAddFirst().setHeader("CamelFileName", constant(testMessageName));
          r.weaveAddFirst().setHeader("DateReceived", constant(testFormattedDate));
          r.weaveByToUri("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1").replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.assertIsSatisfied();
    // The primary key's actual value is generated automatically when the bean persists the message entity,
    // the value below is what it should be given that this entity is the only item in the test database and
    // is used to make an accurate comparison with the MessagePrimaryKey Header pulled out of the database
    String expectedMessagePrimaryKeyHeader = "1";
    String testMessagePrimaryKeyHeader = (String) mock.getExchanges().get(0).getIn().getHeader("MessagePrimaryKey");
    assertTrue(testMessagePrimaryKeyHeader.equals(expectedMessagePrimaryKeyHeader));
  }

  private static MessageEntity createExpectedMessageEntity(String testMessageName, String testFormattedDate, long testFileLength) {
    MessageEntity expectedMessageEntity = new MessageEntity();
    // The MessageId field's actual value is generated automatically when the entity is persisted, the value below is
    // what it should be given that this entity is the only item in the test database and is used to make an accurate
    // comparison with the Message Entity pulled out of the database later.
    expectedMessageEntity.setMessageId(1L);
    String[] splitMessageName = testMessageName.split("\\.");
    String expectedMessageName = splitMessageName[0];
    expectedMessageEntity.setMessageName(expectedMessageName);
    expectedMessageEntity.setDateReceived(testFormattedDate);
    // The input test message has 6 trades
    expectedMessageEntity.setNumberOfTrades(6);
    expectedMessageEntity.setFileSizeInBytes(testFileLength);
    return expectedMessageEntity;
  }
}