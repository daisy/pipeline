<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">
    
    <!--
        FIXME: delete uri-functions.xsl from catalog.xml
    -->
    <xsl:include href="uri-functions.xsl"/>
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Tests whether the file denoted by this path exists on disk. Also works for zipped
            files.</p>
        </desc>
        <param name="uri">
            <p>An absolute file URI</p>
        </param>
        <return>
            <p>A boolean</p>
        </return>
    </doc>
    <java:function name="pf:file-exists" as="xs:boolean">
        <xsl:param name="path" as="xs:string"/>
        <!--
            Implemented in ../../../java/org/daisy/pipeline/file/saxon/impl/FileExists.java
        -->
    </java:function>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Expand 8.3 encoded path segments in a file URI.</p>
        </desc>
        <param name="uri">
            <p>An absolute file URI</p>
        </param>
        <return>
            <p>An absolute file URI</p>
        </return>
    </doc>
    <java:function name="pf:file-expand83" as="xs:string">
        <xsl:param name="uri" as="xs:string"/>
        <!--
            Implemented in ../../../java/org/daisy/pipeline/file/saxon/impl/Expand83.java
        -->
    </java:function>

</xsl:stylesheet>
