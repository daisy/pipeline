<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:variable name="spine" select="collection()[2]"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:attribute name="xml:base" select="base-uri(.)"/>
            <xsl:for-each select="d:file">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:variable name="href" select="@href"/>
                    <xsl:if test="$spine/d:file[following-sibling::*][@href=$href]">
                        <xsl:variable name="next-spine-item" select="$spine/d:file[@href=$href]/following-sibling::*[1]"/>
                        <xsl:attribute name="fallback" select="//d:file[@href=$next-spine-item/@href]/@id"/>
                    </xsl:if>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
