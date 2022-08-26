<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:smil="http://www.w3.org/2001/SMIL20/"
                xpath-default-namespace="http://openebook.org/namespaces/oeb-package/1.0/"
                exclude-result-prefixes="#all">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="package">
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(.),'[^/]+$','')"/>

            <!-- start with the SMILs in spine order -->
            <xsl:variable name="smils" as="element(item)*">
                <xsl:for-each select="spine/itemref">
                    <xsl:sequence select="//manifest/item[@id=current()/@idref]"/>
                </xsl:for-each>
            </xsl:variable>
            <xsl:apply-templates select="$smils"/>

            <xsl:apply-templates select="manifest/item[not(@media-type='application/smil')]"/>
        </d:fileset>
    </xsl:template>

    <xsl:template match="item">
        <d:file href="{@href}" media-type="{
            if (@media-type='application/smil')                          then 'application/smil+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.opf')) then 'application/oebps-package+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.ncx')) then 'application/x-dtbncx+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.xml')) then 'application/x-dtbook+xml'
            else if (@media-type='text/xml' and ends-with(@href,'.res')) then 'application/x-dtbresource+xml'
            else @media-type}"/>
    </xsl:template>

    <xsl:template match="text()"/>


</xsl:stylesheet>
