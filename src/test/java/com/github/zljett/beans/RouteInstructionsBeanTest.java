package com.github.zljett.beans;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
class RouteInstructionsBeanTest {

  @Autowired
  RouteInstructionsBean routeInstructionsBean;

  @Test
  @DisplayName("Should Attach the Correct Headers for all Message")
  public void shouldAttachCorrectHeadersFor_allMessages() throws IOException {
    // Setup bean input so that it works on a message specifically coming from BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers attached by the bean itself where added correctly
    assertTrue(inputHeaders.get("MessageId").equals("0123456789"));
    assertTrue(inputHeaders.get("MessageExtension").equals("xml"));
    assertFalse(inputHeaders.get("DateReceived").isEmpty());
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from BOC")
  public void shouldAttachCorrectHeadersFor_fromBocRoute() throws IOException {
    // Setup bean input so that it works on a message specifically coming from BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the fromBOC header packet where added correctly
    assertTrue((inputHeaders.get("ToInternalTranslationInstructions")).equals("xslt-saxon:XsltTemplates/BocToInternalXsltTemplate.xsl"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to BOC")
  public void shouldAttachCorrectHeadersFor_toBocRoute() throws IOException {
    // Setup bean input so that it works on a message specifically going to BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "ZSE_TRD_MSG_BOC_987654321.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for toBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the toBOC header packet where added correctly
    assertTrue(inputHeaders.get("RoutingPath").equals("direct:PersistFullMessageRoute,direct:ToInternalTranslationRoute,direct:PersistMessageAndTradeDataRoute,direct:ToRecipientFilenameFormatRoute,direct:ToRecipientTranslationRoute,direct:ExitRoute"));
    assertTrue(inputHeaders.get("ToRecipientTranslationInstructions").equals("xslt-saxon:XsltTemplates/InternalToBocXsltTemplate.xsl"));
    assertTrue(inputHeaders.get("RecipientClientCode").equals("BOC"));
    assertTrue(inputHeaders.get("RecipientFilenameFormat").equals("BOC_STD_MSG"));
    assertTrue(inputHeaders.get("RecipientAddress").equals("file:src/main/resources/RecipientFolder"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from ZSE")
  public void shouldAttachCorrectHeadersFor_fromZseRoute() throws IOException {
    // Setup bean input so that it works on a message specifically coming from ZSE
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "ZSE_TRD_MSG_BOC_987654321.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromZSE
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the fromZSE header packet where added correctly
    assertTrue(inputHeaders.get("ToInternalTranslationInstructions").equals("xslt-saxon:XsltTemplates/ZseToInternalXsltTemplate.xsl"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to ZSE")
  public void shouldAttachCorrectHeadersFor_toZseRoute() throws IOException {
    // Setup bean input so that it works on a message specifically going to ZSE
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for toZSE
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the toZSE header packet where added correctly
    assertTrue(inputHeaders.get("RoutingPath").equals("direct:PersistFullMessageRoute,direct:ToInternalTranslationRoute,direct:PersistMessageAndTradeDataRoute,direct:ToRecipientFilenameFormatRoute,direct:ToRecipientTranslationRoute,direct:ExitRoute"));
    assertTrue(inputHeaders.get("ToRecipientTranslationInstructions").equals("xslt-saxon:XsltTemplates/InternalToZseXsltTemplate.xsl"));
    assertTrue(inputHeaders.get("RecipientClientCode").equals("ZSE"));
    assertTrue(inputHeaders.get("RecipientFilenameFormat").equals("ZSE_TRD_MSG"));
    assertTrue(inputHeaders.get("RecipientAddress").equals("file:src/main/resources/RecipientFolder"));
  }
}