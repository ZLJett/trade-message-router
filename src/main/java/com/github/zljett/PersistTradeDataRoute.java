package com.github.zljett;

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
        .log("seda persist trade data route start")
        .to("jpa:com.github.zljett.TradeEntity")
        .log("seda persist trade data route end");
  }
}
