package com.github.zljett;

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
 * Bean to add "Routing Slip" and "Instructions Slip" to message headers
 */
@Component("instructionsBean")
public class RouteInstructionsBean {
  @Value("${headerPacket}")
  String headerPacketLocation;
  public void attachHeadersPacket(String body, @Headers Map<String, String> headers) throws IOException {
    String packetName = "BOC_STANDARD_XML_ZSE";
//    TODO: add comments on each section, i.e. this creates a tree so don't have to load the full object
    String jsonString = new String(Files.readAllBytes(Paths.get(headerPacketLocation)));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(jsonString);
    JsonNode packetNode = rootNode.path(packetName);
    HashMap<String, String> headersMap = mapper.convertValue(packetNode, new TypeReference<>() {});
    headersMap.keySet().forEach(key -> headers.put(key, headersMap.get(key)));
  }
}
