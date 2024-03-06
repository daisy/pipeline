<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:mathml="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

  <xsl:template match="/">
    <xsl:call-template name="pf:next-match-with-generated-ids">
      <xsl:with-param name="prefix" select="'forsmil-'"/>
      <xsl:with-param name="for-elements" select="//*[not(@id) and empty(@pxi:no-smilref)]"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*[not(@id) and empty(@pxi:no-smilref)]" priority="2">
    <xsl:copy>
      <xsl:call-template name="pf:generate-id"/>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="stop-recursion" match="mathml:math" priority="3">
    <xsl:copy>
      <xsl:if test="not(@id) and empty(@pxi:no-smilref)">
        <xsl:call-template name="pf:generate-id"/>
      </xsl:if>
      <xsl:copy-of select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
