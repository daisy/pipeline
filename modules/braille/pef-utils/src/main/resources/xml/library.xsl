<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:java="implemented-in-java">

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Encode a (Unicode) braille string using the specified character set.</p>
        </desc>
    </doc>
    <java:function name="pef:encode" as="xs:string">
        <xsl:param name="table" as="xs:string"/>
        <xsl:param name="braille" as="xs:string"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/pef/saxon/impl/EncodeDefinition.java
        -->
    </java:function>

    <!--
        pef:decode
    -->
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Decode a braille string in the specified character set (to Unicode braille).</p>
        </desc>
    </doc>
    <java:function name="pef:decode" as="xs:string">
        <xsl:param name="table" as="xs:string"/>
        <xsl:param name="text" as="xs:string"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/pef/saxon/impl/DecodeDefinition.java
        -->
    </java:function>

</xsl:stylesheet>
