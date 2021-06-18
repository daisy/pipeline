<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all">

    <xsl:output method="html" indent="yes"/>
    <xsl:strip-space elements="html:li"/>

    <xsl:template match="@*|node()">
        <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
    </xsl:template>

    <xsl:template match="html:ol[not(@class='plain') and not(ancestor::html:ol[@class='plain'])]">
        <xsl:variable name="startpos">
            <xsl:choose>
                <xsl:when test="@start"><xsl:value-of select="@start - 1"/></xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="format_type">
            <xsl:choose>
                <xsl:when test="@type"><xsl:value-of select="@type"/></xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <ol>
            <xsl:copy-of select="@*" />
            <xsl:for-each select="./html:li">
                <xsl:variable name="number" select="$startpos + position()"/>
                <xsl:variable name="number_format">
                    <xsl:choose>
                        <xsl:when test="$format_type = 'a'"><xsl:number format="a" value="$number"/></xsl:when>
                        <xsl:when test="$format_type = 'A'"><xsl:number format="A" value="$number"/></xsl:when>
                        <xsl:when test="$format_type = 'i'"><xsl:number format="i" value="$number"/></xsl:when>
                        <xsl:when test="$format_type = 'I'"><xsl:number format="I" value="$number"/></xsl:when>
                        <xsl:otherwise><xsl:number format="1" value="$number"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <li><xsl:copy-of select="@*" /><span tts:speech-only=""><xsl:value-of select="concat($number_format, '.', ' ')"/></span>
                    <xsl:apply-templates select="node()"/>
                </li>
            </xsl:for-each>
        </ol>
    </xsl:template>

</xsl:stylesheet>
