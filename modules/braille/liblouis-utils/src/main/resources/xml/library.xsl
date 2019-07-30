<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    
    <!--
        Import this library whenever one of the following functions is used:
        * louis:translate
        * louis:hyphenate
    -->
    
    <!--
        louis:translate is implemented in Java (org.daisy.pipeline.braille.liblouis.saxon.impl.TranslateDefinition)
    -->
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Translate a text string to Braille using liblouis.</p>
        </desc>
    </doc>
    
    <!--
        louis:hyphenate is implemented in Java (org.daisy.pipeline.braille.liblouis.saxon.impl.HyphenateDefinition)
    -->
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Hyphenate a text string using liblouis.</p>
        </desc>
    </doc>
    
</xsl:stylesheet>
