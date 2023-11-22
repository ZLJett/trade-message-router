package com.github.zljett;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This class contains two routes that exist together to make the operation of persisting message and trade data
 * asynchronous from the main route.
 */
@Component
public class PersistMessageAndTradeDataRoute extends RouteBuilder {

  @Override
  public void configure() {
    // This route sends each message to a separate thread so that message data persistence can happen separately from processing and sending the message on the main route
    from("direct:PersistMessageAndTradeDataRoute").routeId("persist-message-and-trade-data-route")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        .to("seda:AsynchronousPersistMessageAndTradeDataRoute?concurrentConsumers=1")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, finished with Message: ${header.CamelFileName}");

    // This asynchronous route is where each where the message's metadata and trade data is actually persisted
    from("seda:AsynchronousPersistMessageAndTradeDataRoute?concurrentConsumers=1").routeId("asynchronous-persist-message-and-trade-data-route")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
        // Persist message metadata
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, passed Message: ${header.CamelFileName}, to MessageDataPersistenceBean")
        .bean("MessageDataPersistenceBean","persistMessageData")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, received back Message: ${header.CamelFileName}, from MessageDataPersistenceBean")
        // Separate out each trade in the message
        .split().tokenizeXML("Trade").streaming()
        .setHeader("TradeNumber", simple("${exchangeProperty.CamelSplitIndex}++"))
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, split trade #: ${header.TradeNumber}, from Message: ${header.CamelFileName}")
        // Unmarshalling XML for each trade in the message into TradeEntity pojo
        .unmarshal().jacksonXml(TradeEntity.class)
        // Add value to message_id field of TradeEntity
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, passed trade #: ${header.TradeNumber}, from Message: ${header.CamelFileName}, to AddParentMessagePrimaryKeyToTradeEntityBean")
        .bean("AddParentMessagePrimaryKeyToTradeEntityBean", "setTradeForeignKeyToParentMessagePrimaryKey")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, received back trade #: ${header.TradeNumber}, from Message: ${header.CamelFileName}, from AddParentMessagePrimaryKeyToTradeEntityBean")
        // Add TradeEntity to database
        .to("jpa:com.github.zljett.TradeEntity")
        .log(LoggingLevel.INFO, "com.github.zljett.PersistMessageAndTradeDataRoute", "Route: ${routeId}, finished with trade #: ${header.TradeNumber}, from Message: ${header.CamelFileName}");
  }
}