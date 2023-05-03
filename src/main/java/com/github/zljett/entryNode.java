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
            .log("to test")
            .to("direct:routeOne")
            .log("back from test")
            .to("file:{{destinationFolder}}");
    }
}