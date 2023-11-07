package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


/**
 * Exit Node
 */
@Component
public class ExitNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:exitRoute").routeId("exit-route")
        .log("exit route start")
        .toD("${header.RecipientAddress}");
  }
}