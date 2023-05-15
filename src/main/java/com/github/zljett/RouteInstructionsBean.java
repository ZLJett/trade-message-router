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
  @Value("${headerPacketFilePath}")
  String headerPacketFileLocation;
  public void attachHeadersPacket(String body, @Headers Map<String, String> headers) throws IOException {
    // Pull the sender and recipient from the filename so can select the correct header packet to attach to the message
    String messageFileName = headers.get("CamelFileName");
    String[] splitFileName = messageFileName.split("_");
    String packetName = splitFileName[0] + "to" + splitFileName[3] + "_HeaderPacket";
    // Split the message ID from the file extension to get the message ID number and add ID as header
    String messageID = splitFileName[4].split("[.]")[0];
    headers.put("MessageId", messageID);

//    TODO: add comments on each section, i.e. this creates a tree so don't have to load the full object
    String jsonString = new String(Files.readAllBytes(Paths.get(headerPacketFileLocation)));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(jsonString);
    JsonNode packetNode = rootNode.path(packetName);
    HashMap<String, String> headersMap = mapper.convertValue(packetNode, new TypeReference<>() {});
    headersMap.keySet().forEach(key -> headers.put(key, headersMap.get(key)));
  }
}
