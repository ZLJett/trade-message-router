package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A class for testing if Spring Boot can connect routes across multiple files
 */
@Component
public class testNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:routeOne").routeId("test-route")
        .log("test route hit");
  }
}