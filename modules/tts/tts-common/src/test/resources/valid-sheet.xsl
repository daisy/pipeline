<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <html>
      <xsl:copy-of select="/*"/>
    </html>
  </xsl:template>

</xsl:stylesheet>