<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!--
        Anticipate bugs/shortcomings in Dotify's white space processing.
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
                                          intersect preceding::node()[not(self::obfl:marker or
                                                                          self::obfl:span[normalize-space(.)=''] or
                                                                          self::text()[normalize-space(.)=''])])]">
            <xsl:sequence select="replace(., '^\s+', '')"/>
        </xsl:template>
    -->
    
    <xsl:template match="obfl:block | obfl:td">
        <xsl:apply-templates select="." mode="strip-leading-space"/>
    </xsl:template>
    
    <xsl:template match="*" mode="strip-leading-space">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:variable name="normalization-point" select="(node()[descendant-or-self::*[not(self::obfl:marker or self::obfl:span[normalize-space(.)=''])] | descendant-or-self::text()[normalize-space()!='']])[1]"/>
            <xsl:choose>
                <xsl:when test="count($normalization-point) = 0">
                    <xsl:apply-templates select="node()" mode="strip-leading-space"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$normalization-point/preceding-sibling::node()" mode="strip"/>
                    <xsl:apply-templates select="$normalization-point" mode="strip-leading-space"/>
                    <xsl:apply-templates select="$normalization-point/following-sibling::node()" mode="#default"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*" mode="strip">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()" mode="strip"/>
    
    <xsl:template match="text()" mode="strip-leading-space">
        <xsl:value-of select="replace(., '^\s+', '')"/>
    </xsl:template>
    
    <!--
        Strip white space after br elements.
    -->
    <xsl:template mode="#default strip-leading-space"
                  priority=".6"
                  match="text()[matches(.,'^\s')
                                and preceding-sibling::node()[not(self::text() and normalize-space(.)='' or self::obfl:marker)][1]/self::obfl:br]">
        <xsl:sequence select="replace(., '^\s+', '')"/>
    </xsl:template>
    
    <!--
        Move white space in front of marker elements. Also append a NBSP because otherwise Dotify
        would move the space after the marker element again.
    -->
    <xsl:template match="text()[matches(.,'^\s') and preceding-sibling::node()[1]/self::obfl:marker]">
        <xsl:sequence select="replace(., '^\s+', '')"/>
    </xsl:template>
    
    <xsl:template mode="#default strip-leading-space"
                  match="text()[not(normalize-space()='')
                                and following-sibling::*[1]
                                    /self::obfl:marker
                                    /following-sibling::node()[not(self::text()[normalize-space()=''] or self::obfl:marker)][1]
                                    /self::text()[matches(.,'^\s')]]">
        <xsl:next-match/>
        <xsl:text> &#x200B;</xsl:text>
    </xsl:template>
    
    <!--
        Prevent white space at the begin or end of a span from being stripped by Dotify.
    -->
    
    <xsl:template match="obfl:span/text()" mode="#default strip-leading-space">
        <xsl:if test="not(preceding-sibling::node()) and matches(.,'^\s')
                      and not(parent::*/preceding-sibling::node()[1]/self::text()[matches(., '\s$')])">
            <xsl:text>&#x200B;</xsl:text>
        </xsl:if>
        <xsl:next-match/>
        <xsl:if test="not(following-sibling::node()) and matches(.,'\s$')
                      and not(parent::*/following-sibling::node()[1]/self::text()[matches(., '^\s')])">
            <xsl:text>&#x200B;</xsl:text>
        </xsl:if>
    </xsl:template>
    
    <!--
        Remove comments because when a comment is surrounded by white space, Dotify removes the white space.
    -->
    <xsl:template match="comment()"/>
    
</xsl:stylesheet>
