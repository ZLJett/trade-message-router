package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Change Filename Node
 */
@Component
public class ToRecipientFilenameFormatNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:ToRecipientFilenameFormatRoute").routeId("ToRecipientFilenameFormat-route")
        .log("ToRecipientFilenameFormat route start")
        .setHeader("CamelFileName", simple(
            "${header.RecipientFilenameFormat}" +
                "_${header.RecipientClientCode}" +
                "_${header.MessageId}" +
                ".${header.MessageExtension}"))
        .log("ToRecipientFilenameFormat route end");
  }
}