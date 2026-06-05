<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="/d:fileset">
        <xsl:next-match>
            <xsl:with-param name="base" tunnel="yes" select="base-uri(.)"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="d:file[@original-href]/@href">
        <xsl:param name="base" tunnel="yes" required="yes"/>
        <xsl:attribute name="{name()}" select="pf:relativize-uri(../@original-href,$base)"/>
    </xsl:template>

    <xsl:template match="d:file/@original-href">
        <xsl:param name="base" tunnel="yes" required="yes"/>
        <xsl:attribute name="{name()}" select="resolve-uri(../@href,$base)"/>
    </xsl:template>

    <xsl:template match="d:anchor[@original-id]/@id">
        <xsl:attribute name="{name()}" select="../@original-id"/>
    </xsl:template>

    <xsl:template match="d:anchor/@original-id">
        <xsl:attribute name="{name()}" select="../@id"/>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
