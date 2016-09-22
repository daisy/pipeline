<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" version="2.0">

    <xsl:template match="d:file">
        <xsl:variable name="href" select="resolve-uri(@href,base-uri(.))"/>
        <xsl:if test="every $fileset in collection() satisfies ($fileset//d:file[resolve-uri(@href,base-uri(.))=$href])">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
