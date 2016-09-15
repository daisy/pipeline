<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    exclude-result-prefixes="c"
    version="2.0">

    <xsl:template match="/c:directory">
        <d:fileset>
            <xsl:apply-templates select="node() | @*[not(name()='name')]"/>
        </d:fileset>
    </xsl:template>
    
    <xsl:template match="c:other"/>
    
    <xsl:template match="c:directory">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="c:file">
        <d:file href="{substring-after(resolve-uri(encode-for-uri(@name),base-uri()),base-uri(/*))}">
            <xsl:apply-templates select="node() | @*[not(name()='name')]"/>
        </d:file>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
