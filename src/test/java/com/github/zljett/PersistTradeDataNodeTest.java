package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
class PersistTradeDataNodeTest {

  @Autowired
  private TradeRepository tradeRepository;

  @Autowired
  private CamelContext camelContext;

  @Autowired
  private ProducerTemplate producerTemplate;

  @Test
  @DisplayName("Should Persist Trade Entity in Database")
  public void shouldPersistTadeEntity() throws Exception {
    // Create expected Trade Entity modeled on first trade in example trade messages
    TradeEntity expectedTradeEntity = createExpectedTradeEntity();
    AdviceWith.adviceWith(camelContext, "persist-trade-data-route", r -> {
          r.replaceFromWith("direct:testInput");
          r.weaveAddLast().to("mock:routeResult");
        }
    );
    camelContext.start();
    // As starting a Camel Context starts all routes this stops the Entry Route and prevents it from running and
    // sending messages automatically
    camelContext.getRouteController().stopRoute("entry-route");
    // This makes sure the message completes the route before the below assertion is run
    MockEndpoint mock = camelContext.getEndpoint("mock:routeResult", MockEndpoint.class);
    mock.expectedMessageCount(1);
    // Pass into route the expected Trade Entity created above
    producerTemplate.sendBody("direct:testInput", expectedTradeEntity);
    mock.assertIsSatisfied();
    Optional<TradeEntity> persistedTradeEntity = tradeRepository.findById(1L);
    // The orElse method pulls the Trade Entity out of its Optional object wrapper, or if null a blank Trade Entity
    assertTrue((persistedTradeEntity.orElse(new TradeEntity())).equals(expectedTradeEntity));
  }

  private static TradeEntity createExpectedTradeEntity() {
    TradeEntity testTradeEntity = new TradeEntity();
    // The TradeId field's actual value is generated automatically when the entity is persisted, the value below is
    // what it should be given that this entity is the only item in the test database and is used to make an accurate
    // comparison with the Trade Entity pulled out of the database later.
    testTradeEntity.setTradeId(1L);
    testTradeEntity.setSenderId("BCD");
    testTradeEntity.setRecipientId("JAX");
    testTradeEntity.setFdicId("TR02409438284");
    testTradeEntity.setAssetId("GME");
    testTradeEntity.setCurrency("CAD");
    testTradeEntity.setTradeValue("45678.912");
    testTradeEntity.setTradeType("Buy");
    testTradeEntity.setAssetType("Stock");
    // The field setMessageId is set to null as its value is contingent on an entirely separate database entry
    testTradeEntity.setMessageId(null);
    return testTradeEntity;
  }
}