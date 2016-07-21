<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" exclude-result-prefixes="#all" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                                             xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                                             xmlns:d="http://www.daisy.org/ns/pipeline/data"
                                                             xmlns:opf="http://www.idpf.org/2007/opf">

    <xsl:template match="opf:package">
        <!-- content files first, and in spine order -->
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(/*),'(.*/)[^/]*','$1')"/>
            <d:file href="{replace(base-uri(/*),'.*/','')}" media-type="application/oebps-package+xml"/>
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
