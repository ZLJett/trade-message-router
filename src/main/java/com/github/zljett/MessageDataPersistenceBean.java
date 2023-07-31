package com.github.zljett;

import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Map;

/**
 * Bean to persist message metadata
 */
@Component("messagePersistenceBean")
public class MessageDataPersistenceBean {

  @Autowired
  private MessageRepository messageRepository;

  public void persistMessageData (String body, @Headers Map<String, String> headers, @Header("CamelFileLength") long camelFileLength) throws XMLStreamException {
    // Pull message name and remove file extension
    String messageFileName = headers.get("CamelFileName");
    String[] splitFileName = messageFileName.split("\\.");
    String messageName = splitFileName[0];
    // Pull time message received
    String dateReceived = headers.get("DateReceived");
    // Find number of trades in the message
    int numberOfTrades = countTradesInMessage(body);
    // Add message data to message entity
    MessageEntity messageEntity = new MessageEntity();
    messageEntity.setMessageName(messageName);
    messageEntity.setDateReceived(dateReceived);
    messageEntity.setNumberOfTrades(numberOfTrades);
    messageEntity.setFileSizeInBytes(camelFileLength);
    // Persist message entity in database
    messageRepository.save(messageEntity);
  }

  public int countTradesInMessage (String messageBody) throws XMLStreamException {
    // Stream message XML string to count number of trades in each message
    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(messageBody));
    int numberOfTrades = 0;
    while (xmlEventReader.hasNext()) {
      XMLEvent nextEvent = xmlEventReader.nextEvent();
      if (nextEvent.isStartElement()) {
        StartElement startElement = nextEvent.asStartElement();
        if (startElement.getName().getLocalPart().equalsIgnoreCase("trade")) {
          numberOfTrades += 1;
        }
      }
    }
    return numberOfTrades;
  }
}

