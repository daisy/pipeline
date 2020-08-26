<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="/d:fileset">
        <xsl:variable name="updated-files" as="element(d:file)*">
            <xsl:apply-templates mode="absolute-hrefs" select="collection()[2]/*/d:file"/>
        </xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()">
                <xsl:with-param name="updated-files" tunnel="yes" select="$updated-files"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/d:file">
        <xsl:param name="updated-files" as="element(d:file)*" tunnel="yes" required="yes"/>
        <xsl:variable name="href" as="xs:string" select="pf:normalize-uri(resolve-uri(@href,base-uri(.)))"/>
        <xsl:variable name="updated-file" as="element(d:file)?" select="$updated-files[@href=$href][1]"/>
        <xsl:choose>
            <xsl:when test="exists($updated-file)">
                <xsl:copy>
                    <xsl:sequence select="@xml:base|@href"/>
                    <xsl:sequence select="$updated-file/(@* except (@xml:base|@href))"/>
                    <xsl:sequence select="$updated-file/node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="absolute-hrefs"
                  match="d:file/@href|
                         d:file/@original-href">
        <xsl:attribute name="{name()}" select="pf:normalize-uri(resolve-uri(.,base-uri(..)))"/>
    </xsl:template>
    
    <xsl:template mode="#default absolute-hrefs" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
