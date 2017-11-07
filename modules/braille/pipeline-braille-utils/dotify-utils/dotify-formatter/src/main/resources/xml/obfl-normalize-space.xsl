<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!--
        Anticipate bugs in Dotify's white space normalization.
    -->
    
    <xsl:template match="@*|node()">
       <xsl:copy>
           <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>
    
    <!--
        Strip white space from the beginning of text nodes in a block where none of the preceding
        nodes in the block is something else than a marker or empty text node.
        
        Equivalent oneliner:
        
        <xsl:template match="*[self::obfl:block or self::obfl:td]
                             //text()[not((ancestor::obfl:block|ancestor::obfl:td)[last()]//node()
                                          intersect preceding::node()[not(self::obfl:marker or self::text()[normalize-space(.)=''])])]">
            <xsl:sequence select="replace(., '^\s+', '')"/>
        </xsl:template>
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
    
    <xsl:template match="@*" mode="normalize">
       <xsl:sequence select="."/>
    </xsl:template>
    
    <!--
        Prevent white space at the begin or end of a span from being stripped by Dotify.
    -->
    
    <xsl:template match="obfl:span/text()" mode="#default normalize">
        <xsl:if test="not(preceding-sibling::node()) and matches(.,'^\s')">
            <xsl:text>&#x200B;</xsl:text>
        </xsl:if>
        <xsl:next-match/>
        <xsl:if test="not(following-sibling::node()) and matches(.,'\s$')">
            <xsl:text>&#x200B;</xsl:text>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
