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

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readString;
import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ToInternalTranslationRouteTest {

  @Autowired
  private CamelContext camelContext;

  @Test
  @DisplayName("Should Translate BOC XML Format Message to Internal XML Format")
  public void shouldTranslate_BocXmlFormatMessageTo_InternalXmlFormat() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-internal-translation-route", r -> {
          // Pulls specifically a message coming from BOC
          r.replaceFromWith("file:src/test/resources/TestInboundFolder?fileName=BOC_STD_MSG_ZSE_0123456789.xml&noop=true");
          // Add header needed to give route's XSLT endpoint the appropriate XSLT template for this test message
          r.weaveAddFirst().setHeader("ToInternalTranslationInstructions", constant("xslt-saxon:XsltTemplates/BocToInternalXsltTemplate.xsl"));
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
    Path correctXmlTranslationFilepath = Paths.get("src/test/resources/TestSenderToInternalComparisonXmlFiles/BocToInternalCorrectTranslation.xml");
    String correctTranslationBocToInternalXml = readString(correctXmlTranslationFilepath);
    assertTrue(testOutputXml.equals(correctTranslationBocToInternalXml));
  }

  @Test
  @DisplayName("Should Translate ZSE XML Format Message to Internal XML Format")
  public void shouldTranslate_ZseXmlFormatMessageTo_InternalXmlFormat() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-internal-translation-route", r -> {
          // Pulls specifically a message coming from ZSE
          r.replaceFromWith("file:src/test/resources/TestInboundFolder?fileName=ZSE_TRD_MSG_BOC_987654321.xml&noop=true");
          // Add header needed to give route's XSLT endpoint the appropriate XSLT template for this test message
          r.weaveAddFirst().setHeader("ToInternalTranslationInstructions", constant("xslt-saxon:XsltTemplates/ZseToInternalXsltTemplate.xsl"));
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
    Path correctXmlTranslationFilepath = Paths.get("src/test/resources/TestSenderToInternalComparisonXmlFiles/ZseToInternalCorrectTranslation.xml");
    String correctTranslationZseToInternalXml = readString(correctXmlTranslationFilepath);
    assertTrue(testOutputXml.equals(correctTranslationZseToInternalXml));
  }
}