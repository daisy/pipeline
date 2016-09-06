<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs brl">

    <xsl:param name="announcement" as="xs:string">'&lt;=</xsl:param>
    <xsl:param name="deannouncement" as="xs:string">'&lt;=</xsl:param>

    <xsl:output method="xml" encoding="utf-8" indent="no"/>

    <xsl:template match="text()[normalize-space(.)!='' and ancestor::dtb:prodnote]">
      <xsl:if test="not(preceding::text()[normalize-space(.)!=''][1][ancestor::dtb:prodnote])">
	<brl:literal><xsl:value-of select="$announcement"/></brl:literal>
      </xsl:if>
      <xsl:sequence select="."/>
      <xsl:if test="not(following::text()[normalize-space(.)!=''][1][ancestor::dtb:prodnote])">
	<brl:literal><xsl:value-of select="$deannouncement"/></brl:literal>
      </xsl:if>
    </xsl:template>

    <xsl:template match="dtb:prodnote">
      <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*">
      <xsl:copy>
	<xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|comment()|processing-instruction()">
      <xsl:sequence select="."/>
    </xsl:template>

</xsl:stylesheet>
