<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

    <!--
        Convert a d:fileset, representing a set of files on disk, to a c:zip-manifest which can be
        passed to a px:zip step (http://exproc.org/proposed/steps/other.html#zip) to create a zip
        file with those contents. The hrefs must be relative paths (relative to the xml:base
        attribute) and must point to files on disk. Non-relative paths or relative paths starting
        with ".." are ignored.
    -->

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
