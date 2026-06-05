<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="h:html">
        <xsl:apply-templates select="child::h:body"/>
    </xsl:template>

    <xsl:template match="h:body">
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(/*),'^(.+/)[^/]*$','$1')"/>
            <xsl:choose>
                <!-- http://www.daisy.org/z3986/specifications/daisy_202.html#mastersmil -->
                <xsl:when test="pf:file-exists(resolve-uri('master.smil',base-uri(/*)))">
                    <d:file href="master.smil" media-type="application/smil+xml"/>
                </xsl:when>
                <xsl:when test="pf:file-exists(resolve-uri('MASTER.SMIL',base-uri(/*)))">
                    <d:file href="MASTER.SMIL" media-type="application/smil+xml"/>
                </xsl:when>
            </xsl:choose>
            <xsl:for-each select="distinct-values(*/h:a/tokenize(@href,'#')[1])">
                <xsl:if test="matches(.,'\.(smil|SMIL)$')">
                    <d:file href="{.}" media-type="application/smil+xml"/>
                </xsl:if>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>

</xsl:stylesheet>
