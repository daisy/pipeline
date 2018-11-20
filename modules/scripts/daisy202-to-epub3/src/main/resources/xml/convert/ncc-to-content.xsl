<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xs"
    version="2.0">

    <!-- Generates an EPUB Content Document based on a DAISY 2.02 NCC
    (filtered from a list of IDs) -->
    
    <xsl:strip-space elements="*"/>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:param name="ids" required="yes"/>
    <xsl:param name="base" required="yes"/>

    <xsl:variable name="ids-seq" select="tokenize($ids,'\s+')"/>

    <xsl:template match="html">
        <html>
            <xsl:attribute name="xml:base" select="$base"/>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="head">
        <xsl:copy>
            <xsl:copy-of
                select="@*|meta[@http-equiv]|meta[lower-case(@name)='dc:identifier']|meta[lower-case(@name)='dc:language']"
            />
        </xsl:copy>
    </xsl:template>

    <!--only copy NCC elements of the wanted IDs-->
    <xsl:template match="body/*"/>
    <xsl:template match="body/*[not(@id) and */@id=$ids-seq]">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="body//*[@id=$ids-seq]" priority="2">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>
