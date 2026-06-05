<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:sm="http://www.w3.org/2001/SMIL20/"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@end">
    <xsl:attribute name="end">
      <xsl:value-of select="concat(., ancestor::sm:seq[1]/*[last()]/@id,'.end')"/>
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
