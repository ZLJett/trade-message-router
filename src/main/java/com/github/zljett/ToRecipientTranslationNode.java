package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Recipient Message Format Translation Node
 */
@Component
public class ToRecipientTranslationNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:toRecipientTranslationRoute").routeId("recipient-translation-route")
        .log("to recipient translation route start")
        .toD("${header.ToRecipientTranslationInstructions}")
        .log("to recipient translation route end");
  }
}