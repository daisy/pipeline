<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="3.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:param name="property-names" as="xs:string*"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[not(self::css:_|self::css:counter)]">
        <xsl:param name="parent-style" as="item()?" tunnel="yes" select="()"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except @style"/>
            <xsl:variable name="style" as="item()" select="css:parse-stylesheet(string(@style))"/>
            <!--
                filter
            -->
            <xsl:variable name="properties" as="item()*" select="s:iterator($style)[s:property(.)=$property-names]"/>
            <xsl:variable name="rest-style" as="item()">
                <xsl:iterate select="$properties">
                    <xsl:param name="style" as="item()" select="$style"/>
                    <xsl:on-completion select="$style"/>
                    <xsl:next-iteration>
                        <xsl:with-param name="style" select="s:remove($style,s:property(.))"/>
                    </xsl:next-iteration>
                </xsl:iterate>
            </xsl:variable>
            <xsl:sequence select="css:style-attribute($rest-style)"/>
            <!--
                inherit
            -->
            <xsl:variable name="property-names" as="xs:string*" select="for $p in $properties return s:property($p)"/>
            <xsl:variable name="inherited-style" as="item()" select="css:parse-stylesheet(string(@style),$parent-style)"/>
            <xsl:variable name="properties" as="item()*"
                          select="s:iterator($inherited-style)[s:property(.)=$property-names]"/>
            <!--
                make attributes
            -->
            <xsl:variable name="properties" as="element(css:property)*"
                          select="for $p in $properties return s:toXml($p)"/>
            <xsl:apply-templates mode="css:property-as-attribute" select="$properties"/>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="parent-style" tunnel="yes" select="$style"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
