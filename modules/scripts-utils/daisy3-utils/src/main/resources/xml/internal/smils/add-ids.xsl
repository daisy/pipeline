<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:param name="no-smilref"/>
  <xsl:param name="stop-recursion" select="''"/>

  <xsl:template match="*[not(@id) and not(contains($no-smilref, concat(' ', local-name(), ' ')))]" priority="2">
    <xsl:copy>
      <xsl:attribute name="id">
	<xsl:value-of select="concat('forsmil-', generate-id())"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains($stop-recursion, concat(' ', local-name(), ' '))]" priority="3">
    <xsl:copy>
      <xsl:if test="not(@id) and not(contains($no-smilref, concat(' ', local-name(), ' ')))">
	<xsl:attribute name="id">
	  <xsl:value-of select="concat('forsmil-', generate-id())"/>
	</xsl:attribute>
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
