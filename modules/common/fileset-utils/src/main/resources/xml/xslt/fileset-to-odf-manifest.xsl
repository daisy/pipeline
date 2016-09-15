<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">
    
    <xsl:template match="/d:fileset">
        <manifest:manifest>
            <!--TODO @xml:base ?-->
            <xsl:apply-templates/>
        </manifest:manifest>
    </xsl:template>
    
    <xsl:template match="d:file[@href and not(matches(@href,'^[^/]+:') or starts-with(@href,'..'))]">
        <manifest:file-entry manifest:full-path="{@href}" manifest:media-type="{@media-type}"/>
    </xsl:template>
</xsl:stylesheet>