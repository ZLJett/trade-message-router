package com.github.zljett.routes;

import org.apache.camel.LoggingLevel;
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
        .log(LoggingLevel.INFO, "com.github.zljett.routes.ToRecipientTranslationRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .toD("${header.ToRecipientTranslationInstructions}")
        .log(LoggingLevel.INFO, "com.github.zljett.routes.ToRecipientTranslationRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}