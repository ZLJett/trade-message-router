package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MessageDataPersistenceBeanTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private MessageDataPersistenceBean messageDataPersistenceBean;

  final Path testMessageBodyFilepath = Paths.get("src/test/resources/TestInternalXmlFormatMessages/TestMessageInInternalXmlFormat.xml");

  @ParameterizedTest(name = "Test {index}: Should Persist Message Metadata: {0}, into Database")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldPersistMessageData(String testMessageName) throws Exception {
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // Setup test input for bean
    String testMessageBody = readString(testMessageBodyFilepath);
    Map<String, String> testMessageHeaders = new HashMap<>();
    testMessageHeaders.put("CamelFileName", testMessageName);
    // Both date/time and file length are variable in production so are set to these fixed values for the purposes of this test
    String testFormattedDate = "2023-10-10 09:37:52";
    testMessageHeaders.put("DateReceived", testFormattedDate);
    long testFileLength = 3281L;
    // Create expected Message Entity to compare to entity pulled from database
    MessageEntity expectedMessageEntity = createExpectedMessageEntity(testMessageName, testFormattedDate, testFileLength);
    // Give bean minimum required information to test its core function of persisting message metadata
    messageDataPersistenceBean.persistMessageData(testMessageBody, testMessageHeaders, testFileLength);
    Optional<MessageEntity> persistedMessageEntity = messageRepository.findById(1L);
    // The orElse method pulls the Message Entity out of its Optional object wrapper, or if null a blank Message Entity
    assertTrue((persistedMessageEntity.orElse(new MessageEntity())).equals(expectedMessageEntity));
    // The value the header is compared to is the value that should be generated for the MessageId field, see the above comment
    assertTrue((testMessageHeaders.get("MessagePrimaryKey")).equals("1"));
  }

  @Test
  @DisplayName("Should Count Correct Number of Trades in a Message")
  public void shouldCountCorrectNumberOfTradesInMessage() throws Exception {
    String testMessageBody = readString(testMessageBodyFilepath);
    int tradesInTestMessage = messageDataPersistenceBean.countTradesInMessage(testMessageBody);
    // The input test message has 6 trades
    assertEquals(tradesInTestMessage,6);
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