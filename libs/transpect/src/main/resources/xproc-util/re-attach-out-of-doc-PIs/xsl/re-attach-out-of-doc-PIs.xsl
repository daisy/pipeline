<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:param name="file-uri" as="xs:string"/>
  <xsl:param name="separator" as="xs:string?"/>
  
  <xsl:variable name="as-string" select="unparsed-text($file-uri)"/>
  <xsl:variable name="begin" as="xs:string" select="replace($as-string, '^(.*?)&lt;\i.+$', '$1', 's')"/>
  <xsl:variable name="end" as="xs:string" select="replace($as-string, '^.+\c>(.*?)$', '$1', 's')"/>
  <xsl:variable name="begin-PIs" as="node()*">
    <xsl:call-template name="analyze">
      <xsl:with-param name="string" as="xs:string" select="$begin"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="end-PIs" as="node()*">
    <xsl:call-template name="analyze">
      <xsl:with-param name="string" as="xs:string" select="$end"/>
    </xsl:call-template>
  </xsl:variable>
  
  <xsl:template name="analyze">
    <xsl:param name="string" as="xs:string"/>
    <xsl:analyze-string select="$string" regex="&lt;\?(\i\c*)(\s+(.+?))?\?>" flags="s">
      <xsl:matching-substring>
        <xsl:if test="not(matches(regex-group(1), '^xml$', 'i'))">
          <xsl:processing-instruction name="{regex-group(1)}" select="regex-group(3)"/>
          <xsl:value-of select="$separator"/>
        </xsl:if>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:template>
  
  <xsl:template match="/*">
    <xsl:document>
      <xsl:if test="exists($begin-PIs)">
        <xsl:value-of select="$separator"/>
      </xsl:if>
      <xsl:sequence select="$begin-PIs"/>
      <xsl:copy-of select="."/>
      <xsl:if test="exists($end-PIs)">
        <xsl:value-of select="$separator"/>
      </xsl:if>
      <xsl:sequence select="$end-PIs"/>
    </xsl:document>
  </xsl:template>

</xsl:stylesheet>
