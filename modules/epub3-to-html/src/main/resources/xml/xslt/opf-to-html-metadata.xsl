<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:param name="modified"
               select="format-dateTime(
                         adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                         '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"/>

    <xsl:template match="text()"/>

    <xsl:template match="opf:metadata">
        <xsl:text>&#xa;</xsl:text>
        <head>
            <xsl:copy-of select="namespace::*[not(.=('http://www.w3.org/1999/xhtml',
                                                     'http://www.idpf.org/2007/opf'))]"/>
            <xsl:if test="count(ancestor-or-self::*/@prefix)">
                <xsl:attribute name="epub:prefix" select="string-join(ancestor-or-self::*/@prefix, ' ')"/>
            </xsl:if>
            <xsl:text>&#xa;    </xsl:text>
            <meta charset="UTF-8"/>
            <xsl:text>&#xa;    </xsl:text>
            <title>
                <xsl:copy-of select="dc:title[1]/(@scheme|
                                                  @http-equiv|
                                                  @xml:lang|
                                                  @dir|
                                                  namespace::*[not(.=('http://www.w3.org/1999/xhtml',
                                                                      'http://www.idpf.org/2007/opf'))]|
                                                  node())"/>
            </title>
            <xsl:text>&#xa;    </xsl:text>
            <meta name="dc:identifier" content="{dc:identifier}"/>
            <xsl:for-each select="*[not(self::opf:*) and not(self::dc:title) and not(self::dc:identifier)]">
                <xsl:text>&#xa;    </xsl:text>
                <meta name="{name()}" content="{normalize-space(.)}">
                    <xsl:copy-of select="@scheme|
                                         @http-equiv|
                                         @xml:lang|
                                         @dir|
                                         namespace::*[not(.=('http://www.w3.org/1999/xhtml',
                                                             'http://www.idpf.org/2007/opf'))]"/>
                </meta>
            </xsl:for-each>
            <xsl:for-each select="opf:meta[@property
                                           and not(@refines)
                                           and not(@property=('dcterms:modified'))]">
                <!-- NOTE on fidelity loss: meta elements that refine other meta elements are lost -->
                <!-- NOTE on fidelity loss: the @role attribute on meta elements are lost -->
                <xsl:text>&#xa;    </xsl:text>
                <meta name="{@property}" content="{normalize-space(.)}">
                    <xsl:copy-of select="@scheme|
                                         @http-equiv|
                                         @xml:lang|
                                         @dir|
                                         namespace::*[not(.=('http://www.w3.org/1999/xhtml',
                                                             'http://www.idpf.org/2007/opf'))]"/>
                </meta>
            </xsl:for-each>

            <xsl:text>&#xa;    </xsl:text>
            <meta name="dcterms:modified" content="{$modified}"/>
            <xsl:text>&#xa;</xsl:text>
        </head>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>

</xsl:stylesheet>
