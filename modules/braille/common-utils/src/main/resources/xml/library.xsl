<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    
    <!--
        Import this library whenever one of the following functions is used:
        * pf:text-transform
        * brl:unicode-braille-to-escape-sequence
        * brl:unicode-braille-to-nabcc
        * brl:nabcc-to-unicode-braille
    -->
    
    <xsl:import href="encoding-functions.xsl"/>
    
    <!--
        pf:transform is implemented in Java (org.daisy.pipeline.braille.common.saxon.impl.TextTransformDefinition)
    -->
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Query a text transformer and apply it to a string.</p>
        </desc>
    </doc>
    
</xsl:stylesheet>
