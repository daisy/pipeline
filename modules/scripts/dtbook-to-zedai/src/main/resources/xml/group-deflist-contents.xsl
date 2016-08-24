<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="dtb" version="2.0">

    <xsl:template match="/">
        <xsl:message>Grouping contents of a definition list into items</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dtb:dl">
        <xsl:copy>
            <xsl:for-each-group select="*|text()[normalize-space()]" group-starting-with="dtb:dt">
                <xsl:element name="tmp:item">
                    <xsl:for-each select="current-group()">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
