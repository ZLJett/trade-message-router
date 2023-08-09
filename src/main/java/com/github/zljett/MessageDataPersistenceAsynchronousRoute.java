package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This routing class should be considered as a single Node with the PersistMessageDataNode, TradesToPersistenceEntitiesNode,
 * and the PersistTradeDataNode as they exist together to make the operation of persisting message and trade data
 * asynchronous from the main route
 */
@Component
public class MessageDataPersistenceAsynchronousRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:MessageDataPersistenceAsynchronousRoute").routeId("message-data-persistence-asynchronous-route")
        .log("message data persistence asynchronous route start")
        .to("seda:PersistMessageDataRoute?concurrentConsumers=1")
        .log("message data persistence asynchronous route end");
  }
}