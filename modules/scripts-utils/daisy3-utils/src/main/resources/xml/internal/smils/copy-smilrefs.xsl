<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:key name="audio-loc" match="*[@id]" use="@id"/>

  <xsl:template match="node()|@*" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@id]" priority="2">
    <xsl:copy>
      <xsl:variable name="model" select="key('audio-loc', @id, collection()[/d:audio-order])[1]"/>
      <xsl:if test="$model/@smilref">
	<xsl:attribute name="smilref"
		       namespace="{if (namespace-uri() = 'http://www.daisy.org/z3986/2005/dtbook/') then ''
				  else 'http://www.daisy.org/z3986/2005/dtbook/'}">
	  <!-- According to the specifications, @smilref must have the
	       DTBook namespace for elements that belong to
	       extensions. -->
	  <xsl:value-of select="$model/@smilref"/>
	</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
