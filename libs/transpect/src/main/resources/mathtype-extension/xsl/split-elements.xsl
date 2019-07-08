<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns="http://www.w3.org/1998/Math/MathML"
	 xpath-default-namespace="http://www.w3.org/1998/Math/MathML"
	 version="2.0">
  <xsl:import href="identity.xsl"/>

  <xsl:template match="mn[matches(., '\p{L}')]" mode="split-elements">
    <xsl:variable name="context" select="self::*" as="node()"/>
    <xsl:variable name="regex" select="'\p{L}+'"/>
    <xsl:variable name="split">
      <mrow>
        <xsl:analyze-string select="text()" regex="{$regex}">
          <xsl:matching-substring>
            <mi>
              <xsl:copy-of select="$context/@*, current()"/>
            </mi>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <mn>
              <xsl:copy-of select="$context/@*, current()"/>
            </mn>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </mrow>
    </xsl:variable>
    <xsl:apply-templates select="$split" mode="#current"/>
  </xsl:template>

</xsl:stylesheet>
