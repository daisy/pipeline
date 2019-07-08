<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns="http://www.w3.org/1998/Math/MathML"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:include href="identity.xsl"/>
  <xsl:param name="mml-space-handling" select="'mspace'"/>
  
  <xsl:param name="em-width" select="'1em'"/>
  <xsl:param name="en-width" select="'0.33em'"/>
  <xsl:param name="standard-width" select="'0.16em'"/>
  <xsl:param name="thin-width" select="'0.08em'"/>
  <xsl:param name="hair-width" select="'0.08em'"/>
  <xsl:param name="zero-width" select="'0em'"/>

  <xsl:template match="*[self::mtext or self::mi][matches(.,'\s|[&#x2000;-&#x200b;]')]" mode="handle-whitespace">
    <xsl:variable name="self" select="."/>
    <xsl:choose>
      <xsl:when test="$mml-space-handling='mspace'">
        <xsl:analyze-string select="." regex="\s|[&#x2000;-&#x200b;]">
          <xsl:matching-substring>
            <xsl:variable name="width">
              <xsl:choose>
                <xsl:when test=". = '&#x2002;'">
                  <xsl:value-of select="$en-width"/>
                </xsl:when>
                <xsl:when test=". = '&#x2003;'">
                  <xsl:value-of select="$em-width"/>
                </xsl:when>
                <xsl:when test=". = '&#x2004;'">
                  <xsl:value-of select="$en-width"/>
                </xsl:when>
                <xsl:when test=". = '&#x2009;'">
                  <xsl:value-of select="$thin-width"/>
                </xsl:when>
                <xsl:when test=". = '&#xa0;'">
                  <xsl:value-of select="$hair-width"/>
                </xsl:when>
                <xsl:when test=". = '&#x200b;'">
                  <xsl:value-of select="$zero-width"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$standard-width"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <mspace width="{$width}"/>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <mtext>
              <xsl:apply-templates select="$self/@*"/>
              <xsl:value-of select="current()"/>
            </mtext>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:when>
      <xsl:otherwise>
        <xsl:next-match/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
