<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:tr="http://transpect.io"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:import href="http://transpect.io/xslt-util/uri-to-relative-path/xsl/uri-to-relative-path.xsl"/>
  
  <xsl:param name="cwd-uri" as="xs:string?"/>
  
  <xsl:template match="/*[empty(@local-href)]" priority="2">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
<!--      <xsl:message select="'RRRRRRRRRRRRRR ', $cwd-uri, ' ',@local-href"></xsl:message>-->
      <xsl:if test="exists(self::*:result) and normalize-space($cwd-uri)">
        <xsl:attribute name="rel-path" select="tr:uri-to-relative-path(concat($cwd-uri, '/'), @local-href)"/>  
      </xsl:if>
      <xsl:copy-of select="node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>