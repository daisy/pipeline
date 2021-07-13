<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">
    <xsl:output encoding="UTF-8" method="xml" indent="yes"/>
    <xsl:include href="clock-functions.xsl"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/*">
        <smil xmlns="http://www.w3.org/ns/SMIL" version="3.0">
            <xsl:apply-templates/>
        </smil>
    </xsl:template>
    <xsl:template match="head">
        <metadata xmlns="http://www.w3.org/ns/SMIL">
            <xsl:variable name="metas" select="meta"/>
            <meta xmlns="http://www.w3.org/ns/SMIL" name="dc:format" content="EPUB3"/>
            <xsl:if test="not(meta/lower-case(@name)=('dtb:generator','ncc:generator'))">
                <meta xmlns="http://www.w3.org/ns/SMIL" name="dtb:generator" content="DAISY Pipeline 2"/>
            </xsl:if>
            <xsl:for-each select="meta[not(lower-case(@name)=('base','uid','dc:identifier','dc:format','dtb:totalelapsedtime','ncc:totalelapsedtime','total-elapsed-time','ncc:timeinthissmil','time-in-this-smil'))]">
                <meta xmlns="http://www.w3.org/ns/SMIL" name="{@name}" content="{@value}"/>
            </xsl:for-each>
            <xsl:copy-of select="*[not(self::meta)]"/>
        </metadata>
    </xsl:template>
    <xsl:template match="body">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="seq[parent::body]">
        <body xmlns="http://www.w3.org/ns/SMIL">
            <xsl:apply-templates select="@id"/>
            <xsl:apply-templates select="*[self::seq or self::par]"/>
        </body>
    </xsl:template>
    <xsl:template match="seq[not(parent::body) and (not(descendant::text) or not(descendant::audio))]"/>
    <xsl:template match="seq">
        <seq xmlns="http://www.w3.org/ns/SMIL">
            <xsl:apply-templates select="@id"/>
            <xsl:apply-templates select="*[self::seq or self::par]"/>
        </seq>
    </xsl:template>
    <xsl:template match="par[descendant::text and descendant::audio]">
        <par xmlns="http://www.w3.org/ns/SMIL">
            <xsl:apply-templates select="@id"/>
            <xsl:apply-templates select="descendant::text[1]"/>
            <xsl:variable name="audio" select="descendant::audio"/>
            <audio xmlns="http://www.w3.org/ns/SMIL" src="{$audio[1]/@src}">
                <xsl:apply-templates select="$audio[1]/@id"/>
                <xsl:variable name="clip-begin" select="pf:smil-clock-value-to-seconds($audio[1]/@clip-begin)"/>
                <xsl:variable name="clip-end" select="pf:smil-clock-value-to-seconds($audio[@src=$audio[1]/@src][last()]/@clip-end)"/>
                <xsl:attribute name="clipBegin"
                    select="if ($clip-begin &lt; 60) then pf:smil-seconds-to-timecount($clip-begin)
                                else if ($clip-begin &lt; 3600) then pf:smil-seconds-to-partial-clock-value($clip-begin)
                                else pf:smil-seconds-to-full-clock-value($clip-begin)"/>
                <xsl:attribute name="clipEnd"
                    select="if ($clip-end &lt; 60) then pf:smil-seconds-to-timecount($clip-end)
                                else if ($clip-end &lt; 3600) then pf:smil-seconds-to-partial-clock-value($clip-end)
                                else pf:smil-seconds-to-full-clock-value($clip-end)"
                />
            </audio>
        </par>
    </xsl:template>
    <xsl:template match="par"/>
    <xsl:template match="text">
        <text xmlns="http://www.w3.org/ns/SMIL">
            <xsl:apply-templates select="@src | @id"/>
        </text>
    </xsl:template>
</xsl:stylesheet>
