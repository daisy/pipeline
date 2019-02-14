<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl">
    
    <!--
        Import this (dummy) library whenever one of the following functions is used:
        * pf:file-exists
        * pf:file-expand83
    -->
    
    <!--
        TODO: delete uri-functions.xsl from catalog.xml
    -->
    <xsl:include href="uri-functions.xsl"/>
    
    <xd:doc>
        <xd:desc>
            <xd:p>Tests whether the file denoted by this path exists.</xd:p>
        </xd:desc>
        <xd:param name="path">
            <xd:p>An absolute file path (not a URI)</xd:p>
        </xd:param>
        <xd:return>
            <xd:p>A boolean</xd:p>
        </xd:return>
    </xd:doc>
    <!-- Implemented in Java -->
    <!--
    <xsl:function name="pf:file-exists" as="xs:boolean">
        <xsl:param name="path" as="xs:string" required="yes"/>
    </xsl:function>
    -->
    
</xsl:stylesheet>
