<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:ssml="http://www.w3.org/2001/10/synthesis">
  <xsl:output method="text"/>
  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark" select="''"/>
  <xsl:template match="/*">
    <xsl:next-match/>
    <xsl:text>[[slnc 250ms]]</xsl:text>
  </xsl:template>
  <xsl:template match="*">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="text()">
    <xsl:sequence select="."/>
  </xsl:template>
  <xsl:template match="ssml:phoneme">
    <xsl:text>&lt;phoneme&gt;</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>&lt;/phoneme&gt;</xsl:text>
  </xsl:template>
</xsl:stylesheet>
