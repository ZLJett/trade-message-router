package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This routing class should be considered as a single Node with the TradeDataPersistenceNode
 * and exists to make the operation of that node asynchronous from the main route.
 */
@Component
public class TradeDataPersistenceAsynchronousRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:TradeDataPersistenceAsynchronousRoute").routeId("trade-data-persistence-asynchronous-route")
        .log("trade data persistence asynchronous route start" + ", thread: ${threadName}")
        .to("seda:TradeDataPersistenceNodeRoute?concurrentConsumers=10")
        .log("trade data persistence asynchronous route end" + ", thread: ${threadName}");
  }
}