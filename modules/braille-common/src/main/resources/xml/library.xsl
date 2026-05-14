<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <xsl:import href="encoding-functions.xsl">
        <!--
            * brl:unicode-braille-to-escape-sequence
            * brl:unicode-braille-to-nabcc
            * brl:nabcc-to-unicode-braille
        -->
    </xsl:import>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Apply a text transformer to a string sequence.</p>
        </desc>
    </doc>
    <xsl:function name="pf:text-transform" as="item()*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="styled-text" as="item()*"/>
        <xsl:sequence select="TextTransform:transform($query,$styled-text)"
                      xmlns:TextTransform="org.daisy.pipeline.braille.common.saxon.impl.TextTransformDefinition$TextTransform">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Apply an XML transformer to a node sequence.</p>
        </desc>
    </doc>
    <xsl:function name="pf:transform" as="item()*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="source" as="node()*"/>
        <xsl:sequence select="Transform:transform($query,$source)"
                      xmlns:Transform="org.daisy.pipeline.braille.common.saxon.impl.TransformDefinition$Transform">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/TransformDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Get braille code from language tag.</p>
        </desc>
    </doc>
    <xsl:function name="pf:braille-code-from-language-tag" as="item()">
        <xsl:param name="language" as="xs:string"/>
        <xsl:sequence select="BrailleCode:fromLanguageTag($language)"
                      xmlns:BrailleCode="org.daisy.pipeline.braille.common.saxon.impl.BrailleCodeFunctionProvider$BrailleCodeFunctions">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/BrailleCodeFunctionProvider.java
            -->
        </xsl:sequence>
    </xsl:function>

    <xsl:function name="pf:get-braille-code-info" as="map(xs:string,xs:string)">
        <xsl:param name="code" as="item()"/> <!-- xs:string | item() -->
        <xsl:sequence select="BrailleCode:getImplementationInfo($code)"
                      xmlns:BrailleCode="org.daisy.pipeline.braille.common.saxon.impl.BrailleCodeFunctionProvider$BrailleCodeFunctions">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/BrailleCodeFunctionProvider.java
            -->
        </xsl:sequence>
    </xsl:function>

</xsl:stylesheet>
