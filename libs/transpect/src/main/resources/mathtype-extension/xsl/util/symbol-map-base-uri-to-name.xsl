<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
  <xsl:function name="tr:symbol-map-base-uri-to-name" as="xs:string">
    <xsl:param name="symbols" as="document-node(element(symbols))"/>
    <xsl:sequence
      select="($symbols/symbols/@mathtype-name, translate(replace(base-uri($symbols/symbols), '^.+/([^./]+)\.xml$', '$1'), '_', ' '))[1]"
    />
  </xsl:function>
</xsl:stylesheet>
