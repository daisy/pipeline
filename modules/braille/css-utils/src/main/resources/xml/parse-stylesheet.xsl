<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@style">
        <xsl:variable name="style" as="element()*" select="css:parse-stylesheet(.)"/> <!-- css:rule*-->
        <xsl:variable name="extract-styles" as="element()*"
                      select="$style/self::css:rule[@selector[not(matches(.,'^&amp;::table-by\(.+\)$') or
                                                                  .=('&amp;::list-item',
                                                                     '&amp;::list-header'))]]"/> <!-- css:rule*-->
        <xsl:if test="exists($style except $extract-styles)">
            <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($style except $extract-styles))"/>
        </xsl:if>
        <xsl:apply-templates select="$extract-styles"/>
    </xsl:template>
    
    <xsl:template match="css:rule">
        <xsl:attribute name="css:{replace(replace(replace(@selector, '^(@|&amp;::|&amp;:)|\)' , ''  ),
                                                                     '^-'                     , '_' ),
                                                                     ' +|\('                  , '-' )}"
                       select="@style"/>
    </xsl:template>
    
</xsl:stylesheet>
