package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * To Recipient Filename Format Route
 */
@Component
public class ToRecipientFilenameFormatRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:ToRecipientFilenameFormatRoute").routeId("to-recipient-filename-format-route")
        .log("to recipient filename format route start")
        .setHeader("CamelFileName", simple(
            "${header.RecipientFilenameFormat}" +
                "_${header.RecipientClientCode}" +
                "_${header.MessageId}" +
                ".${header.MessageExtension}"))
        .log("to recipient filename format route end");
  }
}