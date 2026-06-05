<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:param name="compatibility-mode" required="yes"/>

    <xsl:variable name="modified" select="format-dateTime(
                                            adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                                            '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>

    <xsl:template match="metadata">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="not(meta[@name='dcterms:modified']|
                                    dc:date[@opf:event='modification'])">
                    <xsl:variable name="metadata" as="element()">
                        <metadata>
                            <dc:date opf:event="modification">
                                <xsl:value-of select="$modified"/>
                            </dc:date>
                            <xsl:sequence select="node()"/>
                        </metadata>
                    </xsl:variable>
                    <xsl:apply-templates select="$metadata/*"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="meta[@name]" priority="0.6">
        <xsl:if test="contains(@name,':')">
            <xsl:if test="not(../meta[@property=current()/@name])">
                <xsl:next-match/>
            </xsl:if>
            <xsl:if test="$compatibility-mode='true'">
                <xsl:apply-templates mode="compatibility-mode" select="."/>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="meta[@name]">
        <meta property="{@name}">
            <xsl:sequence select="@scheme"/>
            <xsl:value-of select="@content"/>
        </meta>
    </xsl:template>

    <xsl:template match="meta[@name='dcterms:modified']">
        <meta property="{@name}">
            <xsl:sequence select="@scheme"/>
            <xsl:value-of select="$modified"/>
        </meta>
    </xsl:template>

    <xsl:template match="meta[@property]" priority="0.6">
        <xsl:if test="../meta[@name=current()/@property]">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="meta[@property='dcterms:modified']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:value-of select="$modified"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dc:date[@opf:event='modification']">
        <meta property="dcterms:modified">
            <xsl:value-of select="$modified"/>
        </meta>
        <xsl:if test="$compatibility-mode='true'">
            <xsl:apply-templates mode="compatibility-mode" select="."/>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="compatibility-mode" match="meta[@name='dcterms:modified']/@content">
        <xsl:attribute name="{name(.)}" select="$modified"/>
    </xsl:template>

    <xsl:template mode="compatibility-mode" match="dc:date[@opf:event='modification']">
        <meta name="dcterms:modified" content="{$modified}"/>
    </xsl:template>

    <xsl:template mode="#default compatibility-mode" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
