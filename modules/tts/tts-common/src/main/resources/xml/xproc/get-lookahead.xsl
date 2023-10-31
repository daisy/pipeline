<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:function name="ssml:get-lookahead">
    <xsl:param name="node"/>
    <xsl:for-each select="$node/following-sibling::node()[preceding-sibling::ssml:token[1] = $node]">
      <xsl:choose>
	<xsl:when test="self::text()">
	  <xsl:value-of select="."/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="string-join(.//text(),'')"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>

</xsl:stylesheet>