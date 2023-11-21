package com.github.zljett;

import org.apache.camel.LoggingLevel;
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
        .log(LoggingLevel.INFO, "com.github.zljett.TradesToPersistenceEntitiesRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .split().tokenizeXML("Trade").streaming()
        // Unmarshalling XML for each trade in the message into TradeEntity pojo
        .unmarshal().jacksonXml(TradeEntity.class)
        // Add value to message_id field of TradeEntity
        .log(LoggingLevel.INFO, "com.github.zljett.TradesToPersistenceEntitiesRoute", "Route: ${routeId}, passed trade from Message: ${header.CamelFileName}, to AddParentMessagePrimaryKeyToTradeEntityBean")
        .bean("AddParentMessagePrimaryKeyToTradeEntityBean", "setTradeForeignKeyToParentMessagePrimaryKey")
        .log(LoggingLevel.INFO, "com.github.zljett.TradesToPersistenceEntitiesRoute", "Route: ${routeId}, received back trade from Message: ${header.CamelFileName}, from AddParentMessagePrimaryKeyToTradeEntityBean")
        // Add TradeEntity to database
        .to("seda:PersistTradeDataRoute?concurrentConsumers=10")
        .log(LoggingLevel.INFO, "com.github.zljett.TradesToPersistenceEntitiesRoute", "Route: ${routeId}, finished with trade from Message: ${header.CamelFileName}");
  }
}