<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:hyphen="http://hunspell.sourceforge.net/Hyphen"
                xmlns:java="implemented-in-java">
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Hyphenate a text string using Hyphen.</p>
        </desc>
    </doc>
    <java:function name="hyphen:hyphenate" as="xs:string">
        <xsl:param name="table" as="xs:string"/>
        <xsl:param name="text" as="xs:string"/>
        <!--
            Implemented in ../../org/daisy/pipeline/braille/libhyphen/saxon/impl/HyphenateDefinition.java
        -->
    </java:function>
    
</xsl:stylesheet>
