package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Persist Full Message Route
 */
@Component
public class PersistFullMessageRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:PersistFullMessageRoute").routeId("persist-full-message-route")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistFullMessageRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .to("{{full.message.persistence.folder.filepath}}")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistFullMessageRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}