package com.github.zljett;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * old base test - hold as example - delete later
 */
//@SpringBootTest
//@CamelSpringBootTest
//public class MySpringBootApplicationTest {
//
//	@Autowired
//	private CamelContext camelContext;
//
//	@Autowired
//	private ProducerTemplate producerTemplate;
//
//	@Test
//	public void test() throws Exception {
//		MockEndpoint mock = camelContext.getEndpoint("mock:file:{{destinationFolder}}", MockEndpoint.class);
//
//		AdviceWith.adviceWith(camelContext, "main-route", r -> {
//					r.replaceFromWith("direct:start");
//					// mock all so they will not produce any output
//					r.mockEndpoints();
//				}
//		);
//
//		// setting expectations
//		mock.expectedMessageCount(1);
//
//		// invoking consumer
//		producerTemplate.sendBody("direct:start", "Test Test Test");
//
//		// asserting mock is satisfied
//		mock.assertIsSatisfied();
//	}
//}
