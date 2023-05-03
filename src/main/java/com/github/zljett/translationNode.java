package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Translation Node
 */
@Component
public class translationNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:routeOne").routeId("translation-route")
        .log("translation route hit");
  }
}