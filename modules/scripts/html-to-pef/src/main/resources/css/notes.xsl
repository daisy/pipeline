<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

  <!--
      Insert an endnotes section element
  -->

  <xsl:param name="endnotes-section-id" as="xs:string" select="''"/>

  <xsl:template match="/*">
    <xsl:choose>
      <xsl:when test="$endnotes-section-id!=''
                      and //a[tokenize(@epub:type,'\s+')='noteref']">
        <xsl:copy>
          <xsl:sequence select="@*|node()"/>
          <div id="{$endnotes-section-id}"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
