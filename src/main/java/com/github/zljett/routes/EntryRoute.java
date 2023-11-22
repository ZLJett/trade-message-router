package com.github.zljett.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Entry Route
 */
@Component
public class EntryRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("{{message.inbound.folder}}").routeId("entry-route")
            .log(LoggingLevel.INFO, "com.github.zljett.routes.EntryRoute", "Route: ${routeId}, received Message: ${header.CamelFileName}")
            .log(LoggingLevel.INFO, "com.github.zljett.routes.EntryRoute", "Route: ${routeId}, passed Message: ${header.CamelFileName}, to RouteInstructionsBean")
            .bean("RouteInstructionsBean","attachHeadersPacket")
            .log(LoggingLevel.INFO, "com.github.zljett.routes.EntryRoute", "Route: ${routeId}, received back Message: ${header.CamelFileName}, from RouteInstructionsBean")
            .log(LoggingLevel.INFO, "com.github.zljett.routes.EntryRoute", "Route: ${routeId}, sent Message: ${header.CamelFileName}, with Routing Slip to next Route in path set in the RoutingPath Header")
            .routingSlip(header("RoutingPath"));
    }
}