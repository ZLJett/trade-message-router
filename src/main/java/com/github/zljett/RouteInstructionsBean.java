package com.github.zljett;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
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
    // Pull message ID and file extension and add both as headers
    String[] messageIdAndExtension = splitFileName[4].split("[.]");
    String messageID = messageIdAndExtension[0];
    String messageExtension = messageIdAndExtension[1];
    headers.put("MessageId", messageID);
    headers.put("MessageExtension", messageExtension);
    // Bring in headerPackets file as string and use Jackson JsonNode so only have to deserialize relevant packet
    // rather than full file, then add each JSON object as a header
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(new File(headerPacketFileLocation));
    JsonNode packetNode = rootNode.path(packetName);
    HashMap<String, String> headersMap = mapper.convertValue(packetNode, new TypeReference<>() {});
    headersMap.keySet().forEach(key -> headers.put(key, headersMap.get(key)));
  }
}
