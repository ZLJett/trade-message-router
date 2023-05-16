package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Change Filename Node
 */
@Component
public class changeFilenameNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:changeFilenameRoute").routeId("changeFilename-route")
        .log("changeFilename route start")
        .setHeader("CamelFileName", simple(
            "${header.RecipientFilenameFormat}" +
                "_${header.RecipientClientCode}" +
                "_${header.MessageId}" +
                ".${header.MessageExtension}"))
        .log("changeFilename route end");
  }
}