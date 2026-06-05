<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dt:img">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(@id) and @alt">
	<xsl:attribute name="id"><xsl:value-of select="concat('ttsid-', generate-id())"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

