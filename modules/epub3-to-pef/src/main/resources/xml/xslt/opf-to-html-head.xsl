<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns="http://www.w3.org/1999/xhtml">

    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="/*">
        <head>
            <xsl:apply-templates select="opf:metadata/*"/>
        </head>
    </xsl:template>

    <xsl:template match="dc:title[not(@refines) and not(dc:title[not(@refines)])]">
        <title>
            <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
            <xsl:if test="@xml:lang">
                <xsl:attribute name="lang" select="@xml:lang"/>
            </xsl:if>
            <xsl:copy-of select="normalize-space(.)"/>
        </title>
    </xsl:template>

    <xsl:template match="dc:creator[not(@refines) and not(dc:creator[not(@refines)])]">
        <creator>
            <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
            <xsl:if test="@xml:lang">
                <xsl:attribute name="lang" select="@xml:lang"/>
            </xsl:if>
            <xsl:copy-of select="normalize-space(.)"/>
        </creator>
    </xsl:template>

    <xsl:template match="*[not(self::opf:*) and not(self::dc:title) and not(self::dc:creator)]">
        <meta name="{name()}" content="{normalize-space(.)}">
            <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
            <xsl:if test="@xml:lang">
                <xsl:attribute name="lang" select="@xml:lang"/>
            </xsl:if>
        </meta>
    </xsl:template>

    <xsl:template match="opf:meta[@property and not(@refines)]">
        <!-- NOTE on fidelity loss: meta elements that refine other meta elements are lost -->
        <!-- NOTE on fidelity loss: the @role attribute on meta elements are lost -->
        <meta name="{@property}" content="{normalize-space(.)}">
            <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
            <xsl:if test="@xml:lang">
                <xsl:attribute name="lang" select="@xml:lang"/>
            </xsl:if>
        </meta>
    </xsl:template>

</xsl:stylesheet>
