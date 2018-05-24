<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
  
  <xsl:variable name="transformation-sequence">
    <xsw:transform version="2.0">mark-lists-v1.xsl</xsw:transform>
    <xsw:transform version="2.0">nest-lists-v1.xsl</xsw:transform>
  </xsl:variable>

<!-- All the following is copied verbatim from PROMOTE-lists.xsl; one or the other should be superseded. -->

  <!-- Dummy template quiets anxious XSLT engines when HTML is provided as input. -->
  <xsl:template match="/html:html" xmlns:html="http://www.w3.org/1999/xhtml">
    <xsl:next-match/>
  </xsl:template>
    
  <!-- traps the root node of the source and passes it down the chain of transformation references -->
  <xsl:template match="/">
    <xsl:variable name="source" select="."/>
    <xsl:iterate select="$transformation-sequence/*">
      <xsl:param name="sourcedoc" select="$source" as="document-node()"/>
      <xsl:on-completion select="$sourcedoc"/>
      <xsl:next-iteration>
        <xsl:with-param name="sourcedoc">
          <xsl:apply-templates select=".">
            <xsl:with-param name="sourcedoc" select="$sourcedoc"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:next-iteration>
    </xsl:iterate>
  </xsl:template>
  
  <xsl:template match="xsw:transform">
    <xsl:param    name="sourcedoc" as="document-node()"/>
    <xsl:variable name="xslt-spec" select="."/>
    <xsl:variable name="runtime"   select="map {
      'xslt-version'        : xs:decimal($xslt-spec/@version),
      'stylesheet-location' : string($xslt-spec),
      'source-node'         : $sourcedoc }" />
    <!-- The function returns a map; primary results are under 'output'
         unless a base output URI is given
         https://www.w3.org/TR/xpath-functions-31/#func-transform -->
    <xsl:sequence select="transform($runtime)?output"/>
  </xsl:template>

  <!-- Not knowing any better, we simply pass along. -->
  <xsl:template match="*">
    <xsl:param    name="sourcedoc" as="document-node()"/>
    <xsl:sequence select="$sourcedoc"/>
  </xsl:template>
  
</xsl:stylesheet>