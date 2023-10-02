package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"fullMessagePersistenceFolderFilePath=file:src/test/resources/fullMessagePersistenceFolder"})
@CamelSpringBootTest
@UseAdviceWith
class PersistFullMessageNodeTest {

  @Autowired
  private CamelContext camelContext;

  private final String testMessageName = "BOC_STD_MSG_ZSE_0123456789.xml";

  private final File persistedTestMessage = new File("src/test/resources/fullMessagePersistenceFolder/" + testMessageName);

  @AfterEach
  public void removeTestFilesFromTestDirectory() {
    boolean testFileDeleted = persistedTestMessage.delete();
    Logger logger = Logger.getLogger((PersistFullMessageNodeTest.class.getName()));
    if (testFileDeleted) {
      logger.info("Test message: " + testMessageName + " has been deleted from test full message persistence directory");
    } else {
      logger.info("Failed to delete test message: " + testMessageName + " from test full message persistence directory");
    }
  }

  @Test
  @DisplayName("Should put Message into Full Message Persistence Directory")
  public void shouldPutMessageIntoFullMessagePersistenceDirectory() throws Exception {
    AdviceWith.adviceWith(camelContext, "persist-full-message-route", r -> {
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=" + testMessageName + "&noop=true");
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
    // Check if correct test message is in test full message persistence directory
    assertTrue(persistedTestMessage.exists());
  }
}