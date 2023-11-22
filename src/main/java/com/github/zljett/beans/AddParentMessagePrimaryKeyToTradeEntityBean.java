package com.github.zljett.beans;

import com.github.zljett.entitiesandrepositories.MessageEntity;
import com.github.zljett.entitiesandrepositories.MessageRepository;
import com.github.zljett.entitiesandrepositories.TradeEntity;
import org.apache.camel.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Bean to add each message's primary key on the message table, message_id, to each persisted trade as the
 * value for its foreign key connection to the message table.
 */
@Component("AddParentMessagePrimaryKeyToTradeEntityBean")
public class AddParentMessagePrimaryKeyToTradeEntityBean {

  @Autowired
  private MessageRepository messageRepository;

  public void setTradeForeignKeyToParentMessagePrimaryKey(TradeEntity body, @Headers Map<String, String> headers) {
    Long messagePrimaryKey = Long.parseLong(headers.get("MessagePrimaryKey"));
    MessageEntity messageReference = messageRepository.getReferenceById(messagePrimaryKey);
    body.setMessageId(messageReference);
  }
}