package com.github.zljett;

import javax.persistence.*;

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
}
