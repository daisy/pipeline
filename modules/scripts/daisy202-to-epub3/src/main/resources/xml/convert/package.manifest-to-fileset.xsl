<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <xsl:param name="base" required="yes"/>
    <xsl:template match="/*">
        <d:fileset xml:base="{$base}">
            <xsl:for-each select="//opf:item">
                <d:file>
                    <xsl:copy-of select="@href|@media-type"/>
                </d:file>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>
</xsl:stylesheet>
