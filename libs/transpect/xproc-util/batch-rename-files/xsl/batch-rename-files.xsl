<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  version="2.0">
  
  <!--  *
        * this stylesheet is used to rename file reference attributes
        * based on a matching and replace expression
        * -->
  
  <xsl:param name="attribute-name" as="xs:string"/>
  <xsl:param name="regex-match" as="xs:string"/>
  <xsl:param name="regex-replace" as="xs:string"/>
  
  <xsl:template match="*[@*[local-name() eq $attribute-name]]">
    <xsl:variable name="original-href" select="@*[local-name() eq $attribute-name]" as="attribute()"/>
    <xsl:variable name="new-href" select="replace($original-href, $regex-match, $regex-replace)" as="xs:string"/>
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="matches($original-href, $regex-match) and not(matches($original-href, '^(http|ftp)://'))">
          <xsl:attribute name="tr:original-href" select="$original-href"/>  
          <xsl:attribute name="{$attribute-name}" select="$new-href"/>
          <xsl:apply-templates select="@* except $original-href | node()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>