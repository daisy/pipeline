<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:param name="section-elements" />
  <xsl:param name="section-attr" />
  <xsl:param name="section-attr-val" />

  <xsl:variable name="sections" select="concat(',', $section-elements, ',')"/>

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()" priority="1" mode="inside-sentence">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="inside-sentence"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="ssml:s" priority="3">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="inside-sentence"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains($sections, concat(',', local-name(), ',')) and string(@*[local-name()=$section-attr]) = $section-attr-val]"
		priority="2">
    <tmp:group>
      <xsl:element name="{name()}" namespace="{namespace-uri()}">
	<xsl:apply-templates select="@*|node()"/>
      </xsl:element>
    </tmp:group>
  </xsl:template>

</xsl:stylesheet>

