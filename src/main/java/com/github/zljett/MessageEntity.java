package com.github.zljett;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class for persisting data from each individual message
 */
@Entity
@Table(name = "message")
public class MessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "message_id")
  private Long messageId;

  @Column(name = "message_name")
  private String messageName;

  @Column(name = "date_received")
  private String dateReceived;

  @Column(name = "number_of_trades")
  private int numberOfTrades;

  @Column(name = "file_size_bytes")
  private long fileSizeInBytes;

  public Long getMessageId() {
    return messageId;
  }

  public void setMessageId(Long messageId) {
    this.messageId = messageId;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

  public void setDateReceived(String dateReceived) {
    this.dateReceived = dateReceived;
  }

  public void setNumberOfTrades(int numberOfTrades) {
    this.numberOfTrades = numberOfTrades;
  }

  public void setFileSizeInBytes(long fileSize) {
    this.fileSizeInBytes = fileSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MessageEntity)) return false;
    MessageEntity that = (MessageEntity) o;
    return numberOfTrades == that.numberOfTrades && fileSizeInBytes == that.fileSizeInBytes && Objects.equals(messageId, that.messageId) && Objects.equals(messageName, that.messageName) && Objects.equals(dateReceived, that.dateReceived);
  }
}
