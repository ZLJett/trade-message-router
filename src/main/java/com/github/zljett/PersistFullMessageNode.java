package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Persist Full Message Node
 */
@Component
public class PersistFullMessageNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:persistFullMessageRoute").routeId("persist-full-message-route")
        .log("to persist full message route start")
        .to("{{full.message.persistence.folder.filepath}}")
        .log("to persist full message route end");
  }
}

