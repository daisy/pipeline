<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()" mode="#all">
       <xsl:copy>
           <xsl:apply-templates select="@*|node()" mode="#current"/>
       </xsl:copy>
    </xsl:template>
    
    <!--
        Anticipate a bug in Dotify's white space normalization:
        
        Whitespace is stripped from the beginning of text nodes in a block where one of
        the preceding nodes in the block is not a marker or not an empty text node.
    -->
    
    <xsl:template match="obfl:block | obfl:td">
        <xsl:apply-templates select="." mode="normalize"/>
    </xsl:template>
    
    <xsl:template match="*" mode="normalize">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="normalization-point" select="(node()[descendant-or-self::*[not(self::obfl:marker)] | descendant-or-self::text()[normalize-space()!='']])[1]"/>
            <xsl:choose>
                <xsl:when test="count($normalization-point) = 0">
                    <xsl:apply-templates select="node()" mode="normalize"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$normalization-point/(preceding-sibling::node() | .)" mode="normalize"/>
                    <xsl:apply-templates select="$normalization-point/following-sibling::node()" mode="#default"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()" mode="normalize">
        <xsl:value-of select="replace(., '^\s+', '')"/>
    </xsl:template>
    
</xsl:stylesheet>
