package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This routing class should be considered as a single route with the PersistMessageDataRoute, TradesToPersistenceEntitiesRoute,
 * and the PersistTradeDataRoute as they exist together to make the operation of persisting message and trade data
 * asynchronous from the main route.
 */
@Component
public class MessageDataPersistenceAsynchronousRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:MessageDataPersistenceAsynchronousRoute").routeId("message-data-persistence-asynchronous-route")
        .log(LoggingLevel.INFO, "com.github.zljett.MessageDataPersistenceAsynchronousRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .to("seda:PersistMessageDataRoute?concurrentConsumers=1")
        .log(LoggingLevel.INFO, "com.github.zljett.MessageDataPersistenceAsynchronousRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}