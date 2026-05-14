<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" version="2.0">

    <xsl:variable name="other-filesets" as="element(d:fileset)*">
        <xsl:for-each select="collection()[position()&gt;1]">
            <xsl:apply-templates mode="absolute-hrefs" select="/d:fileset"/>
        </xsl:for-each>
    </xsl:variable>

    <xsl:template match="d:file">
        <xsl:variable name="href" select="resolve-uri(@href,base-uri(.))"/>
        <xsl:if test="every $fileset in $other-filesets satisfies $fileset//d:file[@href=$href]">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="absolute-hrefs" match="d:file/@href">
        <xsl:attribute name="{name()}" select="resolve-uri(.,base-uri(..))"/>
    </xsl:template>

    <xsl:template mode="#default absolute-hrefs" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
