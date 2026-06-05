<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template name="main">
        <!-- because collection()/* can apparently change the order (bug?) -->
        <xsl:variable name="filesets" as="element(d:fileset)*">
            <xsl:apply-templates mode="sequence" select="collection()"/>
        </xsl:variable>
        <xsl:call-template name="join">
            <xsl:with-param name="filesets" select="$filesets"/>
        </xsl:call-template>
    </xsl:template>

    <!-- for XSpec tests -->
    <xsl:template name="join">
        <xsl:param name="filesets" as="element(d:fileset)*"/>

        <!-- Joint fileset base: longest common URI of all fileset bases -->
        <xsl:variable name="base" select="pf:longest-common-uri(distinct-values($filesets[@xml:base]/pf:normalize-uri(@xml:base)))" as="xs:string"/>

        <xsl:variable name="files" as="element(d:file)*">
            <xsl:apply-templates mode="absolute-hrefs" select="$filesets/d:file"/>
        </xsl:variable>

        <d:fileset>
            <xsl:if test="$base">
                <xsl:attribute name="xml:base" select="$base"/>
            </xsl:if>
            <xsl:for-each-group select="$files" group-by="@href">
                <xsl:variable name="href" select="current-grouping-key()"/>
                <d:file href="{if ($base) then pf:relativize-uri($href, $base) else $href}">
                    <!-- last occurence of an attribute wins -->
                    <xsl:apply-templates select="current-group()/(@* except @href)"/>
                    <xsl:apply-templates select="current-group()/*"/>
                </d:file>
            </xsl:for-each-group>
        </d:fileset>
    </xsl:template>

    <xsl:template match="d:file/@xml:base"/>

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

    <xsl:template mode="sequence" match="/">
        <xsl:copy-of select="/*"/>
    </xsl:template>

</xsl:stylesheet>
