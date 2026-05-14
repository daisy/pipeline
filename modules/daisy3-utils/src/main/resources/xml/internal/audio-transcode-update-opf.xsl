<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://openebook.org/namespaces/oeb-package/1.0/"
                xpath-default-namespace="http://openebook.org/namespaces/oeb-package/1.0/"
                exclude-result-prefixes="#all">

    <xsl:param name="new-audio-file-type" as="xs:string" required="yes"/>
    <xsl:param name="mapping" as="element(d:fileset)" required="yes">
        <!--
            mapping of px:audio-transcode step
        -->
    </xsl:param>

    <xsl:variable name="mapping-with-absolute-hrefs" as="element(d:fileset)">
        <xsl:apply-templates mode="absolute-hrefs" select="$mapping"/>
    </xsl:variable>

    <xsl:variable name="audio-formats" as="map(xs:string,xs:string)">
        <xsl:map>
            <xsl:map-entry key="'audio/mpeg4-generic'" select="'mp4-aac'"/>
            <xsl:map-entry key="'audio/mpeg'"          select="'mp3'"/>
            <xsl:map-entry key="'audio/mp3'"           select="'mp3'"/>
            <xsl:map-entry key="'audio/x-wav'"         select="'wav'"/>
            <xsl:map-entry key="'audio/wav'"           select="'wav'"/>
            <xsl:map-entry key="'audio/wave'"          select="'wav'"/>
            <xsl:map-entry key="'audio/vnd.wave'"      select="'wav'"/>
        </xsl:map>
    </xsl:variable>

    <!-- update dtb:audioFormat metadata -->
    <xsl:template match="/package/metadata/x-metadata/meta[@name='dtb:audioFormat' and not(@content=('mp4-aac','mp3','wav'))]"/>
    <xsl:template match="/package/metadata/x-metadata">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
            <xsl:if test="$audio-formats($new-audio-file-type)">
                <xsl:variable name="format" as="xs:string" select="$audio-formats($new-audio-file-type)"/>
                <xsl:if test="not(/package/metadata/x-metadata/meta[@name='dtb:audioFormat' and @content=$format])">
                    <meta name="dtb:audioFormat" content="{$format}"/>
                </xsl:if>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- update media-type of manifest items -->
    <xsl:template match="/package/manifest/item[resolve-uri(@href,base-uri(.))=$mapping-with-absolute-hrefs/d:file/@href]">
        <xsl:copy>
            <xsl:apply-templates select="@* except @media-type"/>
            <xsl:attribute name="media-type" select="$new-audio-file-type"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="absolute-hrefs"
                  match="d:file/@href">
        <xsl:attribute name="{name()}" select="resolve-uri(.,base-uri(..))"/>
    </xsl:template>

    <xsl:template mode="#default absolute-hrefs" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
