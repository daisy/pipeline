<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:function name="dt:smil">
    <xsl:param name="smilref"/>
    <xsl:value-of select="substring-before($smilref, '#')"/>
  </xsl:function>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@id">
    <xsl:attribute name="id">xxx</xsl:attribute>
  </xsl:template>

  <xsl:template match="@smilref">
    <xsl:variable name="smil" select="dt:smil(.)"/>
    <xsl:attribute name="smilref">
      <xsl:value-of select="string-join(//*[@smilref and dt:smil(@smilref) = $smil]/@id, '-')"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>


</xsl:stylesheet>
