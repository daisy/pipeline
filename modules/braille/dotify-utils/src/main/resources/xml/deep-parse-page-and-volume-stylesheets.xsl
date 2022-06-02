<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <xsl:template name="main">
        <_>
            <xsl:for-each-group select="collection()//@css:volume" group-by="string(.)">
                <css:rule selector="@volume" serialized="{current-grouping-key()}">
                    <xsl:sequence select="css:parse-stylesheet(current())/*"/>
                </css:rule>
            </xsl:for-each-group>
            <xsl:for-each-group select="collection()//@css:page" group-by="string(.)">
                <css:rule selector="@page" serialized="{current-grouping-key()}">
                    <xsl:sequence select="css:parse-stylesheet(current())/*"/>
                </css:rule>
            </xsl:for-each-group>
        </_>
    </xsl:template>
    
</xsl:stylesheet>
