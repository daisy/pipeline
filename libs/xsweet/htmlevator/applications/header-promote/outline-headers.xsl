<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all"
  version="2.0">

  <!-- XSweet: Performs header promotion based on outline level [2] -->
  <!-- Input:  an HTML Typescript document (wf) -->
  <!-- Output: a copy, with headers promoted according to outline levels detected on paragraphs -->
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="xsw:outline-level" as="xs:integer?">
    <xsl:param name="who" as="node()"/>
    <xsl:variable name="outline-spec" select="replace($who/@style,'^.*xsweet\-outline\-level:\s*','')"/>
    <xsl:variable name="outline-level" select="replace($outline-spec,'\D.*$','')"/>
    <xsl:if test="$outline-level castable as xs:integer">
      <xsl:sequence select="xs:integer($outline-level) + 1"/>
    </xsl:if>
  </xsl:function>
  
<!-- Produces an index relating given outline levels (extant in the document)
     to header levels h1-h6 ... including only the outline levels given.

     So one document could have
       <h1 level="0"/><h2 level="1"/><h3 level="2"/>
     while another has
       <h1 level="1"/><h2 level="3"/><h1 level="4"/>
       (in a document with no outline level '0' or '2')
     
     The result is used to map from the source, resulting
     in the highest outline levels actually given, becoming h1-h6.
-->

  <xsl:variable name="level-map" as="element()*">
    <xsl:for-each-group select="//body/p[exists(xsw:outline-level(.))]
      | //body/div[@class='docx-body']/p[exists(xsw:outline-level(.))]"
      group-by="xsw:outline-level(.)">
      <xsl:sort select="xsw:outline-level(.)"/>
      <xsl:if test="position() le 6">
        <xsl:element name="h{position()}">
          <xsl:attribute name="level" select="current-grouping-key()"/>
          <!--<xsl:apply-templates/>-->
        </xsl:element>  
      </xsl:if>  
    </xsl:for-each-group>
  </xsl:variable>
  
   <!--Diagnostic .... -->
    
  <!--<xsl:template match="body">
    <xsl:copy-of select="$level-map"/>
    <xsl:next-match/>
  </xsl:template>--> 
  
  <xsl:template match="p[xsw:outline-level(.) = $level-map/@level]">
    <xsl:variable name="given-level" select="xsw:outline-level(.)"/>
    <xsl:variable name="h-level" select="$level-map[@level = $given-level]/local-name()"/>
    <xsl:element name="{$h-level}" namespace="http://www.w3.org/1999/xhtml">
      <xsl:copy-of select="@*"/>
      <!--<xsl:comment expand-text="true">{ $level }</xsl:comment>-->
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  
  
</xsl:stylesheet>