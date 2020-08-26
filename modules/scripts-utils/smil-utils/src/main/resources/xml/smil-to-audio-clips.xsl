<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:mo="http://www.w3.org/ns/SMIL"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="output-base-uri"/>

    <xsl:template name="create-map">
        <audio-clips>
            <xsl:apply-templates select="collection()/*"/>
        </audio-clips>
    </xsl:template>

    <xsl:template match="mo:text[@src]|
                         s:text[@src]|
                         text[@src]">
        <xsl:variable name="text" select="."/>
        <xsl:for-each select="parent::*[local-name()='par']">
            <xsl:variable name="audio" select="descendant::*[local-name()='audio']"/>
            <xsl:if test="exists($audio)">
              <xsl:if test="count(distinct-values($audio/@src))>1">
                <!-- FIXME: support audio merge -->
                <xsl:message>WARNING: the audio for the fragment <xsl:sequence
                select="(@src|*[local-name()='text']/@src)[1]"/> spans over multiple files.</xsl:message>
              </xsl:if>
              <!-- FIXME: normalize clock values -->
              <clip idref="{substring-after($text/@src,'#')}"
                    src="{pf:relativize-uri(resolve-uri($audio[1]/@src,base-uri(.)),$output-base-uri)}"
                    clipBegin="{$audio[1]/@clipBegin}"
                    clipEnd="{$audio[@src=$audio[1]/@src][last()]/@clipEnd}"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates select="*"/>
    </xsl:template>

</xsl:stylesheet>
