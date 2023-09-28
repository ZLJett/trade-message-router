package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.logging.Logger;

import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
class exitNodeTest {

  @Autowired
  private CamelContext camelContext;

  public void removeTestFilesFromTestDirectory(File persistedTestMessage, String testMessageName) {
    boolean testFileDeleted = persistedTestMessage.delete();
    Logger logger = Logger.getLogger((PersistFullMessageNodeTest.class.getName()));
    if (testFileDeleted) {
      logger.info("Test message: " + testMessageName + " has been deleted from test directory");
    } else {
      logger.info("Failed to delete test message: " + testMessageName + " from test directory");
    }
  }

  @ParameterizedTest(name = "Test {index}: Should put Message: {0}, into Recipient Directory")
  @ValueSource(strings = {"ZSE_TRD_MSG_BOC_987654321.xml", "BOC_STD_MSG_ZSE_0123456789.xml"})
  public void shouldPutMessageIntoRecipientDirectory(String testMessageName) throws Exception {
    AdviceWith.adviceWith(camelContext, "exit-route", r -> {
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=" + testMessageName + "&noop=true");
          // Add header needed to tell the .toD endpoint where to 'send' the message, i.e. the recipient's "address"
          r.weaveAddFirst().setHeader("RecipientAddress", constant("file:src/test/resources/testToFolder"));
          r.weaveAddLast().to("mock:routeResult");
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
    // Check if correct test message is in test recipient directory
    final File persistedTestMessage = new File("src/test/resources/testToFolder/" + testMessageName);
    assertTrue(persistedTestMessage.exists());
    removeTestFilesFromTestDirectory(persistedTestMessage, testMessageName);
  }
}