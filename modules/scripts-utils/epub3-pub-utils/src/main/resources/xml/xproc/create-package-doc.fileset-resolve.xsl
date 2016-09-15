<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-result-prefixes="#all">
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="d:fallback">
        <!-- matching the wrapper element explicitly to avoid the SXXP0005 warning -->
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="d:file">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="href" select="resolve-uri(@href,base-uri(.))"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
