<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <Internal_XML_Format>
        <Message_ID><xsl:value-of select="/trade_messages/@message-id"/></Message_ID>
        <Trade_Info >
            <Trade_Type/>
            <Trade_ID/>
        </Trade_Info>
        <Sender_Info>
            <Sender_ID><xsl:value-of select="/trade_messages/Trade_Message[1]/sender_info/sender_ID"/></Sender_ID>
            <Sender_BrokerID><xsl:value-of select="/trade_messages/Trade_Message[1]/sender_info/sender_BrokerID"/></Sender_BrokerID>
        </Sender_Info>
        <Recipient_Info>
            <Recipient_ID><xsl:value-of select="/trade_messages/Trade_Message[1]/recipient_info/recipient_ID"/></Recipient_ID>
            <Recipient_BrokerID><xsl:value-of select="/trade_messages/Trade_Message[1]/recipient_info/recipient_BrokerID"/></Recipient_BrokerID>
        </Recipient_Info>
        <Trades><xsl:for-each select="/trade_messages/Trade_Message">
            <Trade>
                <Trade_Type><xsl:value-of select="trade_info/buysell"/></Trade_Type>
                <Asset_Type><xsl:value-of select="trade_info/asset_type"/></Asset_Type>
                <Asset_ID><xsl:value-of select="trade_info/asset_ID"/></Asset_ID>
                <FDIC_ID><xsl:value-of select="trade_info/FDIC_ID"/></FDIC_ID>
                <Currency><xsl:value-of select="trade_info/from_cur"/></Currency>
                <Trade_Value><xsl:value-of select="trade_info/amount"/></Trade_Value>
            </Trade>
        </xsl:for-each>
        </Trades>
    </Internal_XML_Format>
</xsl:template>
</xsl:stylesheet>