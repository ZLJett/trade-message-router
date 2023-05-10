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
            .log("start to one")
            .to("xslt-saxon:{{BOCtoZSE-translationTemplate}}")
            .log("to bean")
            .bean("testBean","addHeadersPacket")
            //.bean(new TestBean())
            .log("back from bean")
            .log("to test")
            .to("direct:testRoute")
            .log("back from test")
            .log("test to end")
            .to("file:{{destinationFolder}}");
    }
}