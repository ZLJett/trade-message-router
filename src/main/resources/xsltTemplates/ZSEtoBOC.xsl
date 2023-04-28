<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <BOS_StandardMessage>
            <MESSAGE_ID><xsl:value-of select="/trade_messages/@message-id"/></MESSAGE_ID>
            <TradeInfo>
                <xsl:attribute name="from"><xsl:value-of select="/trade_messages/Trade_Message[1]/sender_info/lower-case(sender_ID)"/></xsl:attribute>
                <xsl:attribute name="to"><xsl:value-of select="/trade_messages/Trade_Message[1]/recipient_info/lower-case(recipient_ID)"/></xsl:attribute>
                <TradeType/>
                <TradeID/>
            </TradeInfo>
            <Trades>
            <xsl:for-each select="/trade_messages/Trade_Message">
                <Trade>
                    <TradeType><xsl:value-of select="trade_info/buysell"/></TradeType>
                    <AssetType><xsl:value-of select="trade_info/asset_type"/></AssetType>
                    <AssetName><xsl:value-of select="trade_info/asset_ID"/></AssetName>
                    <AssetStandardIDNumber><xsl:value-of select="trade_info/FDIC_ID"/></AssetStandardIDNumber>
                    <Currency><xsl:value-of select="trade_info/from_cur"/></Currency>
                    <TradeValue><xsl:value-of select="trade_info/amount"/></TradeValue>
                </Trade>
            </xsl:for-each>
            </Trades>
        </BOS_StandardMessage>
    </xsl:template>
</xsl:stylesheet>