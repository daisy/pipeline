<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:di="http://www.daisy.org/ns/pipeline/tmp" 
    xmlns:mo="http://www.w3.org/ns/SMIL" 
    version="2.0" exclude-result-prefixes="#all">
    
    <xsl:template match="/di:smil-map">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each-group select="//mo:text" group-adjacent="@src">
                <content src="{current-grouping-key()}">
                    <xsl:apply-templates select="current-group()"/>
                </content>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="mo:text">
        <xsl:variable name="base" select="base-uri(.)"/>
        <text smil-id="{@id}" src-fragment="{@fragment}">
            <xsl:attribute name="xml:base" select="$base"/>
            <xsl:apply-templates select="../mo:audio"/>
        </text>
    </xsl:template>
    
    <xsl:template match="mo:audio">
        <audio xmlns="http://www.w3.org/ns/SMIL" src="{resolve-uri(@src,base-uri(.))}" id="{@id}" clipBegin="{@clipBegin}" clipEnd="{@clipEnd}"/>
    </xsl:template>
</xsl:stylesheet>