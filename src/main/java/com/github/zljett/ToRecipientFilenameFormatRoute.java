package com.github.zljett;

import org.apache.camel.LoggingLevel;
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
        .log(LoggingLevel.INFO, "com.github.zljett.ToRecipientFilenameFormatRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .setHeader("CamelFileName", simple(
            "${header.RecipientFilenameFormat}" +
                "_${header.RecipientClientCode}" +
                "_${header.MessageId}" +
                ".${header.MessageExtension}"))
        .log(LoggingLevel.INFO, "com.github.zljett.ToRecipientFilenameFormatRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}