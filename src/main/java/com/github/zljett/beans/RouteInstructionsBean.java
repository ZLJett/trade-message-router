package com.github.zljett.beans;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean to add "Routing Slip" and "Instructions Slip" to message headers.
 */
@Component("RouteInstructionsBean")
public class RouteInstructionsBean {

  @Value("${header.packet.filepath}")
  private String headerPacketFileLocation;

  private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private HashMap<String, HashMap<String, String>> headerPackets;

  @PostConstruct
  public void convertHeaderPacketJsonToMap() throws IOException {
    // Cache HeaderPackets file as HashMap when RouteInstructionsBean class is first instantiated
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(new File(headerPacketFileLocation));
    headerPackets = mapper.convertValue(rootNode, new TypeReference<>() {});
  }

  public void attachFilenameDataAndHeaderPacketsToMessage(String body, @Headers Map<String, String> headers) {
    // Pull header packet names and other necessary message processing information from the message's filename
    HashMap<String, String> parsedFileNameData = parseFileName(headers);
    headers.put("MessageId", parsedFileNameData.get("messageID"));
    headers.put("MessageExtension", parsedFileNameData.get("messageExtension"));
    // Set date and time message received as a header
    LocalDateTime timeReceived = LocalDateTime.now();
    String formattedDate = timeReceived.format(dateTimeFormat);
    headers.put("DateReceived",formattedDate);
    // Add sender and recipient header packets to message
    String senderHeaderPacketName = parsedFileNameData.get("senderPacketName");
    String recipientHeaderPacketName = parsedFileNameData.get("recipientPacketName");
    attachHeaderPackets(headers, senderHeaderPacketName, recipientHeaderPacketName);
  }

  private HashMap<String, String> parseFileName(Map<String, String> messageHeaders) {
    HashMap<String, String> parsedFileNameData = new HashMap<>();
    // Pull the sender and recipient from the filename to create the correct header packet names
    String messageFileName = messageHeaders.get("CamelFileName");
    String[] splitFileName = messageFileName.split("_");
    String senderClientCode = splitFileName[0];
    String recipientClientCode = splitFileName[3];
    String senderPacketName = "from" + senderClientCode + "_HeaderPacket";
    parsedFileNameData.put("senderPacketName", senderPacketName);
    String recipientPacketName = "to" + recipientClientCode + "_HeaderPacket";
    parsedFileNameData.put("recipientPacketName", recipientPacketName);
    // Pull message ID and file extension from the filename
    String[] messageIdAndExtension = splitFileName[4].split("[.]");
    String messageID = messageIdAndExtension[0];
    parsedFileNameData.put("messageID", messageID);
    String messageExtension = messageIdAndExtension[1];
    parsedFileNameData.put("messageExtension", messageExtension);
    return parsedFileNameData;
  }

  public void attachHeaderPackets(Map<String, String> headers, String senderHeaderPacketName, String recipientHeaderPacketName) {
    // Add sender header packet's key/value pairs as a headers
    HashMap<String, String> senderHeadersMap = headerPackets.get(senderHeaderPacketName);
    senderHeadersMap.keySet().forEach(key -> headers.put(key, senderHeadersMap.get(key)));
    // Add recipient header packet's key/value pairs as a headers
    HashMap<String, String> recipientHeadersMap = headerPackets.get(recipientHeaderPacketName);
    recipientHeadersMap.keySet().forEach(key -> headers.put(key, recipientHeadersMap.get(key)));
  }
}