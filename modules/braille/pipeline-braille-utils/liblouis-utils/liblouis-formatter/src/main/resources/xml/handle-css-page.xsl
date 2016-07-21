<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[contains(string(@style), '@page')]">
        <xsl:param name="current-page-style" as="xs:string?" tunnel="yes"/>
        <xsl:variable name="rules" as="element()*" select="css:parse-stylesheet(@style)"/>
        <xsl:variable name="page-style" as="xs:string?" select="$rules[@selector='@page']/@style"/>
        <xsl:choose>
            <xsl:when test="$page-style">
                <xsl:copy>
                    <xsl:sequence select="@*[not(name()='style')]"/>
                    <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($rules[not(@selector='@page')]))"/>
                    <xsl:if test="not($current-page-style=$page-style)">
                        <xsl:attribute name="css:page" select="$page-style"/>
                        <xsl:if test="not(@xml:id)">
                            <xsl:attribute name="xml:id" select="generate-id()"/>
                        </xsl:if>
                    </xsl:if>
                    <xsl:apply-templates select="node()">
                        <xsl:with-param name="current-page-style" select="$page-style" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
