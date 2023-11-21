package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Exit Route
 */
@Component
public class ExitRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:ExitRoute").routeId("exit-route")
        .log(LoggingLevel.INFO, "com.github.zljett.ExitRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .toD("${header.RecipientAddress}")
        .log(LoggingLevel.INFO, "com.github.zljett.ExitRoute", "Route: ${routeId}, sent Message: ${header.CamelFileName}, to Message Recipient using RecipientAddress Header");
  }
}