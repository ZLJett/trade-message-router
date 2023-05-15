package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Entry Node
 */
@Component
public class entryNode extends RouteBuilder {

    @Override
    public void configure() {
        from("file:{{sourceFolder}}?noop=true").routeId("main-route")
            .log("to bean")
            .bean("instructionsBean","attachHeadersPacket")
            .log("back from bean")
            .log("send with routing slip")
            .routingSlip(header("RoutingPath"));
    }
}