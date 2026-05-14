<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Parse page and volume style sheets contained in the input documents.</p>
            <p>Returns a sequence of parsed style sheets.</p>
        </desc>
    </doc>
    <xsl:function name="f:deep-parse-page-and-volume-stylesheets" as="item()*">
        <xsl:param name="collection" as="document-node()*"/>
        <xsl:for-each-group select="$collection//@css:volume" group-by="string(.)">
            <xsl:sequence select="css:parse-stylesheet(current())"/>
        </xsl:for-each-group>
        <xsl:for-each-group select="$collection//@css:page" group-by="string(.)">
            <xsl:sequence select="css:parse-stylesheet(current())"/>
        </xsl:for-each-group>
    </xsl:function>
    
</xsl:stylesheet>
