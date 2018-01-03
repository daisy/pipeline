<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <xsl:template name="main">
        <_>
            <xsl:for-each select="distinct-values(collection()//@css:volume)">
                <css:rule selector="@volume" style="{.}">
                    <xsl:sequence select="css:deep-parse-stylesheet(.)"/>
                </css:rule>
            </xsl:for-each>
            <xsl:for-each select="distinct-values(collection()//@css:page)">
                <css:rule selector="@page" style="{.}">
                    <xsl:sequence select="css:deep-parse-stylesheet(.)"/>
                </css:rule>
            </xsl:for-each>
        </_>
    </xsl:template>
    
</xsl:stylesheet>
