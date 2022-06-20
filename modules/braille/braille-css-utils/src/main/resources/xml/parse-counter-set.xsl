<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:param name="counter-names"/>
    <xsl:param name="exclude-counter-names"/>
    <xsl:variable name="counter-names-list" as="xs:string*" select="tokenize(normalize-space($counter-names), ' ')"/>
    <xsl:variable name="exclude-counter-names-list" as="xs:string*" select="tokenize(normalize-space($exclude-counter-names), ' ')"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:counter-reset|
                         @css:counter-set|
                         @css:counter-increment">
        <xsl:variable name="pairs" as="element(css:counter-set)*" select="css:parse-stylesheet(.)/css:counter-set"/>
        <xsl:variable name="property" as="xs:string" select="local-name()"/>
        <xsl:for-each select="if ($counter-names='#all')
                              then $pairs[not(@name=$exclude-counter-names-list)]
                              else $pairs[@name=$counter-names-list]">
            <xsl:attribute name="css:{$property}-{@name}" select="@value"/>
        </xsl:for-each>
        <xsl:variable name="other-pairs" as="element(css:counter-set)*"
                      select="if ($counter-names='#all')
                              then $pairs[@name=$exclude-counter-names-list]
                              else $pairs[not(@name=$counter-names-list)]"/>
        <xsl:if test="$other-pairs">
            <xsl:variable name="other-pairs" as="element(css:property)">
                <css:property name="{$property}">
                    <xsl:sequence select="$other-pairs"/>
                </css:property>
            </xsl:variable>
            <xsl:apply-templates mode="css:property-as-attribute" select="$other-pairs"/>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
