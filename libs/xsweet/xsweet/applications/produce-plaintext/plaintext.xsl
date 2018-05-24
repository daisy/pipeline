<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <xsl:output method="text"/>
  
  <!-- We don't actually have to copy anything but in case we run
       in XProc we actually need to emit XML despite aiming for plain text.
       So we copy the document element. (Its tagging will be dropped when
       the result is serialized with method 'text.) -->
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <!-- Elements directly under 'div' get 2 x LF -->
  <xsl:template match="div/*">
    <xsl:text>&#xA;&#xA;</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  
</xsl:stylesheet>