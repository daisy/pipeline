<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                exclude-result-prefixes="#all">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create text nodes for page number -->
  <xsl:template match="*[@epub:type/tokenize(.,'\s+')='pagebreak' or @role='doc-pagebreak']
                        [not(*|text()[normalize-space(.)])]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:value-of select="(@aria-label,@title)[1]"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
