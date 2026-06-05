<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@style">
        <xsl:variable name="style" as="item()?" select="css:parse-stylesheet(.)"/>
        <xsl:if test="$style">
            <xsl:variable name="extract-style" as="item()*"
                          select="s:iterate($style)[s:selector(.)[not(matches(.,'^&amp;::table-by\(.+\)$'))
                                                                  and not(.=('&amp;::list-item',
                                                                             '&amp;::list-header'))]]"/>
            <xsl:choose>
                <xsl:when test="empty($extract-style)">
                    <xsl:sequence select="css:style-attribute($style)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="$extract-style">
                        <xsl:variable name="selector" as="xs:string" select="s:selector(.)"/>
                        <xsl:attribute name="css:{replace(replace(replace($selector, '^(@|&amp;::|&amp;:)|\)' , ''  ),
                                                                                     '^-'                     , '_' ),
                                                                                     ' +|\('                  , '-' )}"
                                       select="string(s:get(.,$selector))"/>
                    </xsl:for-each>
                    <xsl:variable name="rest-style" as="item()*"
                                  select="s:iterate($style)[s:property(.) or
                                                            s:selector(.)[matches(.,'^&amp;::table-by\(.+\)$') or
                                                                          .=('&amp;::list-item',
                                                                             '&amp;::list-header')]]"/>
                    <xsl:if test="exists($rest-style)">
                        <xsl:sequence select="css:style-attribute($rest-style)"/>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
