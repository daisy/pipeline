<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="opf:package">
        <!-- content files first, and in spine order -->
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(pf:base-uri(/*),'(.*/)[^/]*','$1')"/>
            <d:file href="{replace(pf:base-uri(/*),'.*/','')}" media-type="application/oebps-package+xml" media-version="{@version}"/>
            <xsl:apply-templates select="opf:spine/opf:itemref"/>
            <xsl:apply-templates select="opf:manifest/opf:item[not(@id=/*/opf:spine/opf:itemref/@idref)]"/>
        </d:fileset>
    </xsl:template>
    
    <xsl:template match="opf:itemref">
        <xsl:apply-templates select="/*/opf:manifest/opf:item[@id=current()/@idref]"/>
    </xsl:template>
    
    <xsl:template match="opf:item">
        <d:file>
            <xsl:copy-of select="@href | @media-type"/>
        </d:file>
    </xsl:template>

</xsl:stylesheet>
