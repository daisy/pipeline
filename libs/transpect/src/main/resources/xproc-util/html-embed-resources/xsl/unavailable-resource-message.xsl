<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:svg="http://www.w3.org/2000/svg"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:template match="*">
    <xsl:variable name="source" select="(@src, @href, @data)"/>
    <span class="file-not-found-error">File not found: <xsl:value-of select="$source"/></span>
  </xsl:template>
  
  <xsl:template match="script">
    <xsl:copy>
      <xsl:comment>
      <xsl:value-of select="'// Script resource not found: ', @src"/>
    </xsl:comment>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="link[@rel eq 'stylesheet']">
    <xsl:copy>
      <xsl:comment>
      <xsl:value-of select="'/* CSS resource not found: ', @href, '*/'"/>
    </xsl:comment>  
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>