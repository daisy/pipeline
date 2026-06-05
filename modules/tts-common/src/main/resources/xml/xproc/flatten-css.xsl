<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="#all"
    version="2.0">

  <!-- ================================================================================= -->
  <!-- Retrieve the CSS properties and the current language from a the node's ancestors. -->
  <!-- Put them into attributes.                                                         -->
  <!-- ================================================================================= -->

  <xsl:variable name="properties"
		select="'voice-family,richness,volume,stress,speech-rate,pitch,pitch-range,speak,azimuth,speak-punctuation,speak-numeral,elevation'"/>

  <xsl:template match="*" mode="flatten-css-properties">
    <xsl:param name="style-ns" select="''"/>
    <xsl:param name="lang"/>
    <xsl:variable name="node" select="."/>
    <xsl:variable name="ancestor" select="ancestor-or-self::*[@xml:lang][1]"/>

    <xsl:attribute namespace="http://www.w3.org/XML/1998/namespace" name="lang">
      <xsl:value-of select="if ($ancestor) then $ancestor/@xml:lang else $lang"/>
    </xsl:attribute>

    <xsl:for-each select="tokenize($properties, ',')">
      <xsl:variable name="property" select="current()"/>
      <xsl:variable name="ref"
		    select="$node/ancestor-or-self::*[@*[local-name() = $property and namespace-uri() = $style-ns]][1]"/>
      <xsl:if test="$ref">
	<xsl:attribute namespace="{$style-ns}" name="{$property}">
	  <xsl:value-of select="$ref/@*[local-name() = $property and namespace-uri() = $style-ns]" />
	</xsl:attribute>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

