package com.github.zljett;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class for testing anything to be added in a bean
 */
@Component("testBean")
public class TestBean {
  @Value("${headerPacket}")
  String headerPacketLocation;
  public void addHeadersPacket(String body, @Headers Map<String, String> headers) throws JsonProcessingException, IOException {
    String packetName = "BOC_STANDARD_XML_ZSE";

//    TODO: add comments on each section, i.e. this creates a tree so don't have to load the full object
    String jsonString = new String(Files.readAllBytes(Paths.get(headerPacketLocation)));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(jsonString);
    JsonNode packetNode = rootNode.path(packetName);
    Map<String, String> headersMap = mapper.convertValue(packetNode,new TypeReference<HashMap<String, String>>() {});
    System.out.println(headersMap);
    //headersMap.keySet().forEach(key -> headers.put(key, headersMap.get(key)));
    headers.put("TestHeaderOne", "BOC-test-value-one");
  }
}
