<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step" exclude-result-prefixes="#all">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="p:declare-step | p:pipeline">
        <xsl:variable name="local-name" select="tokenize(@type,':')[last()]"/>
        <xsl:variable name="namespace-uri" select="namespace-uri-from-QName(resolve-QName(@type,.))"/>
        <xsl:variable name="prefix" select="prefix-from-QName(resolve-QName(@type,.))"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="version" select="(@version,/*/@version)[1]"/>
            <xsl:attribute name="x:type" select="concat('{',$namespace-uri,'}',$local-name)"/>
            <xsl:namespace name="{$prefix}" select="$namespace-uri"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
