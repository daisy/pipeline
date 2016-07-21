<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:louis="http://liblouis.org/liblouis">
    
    <xsl:template match="@xml:space"/>
    
    <xsl:template match="*[@xml:space]">
        <xsl:next-match>
            <xsl:with-param name="preserve" select="@xml:space='preserve'" tunnel="yes"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:param name="preserve" as="xs:boolean" select="false()" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="$preserve">
                    <xsl:for-each-group select="*|text()" group-adjacent="boolean(self::*)">
                        <xsl:choose>
                            <xsl:when test="current-grouping-key()">
                                <xsl:for-each select="current-group()">
                                    <xsl:apply-templates select="."/>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="string-join(current-group()/string(.), '')!=''">
                                <xsl:element name="louis:space">
                                    <xsl:sequence select="current-group()"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="*|text()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*|text()">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
