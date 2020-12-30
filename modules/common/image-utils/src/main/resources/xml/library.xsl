<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Get the dimensions of an image file.</p>
        </desc>
        <param name="uri">
            <p>An absolute file URI</p>
        </param>
        <return>
            <p>A sequence of two integers: the width and the height in pixels.</p>
        </return>
    </doc>
    <java:function name="pf:image-dimensions" as="xs:integer*">
        <xsl:param name="image" as="xs:string"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/image/saxon/impl/ImageDimensions.java
        -->
    </java:function>

</xsl:stylesheet>
