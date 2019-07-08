<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:c="http://www.w3.org/ns/xproc-step" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
  <xsl:template match="c:files">
    <c:zip-manifest>
      <xsl:copy-of select="@xml:base"/>
      <xsl:apply-templates/>
    </c:zip-manifest>
  </xsl:template>
  <xsl:variable name="base-uri" select="/*/@xml:base" as="xs:string"/>
  <xsl:template match="c:file">
    <c:entry name="{replace(replace(@name, '%5B', '['), '%5D', ']')}"
      href="{concat($base-uri, replace(replace(@name, '\[', '%5B'), '\]', '%5D'))}" compression-method="deflate"
      compression-level="default"/>
  </xsl:template>
</xsl:stylesheet>