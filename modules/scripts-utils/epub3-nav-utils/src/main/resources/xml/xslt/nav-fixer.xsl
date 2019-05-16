<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all" version="2.0">

  <xsl:output method="xhtml" indent="yes"/>

  
  <xsl:param name="untitled" as="xs:string" select="'unwrap'"/>

  <xsl:strip-space elements="ol li"/>

  <xsl:template match="/">
    <xsl:message>Fixing navigation (toc.xhtml via nav-fixer.xsl)</xsl:message>
    <!-- DEBUGGING : retrieve the document structure -->
    <!--<xsl:variable name="document">
            <xsl:copy-of select="*"/>
    </xsl:variable>
    <xsl:message select='$document'/>-->

    <!-- Pass 1 : unwrap -->
    <xsl:variable name="temp">
      <xsl:apply-templates />
    </xsl:variable>

    <!-- pass 2 : remove elements with no remaining text -->
    <xsl:apply-templates mode="clean" select="$temp"/>
  </xsl:template>

  <!--Unwraps all the "Untitled" top-level children of the Navigation Document-->
  
  <xsl:template match="li[@data-generated='true']">
    <xsl:choose>
      <xsl:when test="$untitled='unwrap'">
        <!-- In unwrap mode, if a li with data generated attribute is found, 
          jump to the next ol subnodes. 
          (Note : ginving the structure, it will only copies the next sub li with no "data-generated" attribute) -->
        <!-- The template should apply on OL that posess li child without @daa-generated attributes -->
        <xsl:apply-templates select="ol/*" />
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
  
  <!-- For all nodes or attributes not previously matched -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>


  <!--remove elements with no text-->
  <xsl:template mode="clean" match="node()[descendant::text() != ''] | @*">
    <xsl:copy>
      <xsl:apply-templates mode="clean" select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  

</xsl:stylesheet>
