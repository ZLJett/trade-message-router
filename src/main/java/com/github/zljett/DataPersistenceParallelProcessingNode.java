package com.github.zljett;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DataPersistenceParallelProcessingNode extends RouteBuilder {

  @Override
  public void configure() {
    from("direct:DataPersistenceParallelProcessingRoute").routeId("data-persistence-parallel-processing-route")
        .log("data persistence parallel processing route start")
        .to("seda:")
        .log("data persistence parallel processing route end");
  }
}