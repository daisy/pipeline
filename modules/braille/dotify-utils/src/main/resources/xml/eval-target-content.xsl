<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="3.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[not(self::css:_)]" priority="1">
        <xsl:param name="style" as="item()?" tunnel="yes" select="()"/>
        <xsl:next-match>
            <xsl:with-param name="style" tunnel="yes" select="css:parse-stylesheet(@style,$style)"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="css:content[@target]">
        <xsl:param name="style" as="item()?" tunnel="yes"/>
        <xsl:variable name="target" select="@target"/>
        <xsl:apply-templates mode="copy" select="//*[@css:id=$target][1]/child::node()">
            <xsl:with-param name="anchor" select="$target"/>
            <xsl:with-param name="parent-style" select="$style"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template mode="copy" match="*|text()">
        <xsl:param name="anchor" as="xs:string" required="yes"/>
        <xsl:param name="parent-style" as="item()?" required="yes"/>
        <xsl:param name="style" as="item()?">
            <xsl:iterate select="ancestor-or-self::*[not(self::css:_)]">
                <xsl:param name="style" as="item()?" select="()"/>
                <xsl:on-completion select="$style"/>
                <xsl:next-iteration>
                    <xsl:with-param name="style" select="css:parse-stylesheet(@style,$style)"/>
                </xsl:next-iteration>
            </xsl:iterate>
        </xsl:param>
        <xsl:variable name="style" as="attribute()?" select="css:style-attribute(css:serialize-stylesheet($style,$parent-style))"/>
        <xsl:choose>
            <xsl:when test="self::*">
                <xsl:copy>
                    <xsl:sequence select="@* except @style"/>
                    <xsl:if test="exists($style)">
                        <xsl:sequence select="$style"/>
                    </xsl:if>
                    <xsl:if test="not(@css:anchor)">
                        <xsl:attribute name="css:anchor" select="$anchor"/>
                    </xsl:if>
                    <xsl:sequence select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:when test="exists($style)">
                <!-- create anonymous box for attaching style -->
                <css:box type="inline">
                    <xsl:sequence select="$style"/>
                    <xsl:sequence select="."/>
                </css:box>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="copy"
                  match="css:after|
                         css:before|
                         css:duplicate|
                         css:alternate|
                         css:footnote-call"/>

</xsl:stylesheet>
