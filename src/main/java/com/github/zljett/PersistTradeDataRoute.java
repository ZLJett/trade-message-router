package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This route should be considered as a single route with the TradeDataPersistenceAsynchronousRoute class,
 * PersistMessageDataRoute, and the TradesToPersistenceEntitiesRoute as they exist together to make the
 * operation of persisting message and trade data asynchronous from the main route.
 */
@Component
public class PersistTradeDataRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:PersistTradeDataRoute?concurrentConsumers=10").routeId("persist-trade-data-route")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistTradeDataRoute", "Route: ${routeId}, received trade from Message: ${header.CamelFileName}")
        .to("jpa:com.github.zljett.TradeEntity")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistTradeDataRoute", "Route: ${routeId}, finished with trade from Message: ${header.CamelFileName}");
  }
}