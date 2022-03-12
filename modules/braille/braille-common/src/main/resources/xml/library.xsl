<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">

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
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
    </java:function>
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
        <xsl:param name="style" as="xs:string*"/>
    </java:function>
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
        <xsl:param name="style" as="xs:string*"/>
        <xsl:param name="lang" as="xs:string*"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java
        -->
    </java:function>

    <!--
        FIXME: delete when major version is updated
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

</xsl:stylesheet>
