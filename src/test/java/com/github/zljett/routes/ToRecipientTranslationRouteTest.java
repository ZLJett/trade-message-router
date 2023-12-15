package com.github.zljett.routes;

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
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readString;
import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource("file:src/test/resources/test.properties")
@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ToRecipientTranslationRouteTest {

  @Autowired
  private CamelContext camelContext;

  private static final String testMessageName = "TestMessageInInternalXmlFormat.xml";

  @Test
  @DisplayName("Should Translate Internal XML Format Message to BOC XML Format")
  public void shouldTranslate_InternalXmlFormatMessageTo_BocXmlFormat() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-recipient-translation-route", r -> {
          // Pulls a message in Internal XML format
          r.replaceFromWith("file:src/test/resources/TestInternalXmlFormatMessages?fileName=" + testMessageName + "&noop=true");
          // Add header needed to give route's XSLT endpoint the appropriate XSLT template for a message going to BOC
          r.weaveAddFirst().setHeader("ToRecipientTranslationInstructions", constant("xslt-saxon:XsltTemplates/InternalToBocXsltTemplate.xsl"));
          r.weaveAddLast().to("mock:RouteResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:RouteResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.assertIsSatisfied();
    // Check if XSLT endpoint's output XML string matches the correct XML for test message
    String testOutputXml = (String) mock.getExchanges().get(0).getIn().getBody();
    Path correctXmlTranslationFilepath = Paths.get("src/test/resources/TestInternalToRecipientComparisonXmlFiles/InternalToBocCorrectTranslation.xml");
    String correctTranslationInternalToBocXml = readString(correctXmlTranslationFilepath);
    assertEquals(correctTranslationInternalToBocXml, testOutputXml);
  }

  @Test
  @DisplayName("Should Translate Internal XML Format Message to ZSE XML Format")
  public void shouldTranslate_InternalXmlFormatMessageTo_ZseXmlFormat() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-recipient-translation-route", r -> {
          // Pulls a message in Internal XML format
          r.replaceFromWith("file:src/test/resources/TestInternalXmlFormatMessages?fileName=" + testMessageName + "&noop=true");
          // Add header needed to give route's XSLT endpoint the appropriate XSLT template for a message going to ZSE
          r.weaveAddFirst().setHeader("ToRecipientTranslationInstructions", constant("xslt-saxon:XsltTemplates/InternalToZseXsltTemplate.xsl"));
          r.weaveAddLast().to("mock:RouteResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:RouteResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.assertIsSatisfied();
    // Check if XSLT endpoint's output XML string matches the correct XML for test message
    String testOutputXml = (String) mock.getExchanges().get(0).getIn().getBody();
    Path correctXmlTranslationFilepath = Paths.get("src/test/resources/TestInternalToRecipientComparisonXmlFiles/InternalToZseCorrectTranslation.xml");
    String correctTranslationInternalToZseXml = readString(correctXmlTranslationFilepath);
    assertEquals(correctTranslationInternalToZseXml, testOutputXml);
  }
}