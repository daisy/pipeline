<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/pipeline/data"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:d="http://www.daisy.org/z3986/2005/dtbook/" xmlns:s="http://www.w3.org/2001/SMIL20/"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="audio-base"/>

    <xsl:key name="smil-fragments" match="s:par|s:text" use="@id"/>

    <xsl:template name="create-map">
        <audio-clips>
            <xsl:for-each select="collection()[/d:dtbook]">
                <xsl:apply-templates>
                    <xsl:with-param name="id-prefix"
                        select="if (last()>1) then concat(position(),'_') else ''" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </audio-clips>
    </xsl:template>

    <xsl:template match="d:*[@smilref]">
        <xsl:param name="id-prefix" tunnel="yes"/>
        <xsl:variable name="smil-uri"
            select="resolve-uri(substring-before(@smilref,'#'),base-uri(.))"/>
        <xsl:variable name="smil-id" select="substring-after(@smilref,'#')"/>
        <xsl:apply-templates
            select="key('smil-fragments',$smil-id,collection()[base-uri(/*)=$smil-uri])">
            <xsl:with-param name="idref" select="concat($id-prefix,@id)" tunnel="yes"/>
        </xsl:apply-templates>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="s:text">
        <!--forward to the parent 'par' template-->
        <xsl:apply-templates select=".."/>
    </xsl:template>

    <xsl:template match="s:par">
        <xsl:param name="idref" tunnel="yes"/>
        <xsl:variable name="audios" select="descendant::s:audio"/>

        <xsl:if test="count(distinct-values($audios/@src))>1">
            <!--TODO support audio merge-->
            <xsl:message>WARNING: the audio for the fragment <xsl:sequence
                    select="(@src|s:text/@src)[1]"/> spans over multiple files.</xsl:message>
        </xsl:if>

        <xsl:if test="count($audios)>0">
            <!--TODO normalize clock values-->
            <clip idref="{$idref}"
                src="{pf:relativize-uri(resolve-uri($audios[1]/@src,base-uri(.)),$audio-base)}"
                clipBegin="{$audios[1]/@clipBegin}"
                clipEnd="{$audios[@src=$audios[1]/@src][last()]/@clipEnd}"/>
        </xsl:if>

    </xsl:template>

    <xsl:template match="text()|@*"/>



</xsl:stylesheet>
