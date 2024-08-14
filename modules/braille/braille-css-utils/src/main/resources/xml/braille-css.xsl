<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all">
    
    <xsl:import href="base.xsl"/>
    
    <!-- ============== -->
    <!-- Counter Styles -->
    <!-- ============== -->
    
    <xsl:function name="css:parse-counter-styles" as="map(xs:string,item())">
        <xsl:param name="stylesheet">
            <!--
                input is either:
                - a string, in which case it should be a regular style sheet consisting of @counter-style rules, or
                - a `css:counter-style' attribute, in which case it should have the form "& style1 { ... } & style2 { ... }"
                - an external object item
            -->
        </xsl:param>
        <xsl:map>
            <xsl:variable name="stylesheet" as="item()?">
                <xsl:choose>
                    <xsl:when test="not(exists($stylesheet))"/>
                    <xsl:when test="string($stylesheet)=''"/>
                    <xsl:when test="$stylesheet instance of xs:string or
                                    $stylesheet instance of attribute()">
                        <xsl:sequence select="s:get(css:parse-stylesheet($stylesheet),'@counter-style')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="$stylesheet"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:for-each select="for $s in $stylesheet return s:keys($s)">
                <xsl:map-entry key="replace(.,'^&amp; ','')">
                    <xsl:sequence select="s:get($stylesheet,.)"/>
                </xsl:map-entry>
            </xsl:for-each>
        </xsl:map>
    </xsl:function>
    
    <!--
        round to next .25
    -->
    <xsl:function name="css:round-line-height" as="xs:string">
        <xsl:param name="line-height" as="xs:string"/>
        <xsl:variable name="value" as="xs:double">
            <xsl:choose>
                <xsl:when test="ends-with($line-height,'%')">
                    <xsl:sequence select="number(substring($line-height,1,string-length($line-height)-1))
                                          div 100"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="number($line-height)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="value" as="xs:double" select="round($value * 4) div 4"/>
        <xsl:sequence select="format-number($value, '0.##')"/>
    </xsl:function>
    
</xsl:stylesheet>
