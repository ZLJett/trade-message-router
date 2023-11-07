<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <BOC_StandardMessage>
        <MESSAGE_ID><xsl:value-of select="/Internal_XML_Format/Message_ID"/></MESSAGE_ID>
        <TradeInfo>
            <xsl:attribute name="from"><xsl:value-of select="/Internal_XML_Format/Sender_Info/lower-case(Sender_ID)"/></xsl:attribute>
            <xsl:attribute name="to"><xsl:value-of select="/Internal_XML_Format/Recipient_Info/lower-case(Recipient_ID)"/></xsl:attribute>
            <TradeType><xsl:value-of select="/Internal_XML_Format/Trade_Info/Trade_Type"/></TradeType>
            <TradeID><xsl:value-of select="/Internal_XML_Format/Trade_Info/Trade_ID"/></TradeID>
        </TradeInfo>
        <Trades>
            <xsl:for-each select="/Internal_XML_Format/Trades/Trade">
                <Trade>
                    <TradeType><xsl:value-of select="Trade_Type"/></TradeType>
                    <AssetType><xsl:value-of select="Asset_Type"/></AssetType>
                    <AssetName><xsl:value-of select="Asset_ID"/></AssetName>
                    <AssetStandardIDNumber><xsl:value-of select="FDIC_ID"/></AssetStandardIDNumber>
                    <Currency><xsl:value-of select="Currency"/></Currency>
                    <TradeValue><xsl:value-of select="format-number(number(Trade_Value), '0.000')"/></TradeValue>
                </Trade>
            </xsl:for-each>
        </Trades>
    </BOC_StandardMessage>
</xsl:template>
</xsl:stylesheet>