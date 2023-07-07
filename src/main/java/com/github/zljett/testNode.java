package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A class for testing new nodes
 */
@Component
public class testNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:testRoute").routeId("test-route")
        .log("test route start")
        .log("Property value: {{ftpComponentTradeMessageServer}}")
        .log("Header value: ${header.CamelFileName}")
        .log("test route end");
  }
}