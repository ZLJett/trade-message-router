package com.github.zljett;

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
        .log("exit route start")
        .toD("${header.RecipientAddress}");
  }
}