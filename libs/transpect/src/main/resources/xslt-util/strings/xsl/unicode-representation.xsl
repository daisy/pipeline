<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:import href="http://this.transpect.io/xslt-util/hex/xsl/hex.xsl"/>
  
  <xsl:function name="tr:string-to-UPAD4" as="xs:string*">
    <xsl:param name="chars" as="xs:string"/>
    <xsl:sequence select="for $c in string-to-codepoints($chars) return concat('U+', tr:pad(tr:dec-to-hex($c), 4))"/>
  </xsl:function>
  
  <xsl:function name="tr:string-to-char-UPAD4" as="xs:string*">
    <xsl:param name="chars" as="xs:string"/>
    <xsl:sequence select="
      for $n 
      in (1 to string-length($chars))
      return string-join((substring($chars, $n, 1), ' (', tr:string-to-UPAD4(substring($chars, $n, 1)), ')'), '')"/>
  </xsl:function>
</xsl:stylesheet>