package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto-detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("file:{{sourceFolder}}?noop=true").routeId("main-route")
            .log("start to one")
            .to("xslt-saxon:{{BOCtoZSE-translationTemplate}}")
//            .log("one to end")
            .log("to test")
            .to("direct:routeOne")
            .to("file:{{destinationFolder}}");
    }
}