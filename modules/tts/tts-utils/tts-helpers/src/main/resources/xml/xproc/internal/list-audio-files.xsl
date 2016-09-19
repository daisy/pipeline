<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:template match="/">
    <d:audio-files>
      <xsl:for-each-group select="//d:clip" group-by="@src">
	<d:file src="{current-grouping-key()}"/>
      </xsl:for-each-group>
    </d:audio-files>
  </xsl:template>

</xsl:stylesheet>
