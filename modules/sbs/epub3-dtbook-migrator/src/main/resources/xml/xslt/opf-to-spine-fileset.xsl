<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:template match="/opf:package">
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(.),'^(.*/).*$','$1')"/>
            <xsl:for-each select="opf:spine/opf:itemref">
                <xsl:variable name="idref" select="@idref"/>
                <xsl:for-each select="/*/opf:manifest/opf:item[@id=$idref]">
                    <d:file href="{@href}" media-type="{@media-type}"/>
                </xsl:for-each>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>

</xsl:stylesheet>
