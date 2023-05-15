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
        .log("Header value: ${header.CamelFileName}")
//        .log("Header value: ${header.TestHeaderOne}")
//        .log("Header value: ${header.TestHeaderTwo}")
//        .log("Header value: ${header.TestHeaderThree}")
//        .log("Header value: ${header.TestHeaderFour}")
        .log("test route end");
  }
}