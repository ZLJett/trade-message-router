package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"})
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TradesToPersistenceEntitiesNodeTest {

  @Autowired
  private TradeRepository tradeRepository;

  @Autowired
  private CamelContext camelContext;

  @Test
  @DisplayName("Should Create Trade Entity for each Trade in an Internal Format Message")
  public void shouldCreateTradeEntityFor_EachTradeInInternalFormatXMLMessage() throws Exception {
    final String testMessageName = "ZSE_TRD_MSG_BOC_987654321.xml";
    final String testFormattedDate = "2023-10-10 09:37:52";
    // This is the size in bytes of TestMessageInInternalXmlFormat_WithSingleTrade.xml
    final long testFileLength = 907L;
    // To fully test the TradesToPersistenceEntitiesNode,specifically the AddForeignKeyToTradeEntityBean, there must be
    // an existing entry in the database for the trade's parent message for the bean to use to set the messageId field
    // of the Trade Entity with the foreign key tying the trade to its parent message.
    AdviceWith.adviceWith(camelContext, "persist-message-data-route", r -> {
          // Pulls a message in Internal XML format that only has a single trade
          r.replaceFromWith("file:src/test/resources/TestInternalXmlFormatMessages?fileName=TestMessageInInternalXmlFormat_WithSingleTrade.xml&noop=true");
          // Add headers needed for MessageDataPersistenceBean
          r.weaveAddFirst().setHeader("CamelFileName", constant(testMessageName));
          r.weaveAddFirst().setHeader("DateReceived", constant(testFormattedDate));
          // Remove SEDA endpoint and send directly to TradesToPersistenceEntitiesNode
          r.weaveByToUri("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1").replace().to("direct:TradesToPersistenceEntitiesRoute");
        }
    );
    AdviceWith.adviceWith(camelContext, "trades-to-persistence-entities-route", r -> {
          r.replaceFromWith("direct:TradesToPersistenceEntitiesRoute");
          r.weaveByToUri("seda:PersistTradeDataRoute?concurrentConsumers=10").replace().to("mock:routeResult");
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
    // Create expected Trade Entity to compare to entity produced by route
    TradeEntity expectedTradeEntity = createExpectedTradeEntity(testMessageName, testFormattedDate,testFileLength);
    // Check if Trade Entity produced by Route matches the correct Trade Entity for test message
    TradeEntity receivedTradeEntity = (TradeEntity) mock.getExchanges().get(0).getIn().getBody();
    assertTrue(receivedTradeEntity.equals(expectedTradeEntity));
  }

  private static TradeEntity createExpectedTradeEntity(String testMessageName, String testFormattedDate, long testFileLength) {
    TradeEntity testTradeEntity = new TradeEntity();
    // The field TradeId is set to null as would not be filled in until entity is persisted
    testTradeEntity.setTradeId(null);
    testTradeEntity.setSenderId("BCD");
    testTradeEntity.setRecipientId("JAX");
    testTradeEntity.setFdicId("TR02409438284");
    testTradeEntity.setAssetId("GME");
    testTradeEntity.setCurrency("CAD");
    testTradeEntity.setTradeValue("45678.912");
    testTradeEntity.setTradeType("Buy");
    testTradeEntity.setAssetType("Stock");
    //  The field setMessageId is set to contain the expected MessageEntity. Normally the .getReferenceByID method
    //  used to fetch this value in the AddForeignKeyToTradeEntityBean returns an entity proxy and not the entity itself.
    //  But to test that a value is added, the property 'hibernate.enable_lazy_load_no_trans' is set to 'true', which
    //  allows a copy of the entity refenced in the proxy to be pulled and placed into this field.
    MessageEntity expectedMessageEntity = createExpectedMessageEntity(testMessageName, testFormattedDate,testFileLength);
    testTradeEntity.setMessageId(expectedMessageEntity);
    return testTradeEntity;
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
    // The input test message has 1 trade
    expectedMessageEntity.setNumberOfTrades(1);
    expectedMessageEntity.setFileSizeInBytes(testFileLength);
    return expectedMessageEntity;
  }
}