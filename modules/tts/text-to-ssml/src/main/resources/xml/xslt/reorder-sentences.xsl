<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:variable name="ids" select="collection()[1]"/>
  <xsl:key name="sentences" match="ssml:s" use="@id"/>
  <xsl:key name="ids" match="*[@id]" use="@id"/>

  <xsl:variable name="ssml-docs">
    <ssml:all>
      <xsl:for-each select="collection()[position() > 1]">
	<xsl:sequence select="current()"/>
      </xsl:for-each>
    </ssml:all>
  </xsl:variable>

  <xsl:template match="/">
    <ssml:speak version="1.1">
      <xsl:for-each select="$ids/*/*">
	<xsl:variable name="sentence" select="key('sentences', @id, $ssml-docs)"/>
	<xsl:if test="$sentence">
	  <xsl:copy-of select="$sentence"/>
	</xsl:if>
      </xsl:for-each>

      <!-- TODO: write that on the secondary port? -->
      <xsl:for-each select="$ssml-docs/ssml:all/ssml:speak/ssml:s">
      	<xsl:if test="not(key('ids', @id, $ids))">
      	  <xsl:copy-of select="current()"/>
      	</xsl:if>
      </xsl:for-each>
    </ssml:speak>
  </xsl:template>

</xsl:stylesheet>
