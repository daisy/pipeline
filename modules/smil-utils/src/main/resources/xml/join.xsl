<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mo="http://www.w3.org/ns/SMIL" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:variable name="base" select="pf:longest-common-uri(distinct-values(/*/*//*/replace(base-uri(.),'^(.*/)[^/]*$','$1')))"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="xml:base" select="$base"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*[@src or @epub:textref]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="@src">
                <xsl:attribute name="src" select="pf:relativize-uri(@src,$base)"/>
            </xsl:if>
            <xsl:if test="@epub:textref">
                <xsl:attribute name="epub:textref" select="pf:relativize-uri(@epub:textref,$base)"/>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
