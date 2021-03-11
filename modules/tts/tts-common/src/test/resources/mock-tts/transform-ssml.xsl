<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="text"/>
  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark" select="''"/>
  <xsl:template match="*">
    <xsl:value-of select="string-join(.//text(),'')"/>
  </xsl:template>
</xsl:stylesheet>
