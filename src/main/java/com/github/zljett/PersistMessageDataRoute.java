package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This route should be considered as a single route with the TradeDataPersistenceAsynchronousRoute class,
 * TradesToPersistenceEntitiesRoute, and the PersistTradeDataRoute as they exist together to make the
 * operation of persisting message and trade data asynchronous from the main route.
 */
@Component
public class PersistMessageDataRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:PersistMessageDataRoute?concurrentConsumers=1").routeId("persist-message-data-route")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageDataRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        // Persist message metadata
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageDataRoute", "Route: ${routeId}, passed Message: ${header.CamelFileName}, to MessageDataPersistenceBean")
        .bean("MessageDataPersistenceBean","persistMessageData")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageDataRoute", "Route: ${routeId}, received back Message: ${header.CamelFileName}, from MessageDataPersistenceBean")
        // Persist trade data in each message
        .to("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageDataRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");
  }
}