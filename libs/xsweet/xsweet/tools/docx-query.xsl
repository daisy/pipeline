<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <xsl:param name="docx-file-uri" as="xs:string"/>

  <xsl:output indent="yes"/>
  
  <xsl:variable name="document-xml">
    <xsl:if test="$docx-file-uri castable as xs:anyURI and ends-with($docx-file-uri,'docx')">
      <xsl:variable name="document-path" select="concat('jar:',$docx-file-uri,'!/word/document.xml')"/>    
      <xsl:sequence select="document($document-path)"/>
    </xsl:if>
  </xsl:variable>
  
  <xsl:template match="/">
    <xsl:variable name="hits" select="$document-xml//w:b[@w:val='0']"/>
    <results count="{count($hits)}">
      <xsl:copy-of select="$hits"/>
    </results>
  </xsl:template>
</xsl:stylesheet>