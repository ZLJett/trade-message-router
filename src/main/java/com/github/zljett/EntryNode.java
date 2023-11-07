package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Entry Node
 */
@Component
public class EntryNode extends RouteBuilder {

    @Override
    public void configure() {
        from("{{message.sender.folder}}").routeId("entry-route")
            .log("to bean")
            .bean("RouteInstructionsBean","attachHeadersPacket")
            .log("back from bean")
            .log("send with routing slip")
            .routingSlip(header("RoutingPath"));
    }
}