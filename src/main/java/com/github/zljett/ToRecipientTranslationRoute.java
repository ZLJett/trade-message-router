package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Recipient Message Format Translation Route
 */
@Component
public class ToRecipientTranslationRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:ToRecipientTranslationRoute").routeId("to-recipient-translation-route")
        .log("to recipient translation route start")
        .toD("${header.ToRecipientTranslationInstructions}")
        .log("to recipient translation route end");
  }
}