package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RoutingSlipDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class entryNodeTest {

  @Autowired
  private CamelContext camelContext;

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from BOC")
  public void shouldAttachCorrectHeadersPacketFor_fromBocRoute() throws Exception {
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          // Pulls specifically a message coming from BOC
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=BOC_STD_MSG_ZSE_0123456789.xml&noop=true");
          r.weaveByType(RoutingSlipDefinition.class).replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.expectedHeaderReceived("ToInternalTranslationInstructions","xslt-saxon:xsltTemplates/BOCtoInternal.xsl");
    mock.assertIsSatisfied();
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to BOC")
  public void shouldAttachCorrectHeadersPacketFor_toBocRoute() throws Exception {
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          // Pulls specifically a message going to BOC
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=ZSE_TRD_MSG_BOC_987654321.xml&noop=true");
          r.weaveByType(RoutingSlipDefinition.class).replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedHeaderReceived("RoutingPath","direct:persistFullMessageRoute,direct:toInternalTranslationRoute,direct:MessageDataPersistenceAsynchronousRoute,direct:toRecipientFilenameFormatRoute,direct:toRecipientTranslationRoute,direct:exitRoute");
    mock.expectedHeaderReceived("ToRecipientTranslationInstructions","xslt-saxon:xsltTemplates/InternaltoBOC.xsl");
    mock.expectedHeaderReceived("RecipientClientCode","BOC");
    mock.expectedHeaderReceived("RecipientFilenameFormat","BOC_STD_MSG");
    mock.expectedHeaderReceived("RecipientAddress","file:src/main/resources/testToFolder");
    mock.assertIsSatisfied();
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Coming from ZSE")
  public void shouldAttachCorrectHeadersPacketFor_fromZseRoute() throws Exception {
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          // Pulls specifically a message coming from ZSE
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=ZSE_TRD_MSG_BOC_987654321.xml&noop=true");
          r.weaveByType(RoutingSlipDefinition.class).replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.expectedHeaderReceived("ToInternalTranslationInstructions","xslt-saxon:xsltTemplates/ZSEtoInternal.xsl");
    mock.assertIsSatisfied();
  }

  @Test
  @DisplayName("Should Attach the Correct Headers for Message Going to ZSE")
  public void shouldAttachCorrectHeadersPacketFor_ToZseRoute() throws Exception {
    AdviceWith.adviceWith(camelContext, "entry-route", r -> {
          // Pulls specifically a message going to ZSE
          r.replaceFromWith("file:src/test/resources/testFromFolder?fileName=BOC_STD_MSG_ZSE_0123456789.xml&noop=true");
          r.weaveByType(RoutingSlipDefinition.class).replace().to("mock:routeResult");
        }
    );
    camelContext.start();
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    mock.expectedHeaderReceived("RoutingPath","direct:persistFullMessageRoute,direct:toInternalTranslationRoute,direct:MessageDataPersistenceAsynchronousRoute,direct:toRecipientFilenameFormatRoute,direct:toRecipientTranslationRoute,direct:exitRoute");
    mock.expectedHeaderReceived("ToRecipientTranslationInstructions","xslt-saxon:xsltTemplates/InternaltoZSE.xsl");
    mock.expectedHeaderReceived("RecipientClientCode","ZSE");
    mock.expectedHeaderReceived("RecipientFilenameFormat","ZSE_TRD_MSG");
    mock.expectedHeaderReceived("RecipientAddress","file:src/main/resources/testToFolder");
    mock.assertIsSatisfied();
  }
}