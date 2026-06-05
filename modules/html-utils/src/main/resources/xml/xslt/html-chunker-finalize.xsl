<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:variable name="source" select="collection()[2]"/>
  <xsl:variable name="head" select="$source/html/head"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/html">
    <xsl:copy>
      <xsl:copy-of select="(@* except @xml:base) | namespace::*"/>
      <xsl:apply-templates select="$head">
        <xsl:with-param name="title" select="(((//h1)[1])/string(.),((//h2)[1])/string(.))[1]" tunnel="yes"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="node() except head"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="body">
    <xsl:copy>
      <!-- TODO: try to not "depend" on the TTS namespace here -->
      <xsl:copy-of select="@tts:*|section/@*"/>
      <xsl:apply-templates select="section/node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="title">
    <xsl:param name="title" tunnel="yes" as="xs:string?"/>
    <title>
      <xsl:apply-templates select="@*"/>
      <xsl:value-of select="($title,string(.))[1]"/>
    </title>
  </xsl:template>

</xsl:stylesheet>
