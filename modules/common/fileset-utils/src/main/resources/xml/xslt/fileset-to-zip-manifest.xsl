<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data" version="2.0" exclude-result-prefixes="#all" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="d:fileset">
        <c:zip-manifest>
            <xsl:copy-of select="@xml:base"/>
            <xsl:apply-templates select="*"/>
        </c:zip-manifest>
    </xsl:template>

    <xsl:template match="d:file[@href and not(matches(@href,'^[^/]+:') or starts-with(@href,'..'))]">
        <c:entry href="{@href}" name="{pf:unescape-uri(@href)}"/>
    </xsl:template>

</xsl:stylesheet>
