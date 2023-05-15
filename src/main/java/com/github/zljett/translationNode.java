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
    from("direct:translationRoute").routeId("translation-route")
        .log("translation route start")
        .to("xslt-saxon:{{BOCtoZSE-translationTemplate}}")
        .log("translation route end");
  }
}