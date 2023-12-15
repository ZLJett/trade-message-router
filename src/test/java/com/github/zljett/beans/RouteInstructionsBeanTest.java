package com.github.zljett.beans;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("file:src/test/resources/test.properties")
@SpringBootTest
@CamelSpringBootTest
class RouteInstructionsBeanTest {

  @Autowired
  RouteInstructionsBean routeInstructionsBean;

  @Test
  @DisplayName("Should Attach the Correct Headers for all Message")
  public void shouldAttachCorrectHeadersFor_allMessages() {
    // Setup bean input so that it works on a message specifically coming from BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers attached by the bean itself where added correctly
    assertEquals("0123456789", inputHeaders.get("MessageId"));
    assertEquals("xml", inputHeaders.get("MessageExtension"));
    assertFalse(inputHeaders.get("DateReceived").isEmpty());
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from BOC")
  public void shouldAttachCorrectHeadersFor_fromBocRoute() {
    // Setup bean input so that it works on a message specifically coming from BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the fromBOC header packet where added correctly
    assertEquals("xslt-saxon:XsltTemplates/BocToInternalXsltTemplate.xsl", inputHeaders.get("ToInternalTranslationInstructions"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to BOC")
  public void shouldAttachCorrectHeadersFor_toBocRoute() {
    // Setup bean input so that it works on a message specifically going to BOC
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "ZSE_TRD_MSG_BOC_987654321.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for toBOC
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the toBOC header packet where added correctly
    assertEquals("direct:PersistFullMessageRoute,direct:ToInternalTranslationRoute,direct:PersistMessageAndTradeDataRoute,direct:ToRecipientFilenameFormatRoute,direct:ToRecipientTranslationRoute,direct:ExitRoute", inputHeaders.get("RoutingPath"));
    assertEquals("xslt-saxon:XsltTemplates/InternalToBocXsltTemplate.xsl", inputHeaders.get("ToRecipientTranslationInstructions"));
    assertEquals("BOC", inputHeaders.get("RecipientClientCode"));
    assertEquals("BOC_STD_MSG", inputHeaders.get("RecipientFilenameFormat"));
    assertEquals("file:src/test/resources/TestRecipientFolder", inputHeaders.get("RecipientAddress"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from ZSE")
  public void shouldAttachCorrectHeadersFor_fromZseRoute() {
    // Setup bean input so that it works on a message specifically coming from ZSE
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "ZSE_TRD_MSG_BOC_987654321.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for fromZSE
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the fromZSE header packet where added correctly
    assertEquals("xslt-saxon:XsltTemplates/ZseToInternalXsltTemplate.xsl", inputHeaders.get("ToInternalTranslationInstructions"));
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to ZSE")
  public void shouldAttachCorrectHeadersFor_toZseRoute() {
    // Setup bean input so that it works on a message specifically going to ZSE
    Map<String, String> inputHeaders = new HashMap<>();
    inputHeaders.put("CamelFileName", "BOC_STD_MSG_ZSE_0123456789.xml");
    // Give bean minimum required information to test its core function of attaching correct header packet for toZSE
    routeInstructionsBean.attachFilenameDataAndHeaderPacketsToMessage("", inputHeaders);
    // Checks that the headers from the toZSE header packet where added correctly
    assertEquals("direct:PersistFullMessageRoute,direct:ToInternalTranslationRoute,direct:PersistMessageAndTradeDataRoute,direct:ToRecipientFilenameFormatRoute,direct:ToRecipientTranslationRoute,direct:ExitRoute", inputHeaders.get("RoutingPath"));
    assertEquals("xslt-saxon:XsltTemplates/InternalToZseXsltTemplate.xsl", inputHeaders.get("ToRecipientTranslationInstructions"));
    assertEquals("ZSE", inputHeaders.get("RecipientClientCode"));
    assertEquals("ZSE_TRD_MSG", inputHeaders.get("RecipientFilenameFormat"));
    assertEquals("file:src/test/resources/TestRecipientFolder", inputHeaders.get("RecipientAddress"));
  }
}