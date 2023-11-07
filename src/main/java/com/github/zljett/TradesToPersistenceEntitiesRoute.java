package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This route should be considered as a single route with the TradeDataPersistenceAsynchronousRoute class,
 * PersistMessageDataRoute, and the PersistTradeDataRoute as they exist together to make the operation of
 * persisting message and trade data asynchronous from the main route.
 */
@Component
public class TradesToPersistenceEntitiesRoute extends RouteBuilder {

  @Override
  public void configure() {
    from("seda:TradesToPersistenceEntitiesRoute?concurrentConsumers=1").routeId("trades-to-persistence-entities-route")
        .log("seda trades to persistence entities route start")
        .split().tokenizeXML("Trade").streaming()
        // Unmarshalling XML for each trade in the message into trade entity pojo
        .unmarshal().jacksonXml(TradeEntity.class)
        // Add value to message_id field of TradeEntity
        .bean("AddParentMessagePrimaryKeyToTradeEntityBean", "setTradeForeignKeyToParentMessagePrimaryKey")
        .log("seda trades to persistence entities route route end")
        .to("seda:PersistTradeDataRoute?concurrentConsumers=10");
  }
}