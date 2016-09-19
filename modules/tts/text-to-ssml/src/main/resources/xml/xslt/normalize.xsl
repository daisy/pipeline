<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:param name="word-element" />
  <xsl:param name="word-attr" select="''"/>
  <xsl:param name="word-attr-val" select="''"/>

  <xsl:variable name="sentence-ids" select="collection()[2]"/>
  <xsl:key name="sentences" match="*[@id]" use="@id"/>

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()" priority="1" mode="inside-sentence">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="inside-sentence"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[key('sentences', @id, $sentence-ids)]"
		priority="3">
    <ssml:s>
      <xsl:apply-templates select="@*|node()" mode="inside-sentence"/>
    </ssml:s>
  </xsl:template>

  <xsl:template match="*[local-name()=$word-element and string(@*[local-name()=$word-attr]) = $word-attr-val]"
		priority="2" mode="inside-sentence">
    <ssml:token>
      <xsl:apply-templates select="@*|node()" mode="inside-sentence"/>
    </ssml:token>
  </xsl:template>

</xsl:stylesheet>
