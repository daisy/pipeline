<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
   exclude-result-prefixes="#all">
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <!-- XSweet: rewrites CSS, doing its best to promote CSS settings from style attributes to classes -->
  <!-- Input: An HTML Typescript file. -->
  <!-- Output: A copy, except rewritten wrt use of @style and @class. -->
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- Drop on default traversal -->
  <xsl:template match="@style"/>
  
  <xsl:template match="*[matches(@style,'\S')]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="class">
        <xsl:value-of select="@class"/>
        <xsl:for-each select="@style/../@class"><xsl:text> </xsl:text></xsl:for-each>
        <xsl:apply-templates select="@style" mode="styleClass"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="head">
    <xsl:variable name="abstracted-css">
      <xsl:call-template name="rewrite-css-styles"/>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates/>
      <xsl:if test="matches($abstracted-css,'\S')">
        <style type="text/css">
          <xsl:sequence select="$abstracted-css"/>
        </style>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template name="rewrite-css-styles">
    <!-- Note that we depend on styles being in a regular order. Note they are sorted when
         they are filtered by scrub.xsl in a previous step. -->
    <xsl:for-each-group select="//@style" group-by="string(.)">
      <xsl:text>&#xA;.</xsl:text>
      <xsl:apply-templates select="." mode="styleClass"/>
      <xsl:text> { </xsl:text>
      <xsl:value-of select="current-grouping-key()"/>
      <xsl:text> }</xsl:text>      
    </xsl:for-each-group>
  </xsl:template>
  
  <!-- Emits a string translating a value sequence into a @class-compatible string --> 
  <xsl:template match="@style" mode="styleClass">
    <xsl:variable name="props" select="tokenize(., '\s*;\s*')"/>
    <xsl:value-of>
      <xsl:text>xsw_</xsl:text>
      <!-- Since we're looking at a sequence of strings, we can't write path expressions (in 2.0). -->
      <xsl:for-each select="$props[starts-with(., 'text-align:')]">
        <xsl:sequence select="replace(., '(^text-align|[:\s\.])', '')"/>
      </xsl:for-each>
      <xsl:for-each select="$props[starts-with(., 'margin-')]">
        <xsl:text>margin</xsl:text>
        <xsl:sequence select="replace(., '(^margin-|[:\s\.])', '')"/>
      </xsl:for-each>
      <xsl:for-each select="$props[starts-with(., 'padding-')]">
        <xsl:text>pad</xsl:text>
        <xsl:sequence select="replace(., '(^padding-|[:\s\.])', '')"/>
      </xsl:for-each>
      <xsl:if test="some $p in $props satisfies matches($p,'^font-(family|size)')">font</xsl:if>
      <xsl:for-each select="$props[starts-with(., 'font-family:')]">
        <xsl:sequence select="replace(., '(^font-family:|\C)', '')"/>
      </xsl:for-each>
      <xsl:for-each select="$props[starts-with(., 'font-size:')]">
        <xsl:sequence select="replace(., '(^font-size:|\C)', '')"/>
      </xsl:for-each>
      <xsl:for-each select="$props[starts-with(., 'text-indent:')]">
        <xsl:sequence select="replace(., '(^text-|:|\C)', '')"/>
      </xsl:for-each>
      <!--<xsl:for-each select="$props[matches(., '^border-')]">
        <xsl:variable name="prop" select="tokenize(.,'\s*:\s*')[1]"/>
        <xsl:variable name="val"  select="tokenize(.,'\s*:\s*')[2]"/>
        <xsl:sequence select="concat(replace($prop, '(\w)\w*\-', '$1-'),': ',$val)"/>
      </xsl:for-each>-->
      <xsl:for-each select="$props[matches(., '^\-?xsweet-')]">
        <xsl:sequence select="replace(., '(^-?xsweet-|:|\C|\-)', '')"/>
      </xsl:for-each>
      <xsl:if test="$props = 'font-weight: bold'">bold</xsl:if>
      <xsl:if test="$props = 'font-style: italic'">italic</xsl:if>
      <xsl:if test="$props = 'text-decoration: underline'">underline</xsl:if>
      <xsl:if test="$props = 'font-variant: small-caps'">smallcaps</xsl:if>
      <xsl:for-each select="$props[starts-with(., 'color:')]">
        <xsl:sequence select="replace(., '^color:|\C', '')"/>
      </xsl:for-each>
    </xsl:value-of>
  </xsl:template>
  
</xsl:stylesheet>