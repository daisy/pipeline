<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/">

  <xsl:output indent="yes"/>


  <!-- Identity template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!--
    Workaround for issue https://github.com/daisy/pipeline-mod-braille/issues/162
    The application doesn't handle backslashes well so we convert them to forward slashes
  -->
  <xsl:template match="@src">
      <xsl:attribute name="src">
          <xsl:value-of select="translate(., '\', '/')"/>
      </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
