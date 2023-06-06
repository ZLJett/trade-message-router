package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Internal Message Format Translation Node
 */
@Component
public class ToInternalTranslationNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:toInternalTranslationRoute").routeId("internal-translation-route")
        .log("translation route start")
        .toD("${header.ToInternalTranslationInstructions}")
        .log("translation route end");
  }
}