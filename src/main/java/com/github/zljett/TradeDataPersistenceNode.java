package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * This node should be considered as a single Node with the TradeDataPersistenceAsynchronousRoute class
 * as that class exists to make the operation of this node asynchronous from the main route.
 */
@Component
public class TradeDataPersistenceNode extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:TradeDataPersistenceNodeRoute?concurrentConsumers=10").routeId("trade-data-persistence-node-route")
        .log("seda trade data persistence node start")
        // testing degree you can change message on SEDA route without affecting main route
        .process(exchange -> {
          exchange.getIn().setBody(new Date());
        })
        .log("${threadName} -- " + "${body}")
        .log("seda trade data persistence node end");
  }
}
