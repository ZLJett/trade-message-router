<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <trade_messages format="ZSE-TRADE-MESSAGE">
        <xsl:attribute name="message-id"><xsl:value-of select="/Internal_XML_Format/Message_ID"/></xsl:attribute>
        <xsl:for-each select="/Internal_XML_Format/Trades/Trade">
            <Trade_Message>
                <sender_info>
                    <sender_ID><xsl:value-of select="/Internal_XML_Format/Sender_Info/Sender_ID"/></sender_ID>
                    <sender_BrokerID><xsl:value-of select="/Internal_XML_Format/Sender_Info/Sender_BrokerID"/></sender_BrokerID>
                </sender_info>
                <recipient_info>
                    <recipient_ID><xsl:value-of select="/Internal_XML_Format/Recipient_Info/Recipient_ID"/></recipient_ID>
                    <recipient_BrokerID><xsl:value-of select="/Internal_XML_Format/Recipient_Info/Recipient_BrokerID"/></recipient_BrokerID>
                </recipient_info>
                <trade_info>
                    <buysell><xsl:value-of select="Trade_Type"/></buysell>
                    <asset_type><xsl:value-of select="Asset_Type"/></asset_type>
                    <asset_ID><xsl:value-of select="Asset_ID"/></asset_ID>
                    <FDIC_ID><xsl:value-of select="FDIC_ID"/></FDIC_ID>
                    <from_cur><xsl:value-of select="Currency"/></from_cur>
                    <to_cur><xsl:value-of select="Currency"/></to_cur>
                    <amount><xsl:value-of select="format-number(number(Trade_Value), '0.00')"/></amount>
                </trade_info>
            </Trade_Message>
        </xsl:for-each>
    </trade_messages>
</xsl:template>
</xsl:stylesheet>