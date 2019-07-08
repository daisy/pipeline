<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns="http://www.w3.org/1998/Math/MathML"
    version="1.0">

  <!-- Summations -->

  <xsl:template match="tmpl[selector = 'tmSUM'][variation = 'tvBO_SUM']">
    <mstyle displaystyle="true">
      <munderover>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][4]"/>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][2]"/>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][3]"/>
      </munderover>
      <xsl:apply-templates select="*[self::slot or self::pile or self::char][1]"/>
    </mstyle>
  </xsl:template>

  <xsl:template match="tmpl[selector = 'tmSUM' and not(variation = 'tvBO_SUM')]">
    <mstyle displaystyle="true">
      <msubsup>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][4]"/>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][2]"/>
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][3]"/>
      </msubsup>
      <xsl:apply-templates select="*[self::slot or self::pile or self::char][1]"/>
    </mstyle>
  </xsl:template>

  <!-- Sum operator -->
  <xsl:template match="tmpl[selector = 'tmSUMOP']">
    <munderover>
      <mstyle displaystyle="true" mathsize="140%">
        <xsl:apply-templates select="*[self::slot or self::pile or self::char][4]"/>
      </mstyle>
      <xsl:apply-templates select="*[self::slot or self::pile or self::char][2]"/>
      <xsl:apply-templates select="*[self::slot or self::pile or self::char][3]"/>
    </munderover>
  </xsl:template>

</xsl:stylesheet>
