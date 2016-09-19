<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
		exclude-result-prefixes="#all"
		version="2.0">

  <!-- only the most important @ids are listed -->

  <xsl:template match="/">
    <tmp:ids>
      <xsl:for-each select="//ssml:s[@id]">
	<xsl:variable name="sent-id" select="@id"/>
	<tmp:element id="{$sent-id}"/>
	<xsl:for-each select=".//*[@id]">
	  <xsl:if test="@id != $sent-id">
	    <tmp:element id="{@id}"/>
	  </xsl:if>
	</xsl:for-each>
      </xsl:for-each>
    </tmp:ids>
  </xsl:template>

</xsl:stylesheet>
