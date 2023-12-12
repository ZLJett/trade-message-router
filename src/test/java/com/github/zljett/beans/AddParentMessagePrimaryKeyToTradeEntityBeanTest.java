package com.github.zljett.beans;

import com.github.zljett.entitiesandrepositories.MessageEntity;
import com.github.zljett.entitiesandrepositories.TradeEntity;
import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("file:src/test/resources/test.properties")
@SpringBootTest(properties = {"spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"})
@CamelSpringBootTest
@UseAdviceWith
class AddParentMessagePrimaryKeyToTradeEntityBeanTest {
  
  @Autowired
  private CamelContext camelContext;

  @Autowired
  private MessageDataPersistenceBean messageDataPersistenceBean;

  @Autowired
  private AddParentMessagePrimaryKeyToTradeEntityBean addParentMessagePrimaryKeyToTradeEntityBean;

  @Test
  @DisplayName("Should add the Parent Message's Primary Key to the Trade Entity")
  public void shouldAddForeignKeyToTradeEntity() throws Exception {
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // Persist this Trade Entity's 'parent message' so the bean has a database entry from which to pull the parent
    // message's primary key
    String testMessageName = "ZSE_TRD_MSG_BOC_987654321.xml";
    // Date/time is variable in production so is set to a fixed value for the purposes of this test
    String testFormattedDate = "2023-10-10 09:37:52";
    // This is the size in bytes of TestMessageInInternalXmlFormat_WithSingleTrade.xml
    long testFileLength = 907L;
    // This test message has the single trade with the data that the below test and expected Trade Entities are based on
    final Path testMessageBodyFilepath = Paths.get("src/test/resources/TestInternalXmlFormatMessages/TestMessageInInternalXmlFormat_WithSingleTrade.xml");
    String persistedMessagePrimaryKey = persistTestMessageEntity(testMessageBodyFilepath, testMessageName, testFormattedDate, testFileLength);
    // Create a Trade Entity with a null messageId field to be passed into bean
    TradeEntity testTradeEntity = createTestTradeEntity();
    // Setup Headers map to be passed into bean
    Map<String, String> inputMessageHeaders = new HashMap<>();
    inputMessageHeaders.put("MessagePrimaryKey", persistedMessagePrimaryKey);
    // Create the expected Trade Entity to compare to the Trade Entity returned by the bean
    TradeEntity expectedTradeEntity = createExpectedTradeEntity(testMessageName, testFormattedDate,testFileLength);
    // Give bean minimum required information to test its core function
    addParentMessagePrimaryKeyToTradeEntityBean.setTradeForeignKeyToParentMessagePrimaryKey(testTradeEntity, inputMessageHeaders);
    // Check if Trade Entity produced by bean matches the correct Trade Entity
    assertTrue(testTradeEntity.equals(expectedTradeEntity));
  }

  /**
   * This method uses the messageDataPersistenceBean class as an expedient way to persist a 'parent message'
   */
  private String persistTestMessageEntity(Path testMessageBodyFilepath, String testMessageName, String testFormattedDate, Long testFileLength) throws IOException, XMLStreamException {
    String testMessageBody = readString(testMessageBodyFilepath);
    Map<String, String> testMessageHeaders = new HashMap<>();
    testMessageHeaders.put("CamelFileName", testMessageName);
    testMessageHeaders.put("DateReceived", testFormattedDate);
    messageDataPersistenceBean.persistMessageData(testMessageBody, testMessageHeaders, testFileLength);
    String persistedMessagePrimaryKey = testMessageHeaders.get("MessagePrimaryKey");
    return persistedMessagePrimaryKey;
  }

  private static TradeEntity createTestTradeEntity() {
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
    testTradeEntity.setMessageId(null);
    return testTradeEntity;
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
    // The test message used as the 'parent message' for this test has 1 trade
    expectedMessageEntity.setNumberOfTrades(1);
    expectedMessageEntity.setFileSizeInBytes(testFileLength);
    return expectedMessageEntity;
  }
}