<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/">

    <xsl:param name="modified" select="format-dateTime(
        adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
        '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"/>

    <xsl:template match="text()"/>

    <xsl:template match="opf:metadata">
        <head>

            <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

            <title>
                <xsl:for-each select="dc:title[1]">
                    <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
                    <xsl:if test="@xml:lang">
                        <xsl:attribute name="lang" select="@xml:lang"/>
                    </xsl:if>
                    <xsl:copy-of select="normalize-space(.)"/>
                </xsl:for-each>
            </title>

            <meta name="viewport" content="width=device-width"/>

            <xsl:for-each select="*[not(self::opf:*) and not(self::dc:title)]">
                <meta name="{name()}" content="{normalize-space(.)}">
                    <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
                    <xsl:if test="@xml:lang">
                        <xsl:attribute name="lang" select="@xml:lang"/>
                    </xsl:if>
                </meta>
            </xsl:for-each>

            <xsl:for-each select="opf:meta[@property and not(@refines) and not(@property='dcterms:modified')]">
                <!-- NOTE on fidelity loss: meta elements that refine other meta elements are lost -->
                <!-- NOTE on fidelity loss: the @role attribute on meta elements are lost -->
                <meta name="{@property}" content="{normalize-space(.)}">
                    <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
                    <xsl:if test="@xml:lang">
                        <xsl:attribute name="lang" select="@xml:lang"/>
                    </xsl:if>
                </meta>
            </xsl:for-each>

            <meta name="dcterms:modified" content="{$modified}"/>
        </head>
    </xsl:template>

</xsl:stylesheet>
