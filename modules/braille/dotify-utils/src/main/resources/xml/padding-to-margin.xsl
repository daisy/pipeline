<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:box">
        <xsl:param name="source-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:param name="result-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(string(@style),$source-style)"/>
        <xsl:copy>
            <xsl:sequence select="@* except @style"/>
            <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($source-style,$result-style))"/>
            <xsl:apply-templates>
                <xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
                <xsl:with-param name="result-style" tunnel="yes" select="$source-style"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:padding-left|
                         @css:padding-right|
                         @css:padding-top|
                         @css:padding-bottom"/>
    
    <xsl:template match="css:box[@type='block'][@css:padding-left or
                                                @css:padding-right or
                                                @css:padding-top or
                                                @css:padding-bottom]">
        <xsl:param name="source-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:param name="result-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(string(@style),$source-style)"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except @style"/>
            <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($source-style,$result-style))"/>
            <xsl:element name="css:box">
                <xsl:attribute name="type" select="'block'"/>
                <xsl:attribute name="css:collapsing-margins" select="'no'"/>
                <xsl:apply-templates select="@css:padding-left|
                                             @css:padding-right|
                                             @css:padding-top|
                                             @css:padding-bottom" mode="padding-to-margin"/>
                <xsl:apply-templates>
                    <xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
                    <xsl:with-param name="result-style" tunnel="yes" select="css:parse-stylesheet((),$source-style)"/>
                </xsl:apply-templates>
            </xsl:element>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:padding-left|
                         @css:padding-right|
                         @css:padding-top|
                         @css:padding-bottom" mode="padding-to-margin">
        <xsl:attribute name="css:{replace(local-name(),'padding','margin')}" select="."/>
    </xsl:template>
    
</xsl:stylesheet>
