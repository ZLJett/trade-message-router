<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <Internal_XML_Format>
        <Message_ID><xsl:value-of select="/BOS_StandardMessage/MESSAGE_ID"/></Message_ID>
        <Trade_Info >
            <Trade_Type><xsl:value-of select="/BOS_StandardMessage/TradeInfo/TradeType"/></Trade_Type>
            <Trade_ID><xsl:value-of select="/BOS_StandardMessage/TradeInfo/TradeID"/></Trade_ID>
        </Trade_Info>
        <Sender_Info>
            <Sender_ID><xsl:value-of select="/BOS_StandardMessage/TradeInfo/upper-case(@from)"/></Sender_ID>
            <Sender_BrokerID/>
        </Sender_Info>
        <Recipient_Info>
            <Recipient_ID><xsl:value-of select="/BOS_StandardMessage/TradeInfo/upper-case(@to)"/></Recipient_ID>
            <Recipient_BrokerID/>
        </Recipient_Info>
        <Trades><xsl:for-each select="/BOS_StandardMessage/Trades/Trade">
            <Trade>
                <Trade_Type><xsl:value-of select="TradeType"/></Trade_Type>
                <Asset_Type><xsl:value-of select="AssetType"/></Asset_Type>
                <Asset_ID><xsl:value-of select="AssetName"/></Asset_ID>
                <FDIC_ID><xsl:value-of select="AssetStandardIDNumber"/></FDIC_ID>
                <Currency><xsl:value-of select="Currency"/></Currency>
                <Trade_Value><xsl:value-of select="TradeValue"/></Trade_Value>
            </Trade>
        </xsl:for-each>
        </Trades>
    </Internal_XML_Format>
</xsl:template>
</xsl:stylesheet>