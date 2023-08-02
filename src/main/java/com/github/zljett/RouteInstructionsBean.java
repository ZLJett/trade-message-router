package com.github.zljett;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    // Pull the sender and recipient from the filename so can select the correct header packets to attach to the message
    String messageFileName = headers.get("CamelFileName");
    String[] splitFileName = messageFileName.split("_");
    String senderClientCode = splitFileName[0];
    String recipientClientCode = splitFileName[3];
    String senderPacketName = "from" + senderClientCode + "_HeaderPacket";
    String recipientPacketName = "to" + recipientClientCode + "_HeaderPacket";
    // Pull message ID and file extension and add both as headers
    String[] messageIdAndExtension = splitFileName[4].split("[.]");
    String messageID = messageIdAndExtension[0];
    String messageExtension = messageIdAndExtension[1];
    headers.put("MessageId", messageID);
    headers.put("MessageExtension", messageExtension);
    // Set date and time message received as a header
    LocalDateTime timeReceived = LocalDateTime.now();
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDate = timeReceived.format(dateTimeFormat);
    headers.put("DateReceived", formattedDate);
    // Bring in headerPackets file as string and use Jackson JsonNode so only have to deserialize relevant packet
    // rather than full file. Then add each packet's JSON key/value pairs as a headers
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(new File(headerPacketFileLocation));
    // Add sender header packet
    JsonNode senderPacketNode = rootNode.path(senderPacketName);
    HashMap<String, String> senderHeadersMap = mapper.convertValue(senderPacketNode, new TypeReference<>() {});
    senderHeadersMap.keySet().forEach(key -> headers.put(key, senderHeadersMap.get(key)));
    // Add recipient header packet
    JsonNode recipientPacketNode = rootNode.path(recipientPacketName);
    HashMap<String, String> recipientHeadersMap = mapper.convertValue(recipientPacketNode, new TypeReference<>() {});
    recipientHeadersMap.keySet().forEach(key -> headers.put(key, recipientHeadersMap.get(key)));
  }
}
