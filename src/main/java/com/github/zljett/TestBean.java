package com.github.zljett;

import org.apache.camel.Headers;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * A Class for testing anything to be added in a bean
 */
@Component
public class TestBean {

  public void addHeadersPacket(String body, @Headers Map<String, String> headers) {
      String packetName = "BOC_STANDARD_XML_ZSE";
      headers.put("TestHeaderOne", "BOC-test-value-one");
  }
}
