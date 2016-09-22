<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <xsl:param name="xml-declaration" required="yes"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:choose>
                <xsl:when test="matches(text(),'&lt;?xml[ \t\r\n]')">
                    <xsl:value-of select="$xml-declaration"/>
                    <xsl:choose>
                        <xsl:when test="$xml-declaration='' and substring(substring-after(text(),'&gt;'),1,1) = '&#x0a;'">
                            <xsl:value-of select="substring-after(text(),'&gt;&#x0a;')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="substring-after(text(),'&gt;')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$xml-declaration"/>
                    <xsl:value-of select="'&#x0a;'"/>
                    <xsl:value-of select="text()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
