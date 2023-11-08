package com.github.zljett;

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
        .log("seda persist message data route start")
        // Persist message metadata
        .bean("MessageDataPersistenceBean","persistMessageData")
        // Persist trade data in each message
        .to("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1")
        .log("seda persist message data route end");
  }
}