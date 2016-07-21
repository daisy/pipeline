<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" version="2.0" exclude-result-prefixes="#all">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="//dtb:head">
        <metadata prefix="dc: http://purl.org/dc/elements/1.1/">
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
        </metadata>
    </xsl:template>

    <!-- identity template which discards everything -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

</xsl:stylesheet>
