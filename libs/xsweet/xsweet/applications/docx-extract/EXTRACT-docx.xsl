<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
 
 
  <!-- XSweet: 'wrapper' XSLT for docx extraction with cleanup; using this XSLT in a 3.0 processor replaces five calls to distinct XSLTs. [1] -->
  <!-- Input: a WordML document.xml file as extracted from .docx input, with its related (neighbor) files in place -->
  <!-- Output: HTML Typescript - fairly clean and regular HTML -->
  
<!-- Use this XQuery to get a list of stylesheets called by an XProc pipeline:

declare namespace p='http://www.w3.org/ns/xproc';
declare namespace xsw ="http://coko.foundation/xsweet";

<xsw:variable name="transformation-sequence">
{ //p:input[@port='stylesheet']/*/@href/
  <xsw:transform>{string(.)}</xsw:transform> 

}</xsw:variable>

  -->
 
  <xsl:variable name="transformation-sequence">
    <xsw:transform>docx-html-extract.xsl</xsw:transform>
    <xsw:transform>handle-notes.xsl</xsw:transform>
    <xsw:transform>scrub.xsl</xsw:transform>
    <xsw:transform>join-elements.xsl</xsw:transform>
    <xsw:transform>collapse-paragraphs.xsl</xsw:transform>
  </xsl:variable> 
  
  <!-- Dummy template quiets anxious XSLT engines.  -->
  <xsl:template match="/html:html" xmlns:html="http://www.w3.org/1999/xhtml">
    <xsl:next-match/>
  </xsl:template>
  
  <xsl:template match="/w:document" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <xsl:next-match/>
  </xsl:template>
  
  <!-- traps the root node of the source and passes it down the chain of transformation references -->
  <xsl:template match="/">
    <xsl:variable name="source" select="."/>
    <xsl:iterate select="$transformation-sequence/*">
      <xsl:param name="sourcedoc" select="$source" as="document-node()"/>
      <xsl:on-completion select="$sourcedoc"/>
      <xsl:next-iteration>
        <xsl:with-param name="sourcedoc">
          <xsl:apply-templates select=".">
            <xsl:with-param name="sourcedoc" select="$sourcedoc"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:next-iteration>
    </xsl:iterate>
  </xsl:template>
  
  <xsl:template match="xsw:transform">
    <xsl:param    name="sourcedoc" as="document-node()"/>
    <xsl:variable name="xslt-spec" select="."/>
    <xsl:variable name="runtime"   select="map {
      'xslt-version'        : if (empty($xslt-spec/@version)) then 2.0 else xs:decimal($xslt-spec/@version),
      'stylesheet-location' : string($xslt-spec),
      'source-node'         : $sourcedoc }" />
    <!-- The function returns a map; primary results are under 'output'
         unless a base output URI is given
         https://www.w3.org/TR/xpath-functions-31/#func-transform -->
    <xsl:sequence select="transform($runtime)?output"/>
  </xsl:template>

  <!-- Not knowing any better, we simply pass along. -->
  <xsl:template match="*">
    <xsl:param    name="sourcedoc" as="document-node()"/>
    <xsl:sequence select="$sourcedoc"/>
  </xsl:template>
  
</xsl:stylesheet>