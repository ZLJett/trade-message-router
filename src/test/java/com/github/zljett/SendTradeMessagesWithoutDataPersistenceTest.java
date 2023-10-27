package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
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
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.Files.readString;
import static java.util.Map.entry;
import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Test for whole message processing route excluding the message data persistence SEDA route. This tests
 * the full message processing route from sender message pickup to final delivery.
 */
@SpringBootTest(properties = {"fullMessagePersistenceFolderFilePath=file:src/test/resources/fullMessagePersistenceFolder"})
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SendTradeMessagesWithoutDataPersistenceTest {

  @Autowired
  private CamelContext camelContext;

  private final Map<String, String> expectedMessageNames = Map.ofEntries(
      // Name of each sent test message and the corresponding name of its resulting expected message
      entry("ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_BOC_987654321.xml"),
      entry("BOC_STD_MSG_ZSE_0123456789.xml", "ZSE_TRD_MSG_ZSE_0123456789.xml")
  );

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

  @ParameterizedTest(name = "Test {index}: Should Produce Correct Output For Sent Message: {0}")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldProduceCorrectOutputForEachSentMessage(String testMessageName) throws Exception {
    String expectedMessageName = expectedMessageNames.get(testMessageName);
    String recipientAddress = "file:src/test/resources/testToFolder";
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=" + testMessageName + "&noop=true");
          // Change destination address header to point to test recipient directory
          r.weaveByType(RoutingSlipDefinition.class).before().setHeader("RecipientAddress", constant(recipientAddress));
        }
    );
    // This stops message data persistence route(s) from running
    AdviceWith.adviceWith(camelContext, "message-data-persistence-asynchronous-route", r -> {
          r.weaveByToUri("seda:PersistMessageDataRoute?concurrentConsumers=1").replace().log(LoggingLevel.INFO, "Skipped message data persistence route(s)");
        }
    );
    AdviceWith.adviceWith(camelContext, "exit-route", r -> {
          r.weaveAddLast().to("mock:routeResult");
        }
    );
    camelContext.start();
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.assertIsSatisfied();
    // Check if correct test message is in test full message persistence directory
    final File persistedTestMessage = new File("src/test/resources/fullMessagePersistenceFolder/" + testMessageName);
    assertTrue(persistedTestMessage.exists());
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
}