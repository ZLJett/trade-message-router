package com.github.zljett.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
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

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ToRecipientFilenameFormatRouteTest {

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private ProducerTemplate producerTemplate;

  @Test
  @DisplayName("Should Change Current Filename to Recipient's Filename Format Where BOC is the Recipient")
  public void shouldChangeFilenameToRecipientFormat_BocAsRecipient() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-recipient-filename-format-route", r -> {
          // Add filename related headers from toBOC_HeaderPacket that would have been added by RouteInstructionsBean
          r.weaveAddFirst().setHeader("RecipientFilenameFormat", constant("BOC_STD_MSG"));
          r.weaveAddFirst().setHeader("RecipientClientCode", constant("BOC"));
          // These headers that would have been added by RouteInstructionsBean are set to values that correspond to
          // the test message going to BOC
          r.weaveAddFirst().setHeader("MessageId", constant("987654321"));
          r.weaveAddFirst().setHeader("MessageExtension", constant("xml"));
          r.weaveAddLast().to("mock:RouteResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    MockEndpoint mock = camelContext.getEndpoint("mock:RouteResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    // Check if setHeader endpoint generated the correct filename for a message going to BOC and set the new name
    // to the CamelFileName header where the message's name is drawn from
    mock.expectedHeaderReceived("CamelFileName","BOC_STD_MSG_BOC_987654321.xml");
    // Blank message used for test as body of message, as message's starting filename is irrelevant for this test
    producerTemplate.sendBody("direct:ToRecipientFilenameFormatRoute", "");
    mock.assertIsSatisfied();
  }

  @Test
  @DisplayName("Should Change Current Filename to Recipient's Filename Format Where ZSE is the Recipient")
  public void shouldChangeFilenameToRecipientFormat_ZseAsRecipient() throws Exception {
    AdviceWith.adviceWith(camelContext, "to-recipient-filename-format-route", r -> {
          // Add filename related headers from toZSE_HeaderPacket that would have been added by RouteInstructionsBean
          r.weaveAddFirst().setHeader("RecipientFilenameFormat", constant("ZSE_TRD_MSG"));
          r.weaveAddFirst().setHeader("RecipientClientCode", constant("ZSE"));
          // These headers that would have been added by RouteInstructionsBean are set to values that correspond to
          // the test message going to ZSE
          r.weaveAddFirst().setHeader("MessageId", constant("0123456789"));
          r.weaveAddFirst().setHeader("MessageExtension", constant("xml"));
          r.weaveAddLast().to("mock:RouteResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    MockEndpoint mock = camelContext.getEndpoint("mock:RouteResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    // Check if setHeader endpoint generated the correct filename for a message going to ZSE and set the new name
    // to the CamelFileName header where the message's name is drawn from
    mock.expectedHeaderReceived("CamelFileName","ZSE_TRD_MSG_ZSE_0123456789.xml");
    // Blank message used for test as body of message, as message's starting filename is irrelevant for this test
    producerTemplate.sendBody("direct:ToRecipientFilenameFormatRoute", "");
    mock.assertIsSatisfied();
  }
}