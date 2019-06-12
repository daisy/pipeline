<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:param name="body-is-section" required="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="h1 | h2 | h3 | h4 | h5 | h6">
        <xsl:variable name="level" select="count(ancestor::section | ancestor::article | ancestor::nav | ancestor::aside)"/>
        <xsl:variable name="level" select="if ($body-is-section='true') then ($level+1) else $level"/>
        <xsl:variable name="level" select="min(($level,6))"/>
        <xsl:element name="h{$level}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
