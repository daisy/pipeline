<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all">

    <xsl:output method="html" indent="yes"/>
    <xsl:strip-space elements="html:li"/>

    <xsl:template match="@*|node()">
        <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
    </xsl:template>

    <xsl:template match="html:ol">
        <xsl:param name="parentNumber" />
        <ol>
            <xsl:copy-of select="@*" />
            <xsl:for-each select="./html:li">
                <xsl:variable name="number" select="concat($parentNumber, position(), '.')"/>
                <li><xsl:copy-of select="@*" /><xsl:value-of select="concat($number, ' ')"/>
                    <xsl:apply-templates select="node()">
                        <xsl:with-param name="parentNumber" select="$number"/>
                    </xsl:apply-templates>
                </li>
            </xsl:for-each>
        </ol>
    </xsl:template>

</xsl:stylesheet>
