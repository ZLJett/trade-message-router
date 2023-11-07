package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This node should be considered as a single Node with the TradeDataPersistenceAsynchronousRoute class,
 * TradesToPersistenceEntitiesNode, and the PersistTradeDataNode as they exist together to make the
 * operation of persisting message and trade data asynchronous from the main route
 */
@Component
public class PersistMessageDataNode extends RouteBuilder {

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
