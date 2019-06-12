<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <xsl:variable name="html" select="collection() except collection()[1]"/>
    <xsl:variable name="base-dir" select="replace(base-uri(collection()[1]/*),'[^/]+$','')"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="a[@href]">
        <xsl:variable name="id" select="tokenize(@href,'#')[last()]"/>
        <xsl:variable name="target-base" select="$html//*[@id=$id]/base-uri(.)"/>
        <xsl:variable name="href" select="substring-after($target-base,$base-dir)"/>
        
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:attribute name="href" select="concat($href,'#',$id)"/>
            <xsl:apply-templates select="@* except @href | node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
