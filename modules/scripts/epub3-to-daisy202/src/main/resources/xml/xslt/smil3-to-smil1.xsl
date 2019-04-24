<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <!--
        TODO:
          - update metadata according to SMIL 1.0
    -->

    <xsl:template match="@*|node()[not(self::*)]">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="*[not(namespace-uri()='http://www.w3.org/ns/SMIL')]"/>
    <xsl:template match="@*[not(namespace-uri()=('','http://www.w3.org/ns/SMIL'))]"/>
    
    <xsl:template match="@clipEnd">
        <xsl:attribute name="clip-end" select="."/>
    </xsl:template>
    
    <xsl:template match="@clipBegin">
        <xsl:attribute name="clip-begin" select="."/>
    </xsl:template>

</xsl:stylesheet>
