package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This node should be considered as a single Node with the TradeDataPersistenceAsynchronousRoute class
 * and the TradesToPersistenceEntitiesNode as they exist together to make the operation of persisting trade data
 * asynchronous from the main route
 */
@Component
public class TradeDataPersistenceNode extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:TradeDataPersistenceRoute?concurrentConsumers=10").routeId("trade-data-persistence-route")
        .log("seda trade data persistence route start")
        .log("${threadName} -- " + "${body}")
        .to("jpa:com.github.zljett.Trade")
        .log("seda trade data persistence route end");
  }
}
