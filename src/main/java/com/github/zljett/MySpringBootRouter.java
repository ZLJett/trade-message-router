package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * old main route - hold as example - delete later
 */
//@Component
//public class MySpringBootRouter extends RouteBuilder {
//
//    @Override
//    public void configure() {
//        from("file:{{sourceFolder}}?noop=true").routeId("main-route")
//            .log("start to one")
//            .to("xslt-saxon:{{BOCtoZSE-translationTemplate}}")
//            .log("to test")
//            .to("direct:routeOne")
//            .log("back from test")
//            .to("file:{{destinationFolder}}");
//    }
//}