<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs tr"
  version="2.0">
  <xsl:function name="tr:sequenceType" as="xs:string">
    <xsl:param name="item" as="item()"/>
    <xsl:choose>
      <xsl:when test="$item instance of xs:boolean">
        <xsl:sequence select="'boolean'"/>
      </xsl:when>
      <xsl:when test="$item instance of xs:double">
        <xsl:sequence select="'double'"/>
      </xsl:when>
      <xsl:when test="$item instance of xs:integer">
        <xsl:sequence select="'integer'"/>
      </xsl:when>
      <xsl:when test="$item instance of xs:string">
        <xsl:sequence select="'string'"/>
      </xsl:when>
      <xsl:when test="$item instance of xs:anyURI">
        <xsl:sequence select="'anyURI'"/>
      </xsl:when>
      <xsl:when test="$item instance of xs:anyAtomicType">
        <xsl:sequence select="'anyAtomicType'"/>
      </xsl:when>
      <xsl:when test="$item instance of attribute()">
        <xsl:sequence select="concat('attribute(', name($item), ')')"/>
      </xsl:when>
      <xsl:when test="$item instance of element(*)">
        <xsl:sequence select="concat('element(', name($item), ')')"/>
      </xsl:when>
      <xsl:when test="$item instance of text()">
        <xsl:sequence select="'text()'"/>
      </xsl:when>
      <xsl:when test="$item instance of processing-instruction()">
        <xsl:sequence select="concat('processing-instruction(', name($item), ')')"/>
      </xsl:when>
      <xsl:when test="$item instance of comment()">
        <xsl:sequence select="'comment()'"/>
      </xsl:when>
      <xsl:when test="$item instance of document-node()">
        <xsl:variable name="inner" as="item()*" select="$item/(@* | node())"/>
        <xsl:variable name="prelim" as="xs:string*" select="for $i in $inner return tr:sequenceType($i)"/>
        <xsl:sequence select="concat(
                                'document-node(', 
                                string-join($prelim, ','),
                                ')'
                              )"/>
      </xsl:when>
    </xsl:choose>
  </xsl:function>
  
</xsl:stylesheet>