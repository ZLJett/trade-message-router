package com.github.zljett;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class for persisting data from each individual trade in each message
 */
@Entity
@Table(name = "trade")
public class TradeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "trade_id")
  private Long tradeId;

  @JsonProperty("Sender_ID")
  @Column(name = "sender_id")
  private String senderId;

  @JsonProperty("Recipient_ID")
  @Column(name = "recipient_id")
  private String recipientId;

  @JsonProperty("FDIC_ID")
  @Column(name = "fdic_id")
  private String fdicId;

  @JsonProperty("Asset_ID")
  @Column(name = "asset_id")
  private String assetId;

  @JsonProperty("Currency")
  @Column(name = "currency_type")
  private String currency;

  @JsonProperty("Trade_Value")
  @Column(name = "trade_value")
  private String tradeValue;

  @JsonProperty("Trade_Type")
  @Column(name = "trade_type")
  private String tradeType;

  @JsonProperty("Asset_Type")
  @Column(name = "asset_type")
  private String assetType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  private MessageEntity messageId;

  public void setTradeId(Long tradeId) {
    this.tradeId = tradeId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public void setFdicId(String fdicId) {
    this.fdicId = fdicId;
  }

  public void setAssetId(String assetId) {
    this.assetId = assetId;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public void setTradeValue(String tradeValue) {
    this.tradeValue = tradeValue;
  }

  public void setTradeType(String tradeType) {
    this.tradeType = tradeType;
  }

  public void setAssetType(String assetType) {
    this.assetType = assetType;
  }

  public void setMessageId(MessageEntity message) {
    this.messageId = message;
  }

  public MessageEntity getMessageId() {
    return messageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TradeEntity)) return false;
    TradeEntity that = (TradeEntity) o;
    return Objects.equals(tradeId, that.tradeId) && Objects.equals(senderId, that.senderId) && Objects.equals(recipientId, that.recipientId) && Objects.equals(fdicId, that.fdicId) && Objects.equals(assetId, that.assetId) && Objects.equals(currency, that.currency) && Objects.equals(tradeValue, that.tradeValue) && Objects.equals(tradeType, that.tradeType) && Objects.equals(assetType, that.assetType) && Objects.equals(messageId, that.messageId);
  }
}