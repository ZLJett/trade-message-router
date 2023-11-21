package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Internal Message Format Translation Route
 */
@Component
public class ToInternalTranslationRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:ToInternalTranslationRoute").routeId("to-internal-translation-route")
        .log(LoggingLevel.INFO, "com.github.zljett.ToInternalTranslationRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .toD("${header.ToInternalTranslationInstructions}")
        .log(LoggingLevel.INFO, "com.github.zljett.ToInternalTranslationRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}