<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:h="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    version="2.0" exclude-result-prefixes="#all">

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
            <xsl:for-each select="distinct-values(*/h:a/tokenize(@href,'#')[1])">
                <xsl:if test="matches(.,'smil$')">
                    <d:file href="{.}" media-type="application/smil+xml"/>
                </xsl:if>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>

</xsl:stylesheet>
