package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This node should be considered as a single Node with the TradeDataPersistenceAsynchronousRoute class,
 * PersistMessageDataNode, and the TradeDataPersistenceNode as they exist together to make the operation of
 * persisting message and trade data asynchronous from the main route
 */
@Component
public class TradesToPersistenceEntitiesNode extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=10").routeId("trades-to-persistence-entities-route")
        .log("seda trades to persistence entities route start")
        .split().tokenizeXML("Trade").streaming()
        // Unmarshalling XML for each trade in the message into trade entity pojo
        .unmarshal().jacksonXml(TradeEntity.class)
        .log("seda trades to persistence entities route route end")
        .to("seda:PersistTradeDataRoute?concurrentConsumers=10");
  }
}
