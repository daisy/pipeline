<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:mo="http://www.w3.org/ns/SMIL"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="output-base-uri"/>

    <xsl:template name="create-map">
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <audio-clips>
                <xsl:apply-templates select="collection()/*"/>
            </audio-clips>
        </xsl:variable>
        <!--
            Remove duplicate clips (two clips could have the same textref if the SMILs contain two
            text references to the same element)
        -->
        <xsl:for-each select="$audio-clips">
            <xsl:copy>
                <xsl:for-each-group select="d:clip" group-by="@textref">
                    <xsl:variable name="clips" as="element(d:clip)*" select="current-group()"/>
                    <xsl:variable name="clip" as="element(d:clip)" select="$clips[1]"/>
                    <xsl:if test="$clips[not(@clipBegin=$clip/@clipBegin and @clipEnd=$clip/@clipEnd)]">
                        <xsl:message terminate="yes">
                            <xsl:text>SMILs contain two text references to the same element (</xsl:text>
                            <xsl:value-of select="$clip/@textref"/>
                            <xsl:text>) but with different clipBegin and/or clipEnd.</xsl:text>
                        </xsl:message>
                    </xsl:if>
                    <xsl:sequence select="$clip"/>
                </xsl:for-each-group>
            </xsl:copy>
        </xsl:for-each>
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
              <clip textref="{pf:relativize-uri($text/resolve-uri(@src,base-uri(.)),$output-base-uri)}"
                    src="{pf:relativize-uri($audio[1]/resolve-uri(@src,base-uri(.)),$output-base-uri)}"
                    clipBegin="{$audio[1]/@clipBegin}"
                    clipEnd="{$audio[@src=$audio[1]/@src][last()]/@clipEnd}"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates select="*"/>
    </xsl:template>

</xsl:stylesheet>
