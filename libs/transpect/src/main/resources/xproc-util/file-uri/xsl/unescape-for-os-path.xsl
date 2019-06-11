<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io" 
  exclude-result-prefixes="xs" version="2.0">
  
  <xsl:import href="http://transpect.io/xslt-util/hex/xsl/hex.xsl"/>
  
  <xsl:param name="uri" as="xs:string"/>
  <xsl:param name="attribute-names" as="xs:string"/>
  
  <xsl:variable name="tokenized-attribute-names" as="xs:string*" select="tokenize($attribute-names, '\s+')[normalize-space()]"/>
  
  <xsl:template name="main">
    <xsl:choose>
      <xsl:when test="normalize-space($uri)">
        <c:result>
          <xsl:sequence select="tr:unescape-uri($uri)"/>
        </c:result>    
      </xsl:when>
      <xsl:when test="normalize-space($attribute-names)">
        <xsl:apply-templates select="/" mode="#default"/>    
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'unescape-for-os-paths.xsl: either $uri or $attribute-names must be given'"/>
        <c:result/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@* | node()" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*[name() = $tokenized-attribute-names]" mode="#default">
    <xsl:attribute name="{name()}" select="tr:unescape-uri(.)"/>
  </xsl:template>

</xsl:stylesheet>