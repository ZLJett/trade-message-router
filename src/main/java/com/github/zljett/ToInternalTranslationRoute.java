package com.github.zljett;

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
        .log("to internal translation route start")
        .toD("${header.ToInternalTranslationInstructions}")
        .log("to internal translation route end");
  }
}