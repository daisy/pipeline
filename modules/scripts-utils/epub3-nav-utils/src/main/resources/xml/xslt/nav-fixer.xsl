<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all" version="2.0">

  <xsl:output method="xhtml" indent="yes"/>
  
  <xsl:param name="untitled" as="xs:string" select="'unwrap'"/>

  <!--Unwraps all the "Untitled" top-level children of the Navigation Document-->
  
  <xsl:template match="li[@data-generated='true']">
    <xsl:choose>
      <xsl:when test="$untitled='unwrap'">
        <xsl:apply-templates select="ol/*"/>
      </xsl:when>
      <xsl:when test="$untitled='hide'">
        <li hidden="true">
          <xsl:apply-templates select="@* except @data-generated | node()"/>
        </li>
      </xsl:when>
      <xsl:when test="$untitled='include'">
        <li>
          <xsl:apply-templates select="@* except @data-generated | node()"/>
        </li>
      </xsl:when>
      <!-- otherwise excluded -->
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
    

</xsl:stylesheet>
