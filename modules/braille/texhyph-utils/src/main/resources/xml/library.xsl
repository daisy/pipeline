<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tex="http://code.google.com/p/texhyphj/"
                xmlns:java="implemented-in-java">

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Hyphenate a text string using texhyphj.</p>
        </desc>
    </doc>
    <java:function name="tex:hyphenate" as="xs:string">
        <xsl:param name="table" as="xs:string"/>
        <xsl:param name="text" as="xs:string"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/tex/saxon/impl/HyphenateDefinition.java
        -->
    </java:function>

</xsl:stylesheet>
