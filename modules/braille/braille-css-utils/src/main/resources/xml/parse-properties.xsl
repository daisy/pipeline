<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="3.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:param name="property-names" as="xs:string*"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[not(self::css:_|self::css:counter)]">
        <xsl:param name="parent-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:param name="parent-rest-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except @style"/>
            <!--
                inherit
            -->
            <xsl:variable name="style" as="item()?" select="css:parse-stylesheet(string(@style),$parent-style)"/>
            <!--
                filter
            -->
            <xsl:variable name="properties" as="item()*" select="s:iterate($style)[s:property(.)=$property-names]"/>
            <xsl:variable name="rest-style" as="item()?">
                <xsl:iterate select="$properties">
                    <xsl:param name="style" as="item()?" select="$style"/>
                    <xsl:on-completion select="$style"/>
                    <xsl:next-iteration>
                        <xsl:with-param name="style" select="s:remove($style,s:property(.))"/>
                    </xsl:next-iteration>
                </xsl:iterate>
            </xsl:variable>
            <xsl:sequence select="css:style-attribute(s:toString($rest-style,$parent-rest-style))"/>
            <!--
                make attributes
            -->
            <xsl:sequence select="s:toAttributes(s:merge($properties))"/>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="parent-style" tunnel="yes" select="$style"/>
                <xsl:with-param name="parent-rest-style" tunnel="yes" select="$rest-style"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
