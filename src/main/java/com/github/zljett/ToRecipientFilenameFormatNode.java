package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Recipient Filename Format Node
 */
@Component
public class ToRecipientFilenameFormatNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:toRecipientFilenameFormatRoute").routeId("recipient-filename-format-route")
        .log("to recipient filename format route start")
        .setHeader("CamelFileName", simple(
            "${header.RecipientFilenameFormat}" +
                "_${header.RecipientClientCode}" +
                "_${header.MessageId}" +
                ".${header.MessageExtension}"))
        .log("to recipient filename format route end");
  }
}