<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:include href="source-of-pagination.xsl"/>

    <xsl:template match="/*">
        <xsl:choose>
            <xsl:when test="dtb:head">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <metadata>
                    <xsl:if test="@xml:lang">
                        <dc:language id="language-1"><xsl:value-of select="@xml:lang"/></dc:language>
                    </xsl:if>
                </metadata>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="//dtb:head">
        <metadata>
            <xsl:if test="//dtb:pagenum">
                <xsl:attribute name="prefix" select="'a11y: http://www.idpf.org/epub/vocab/package/a11y/#'"/>
            </xsl:if>
            <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
            <xsl:for-each select="dtb:meta">
                <xsl:choose>
                    <xsl:when
                        test="starts-with(@name,'dc:') and tokenize(@name,':')[last()] = ('Contributor','Coverage','Creator','Date','Description','Format','Identifier','Language','Publisher','Relation','Rights','Source','Subject','Title','Type')">
                        <xsl:element name="{lower-case(@name)}">
                            <xsl:attribute name="id"
                                select="concat(lower-case(tokenize(@name,':')[last()]),'-',count(preceding-sibling::*[lower-case(@name) = lower-case(current()/@name)]) + 1)"/>
                            <xsl:value-of select="@content"/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <meta property="{@name}">
                            <xsl:value-of select="@content"/>
                        </meta>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <!-- Language -->
            <xsl:if test="/*/@xml:lang and not(dtb:meta[lower-case(@name)='dc:language'])">
                <dc:language id="language-1">
                    <xsl:value-of select="/*/@xml:lang"/>
                </dc:language>
            </xsl:if>
            <!-- Source of pagination
                 - http://kb.daisy.org/publishing/docs/navigation/pagesrc.html
                 - https://www.w3.org/publishing/a11y/page-source-id/
            -->
            <xsl:if test="//dtb:pagenum and not(dtb:meta[lower-case(@name)=('a11y:pageBreakSource','pageBreakSource')])">
                <xsl:variable name="source-of-pagination" as="xs:string?" select="pf:dtbook-source-of-pagination(/)"/>
                <xsl:if test="exists($source-of-pagination)">
                    <meta property="a11y:pageBreakSource">
                        <xsl:value-of select="$source-of-pagination"/>
                    </meta>
                </xsl:if>
            </xsl:if>
        </metadata>
    </xsl:template>
    
    <!-- identity template which discards everything -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

</xsl:stylesheet>
