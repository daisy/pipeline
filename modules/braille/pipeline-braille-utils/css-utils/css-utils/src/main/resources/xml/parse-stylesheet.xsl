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
        <xsl:apply-templates select="css:parse-stylesheet(.)"/>
    </xsl:template>
    
    <xsl:template match="css:rule[not(@selector)]">
        <xsl:sequence select="css:style-attribute(@style)"/>
    </xsl:template>
    
    <xsl:template match="css:rule">
        <xsl:attribute name="css:{replace(replace(replace(@selector, '^(@|::|:)' , ''  ),
                                                                     '^-'        , '_' ),
                                                                     ' +'        , '-' )}"
                       select="@style"/>
    </xsl:template>
    
</xsl:stylesheet>
