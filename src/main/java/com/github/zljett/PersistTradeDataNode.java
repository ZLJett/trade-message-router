package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This node should be considered as a single Node with the TradeDataPersistenceAsynchronousRoute class,
 * PersistMessageDataNode, and the TradesToPersistenceEntitiesNode as they exist together to make the
 * operation of persisting message and trade data asynchronous from the main route
 */
@Component
public class PersistTradeDataNode extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:PersistTradeDataRoute?concurrentConsumers=10").routeId("persist-trade-data-route")
        .log("seda persist trade data route start")
        .log("${threadName} -- " + "${body}")
        .to("jpa:com.github.zljett.TradeEntity")
        .log("seda persist trade data route end");
  }
}
