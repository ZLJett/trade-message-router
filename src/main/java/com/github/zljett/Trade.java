package com.github.zljett;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Class for persisting data from each individual trade in each message
 */
@Entity
@Table(name = "trade")
public class Trade {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "trade_id")
  private int tradeId;

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
}
