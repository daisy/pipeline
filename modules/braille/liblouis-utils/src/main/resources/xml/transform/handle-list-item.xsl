<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="xs css"
                version="2.0">
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[contains(string(@style), 'list-item')]">
        <xsl:variable name="display" as="xs:string"
            select="css:specified-properties('display', true(), true(), false(), .)/@value"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="$display='list-item'">
                <xsl:variable name="list-style-type" as="xs:string"
                    select="css:specified-properties('list-style-type', true(), true(), false(), .)/@value"/>
                <xsl:if test="$list-style-type!='none'">
                    <xsl:element name="css:marker">
                        <xsl:attribute name="style" select="'display:inline'"/>
                        <xsl:choose>
                            <xsl:when test="$list-style-type='decimal'">
                                <xsl:number value="count(preceding-sibling::*) + 1" format="1"/>
                                <xsl:text>. </xsl:text>
                            </xsl:when>
                            <xsl:when test="$list-style-type='lower-alpha'">
                                <xsl:number value="count(preceding-sibling::*) + 1" format="a"/>
                                <xsl:text>. </xsl:text>
                            </xsl:when>
                            <xsl:when test="$list-style-type='upper-alpha'">
                                <xsl:number value="count(preceding-sibling::*) + 1" format="A"/>
                                <xsl:text>. </xsl:text>
                            </xsl:when>
                            <xsl:when test="$list-style-type='lower-roman'">
                                <xsl:number value="count(preceding-sibling::*) + 1" format="i"/>
                                <xsl:text>. </xsl:text>
                            </xsl:when>
                            <xsl:when test="$list-style-type='upper-roman'">
                                <xsl:number value="count(preceding-sibling::*) + 1" format="I"/>
                                <xsl:text>. </xsl:text>
                            </xsl:when>
                            <xsl:when test="matches($list-style-type,re:exact($css:BRAILLE_STRING_RE))">
                                <xsl:value-of select="substring($list-style-type,2,string-length($list-style-type)-2)"/>
                                <xsl:text> </xsl:text>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:element>
                </xsl:if>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
