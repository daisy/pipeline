<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    exclude-result-prefixes="xs"
    version="2.0">

  <xsl:param name="tmp-ns"/>
  <xsl:param name="tmp-word-tag"/>
  <xsl:param name="tmp-sentence-tag"/>
  <xsl:param name="can-contain-sentences"/>
  <xsl:param name="cannot-be-sentence-child"/>

  <xsl:variable name="ok-parent-list" select="concat(',', $can-contain-sentences, ',')"/>
  <xsl:variable name="no-sent-child" select="concat(',', $cannot-be-sentence-child, ',')"/>

  <!-- Copy the document until a sentence is found. -->

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name() = $tmp-sentence-tag]" priority="2">
    <xsl:choose>
      <xsl:when test="contains($ok-parent-list, concat(',', local-name(..), ','))">
	<xsl:call-template name="new-sent-on-top-of-children">
	  <!-- @xml:lang comes from the Java detection -->
	  <xsl:with-param name="lang" select="@xml:lang"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="node()" mode="split-sentence">
	  <xsl:with-param name="lang" select="@xml:lang"/>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()[contains($ok-parent-list, concat(',', local-name(.), ','))]"
		mode="split-sentence" priority="3">
    <xsl:param name="lang" select="''"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:call-template name="new-sent-on-top-of-children">
	<xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- Sentences forbidden here: let us try the insertion inside the children. -->
  <xsl:template match="node()" mode="split-sentence" priority="1">
    <xsl:param name="lang" select="''"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="split-sentence">
	<xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[local-name() = $tmp-word-tag]" mode="split-sentence" priority="2">
    <xsl:param name="lang" select="''"/>
    <xsl:copy-of select="node()"/> <!-- ignore the tmp:word -->
  </xsl:template>

  <xsl:template match="*[local-name() = $tmp-sentence-tag]" mode="split-sentence" priority="2">
    <xsl:param name="lang" select="''"/>
    <!-- Should not happen. -->
    <xsl:copy-of select="node()"/>
  </xsl:template>

  <xsl:template name="new-sent-on-top-of-children">
    <xsl:param name="lang" select="''"/>
    <xsl:for-each-group select="node()"
			group-adjacent="self::text() or not(contains($no-sent-child, concat(',', local-name(.), ',')))">
      <xsl:choose>
	<xsl:when test="current-grouping-key()">
	  <!-- assuming the tmp words are inserted at the lowest possible level. -->
	  <xsl:element name="{$tmp-sentence-tag}" namespace="{$tmp-ns}">
	    <xsl:if test="$lang != ''">
	      <xsl:attribute namespace="http://www.w3.org/XML/1998/namespace" name="lang">
		<xsl:value-of select="$lang"/>
	      </xsl:attribute>
	    </xsl:if>
	    <xsl:copy-of select="current-group()"/> <!-- including the <tmp:word> nodes. -->
	  </xsl:element>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="current-group()" mode="split-sentence">
	    <xsl:with-param name="lang" select="$lang"/>
	  </xsl:apply-templates>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>


</xsl:stylesheet>

