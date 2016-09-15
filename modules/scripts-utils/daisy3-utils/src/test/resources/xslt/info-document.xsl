<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		exclude-result-prefixes="#all" version="2.0">

  <xsl:function name="dt:smil">
    <xsl:param name="node"/>
    <xsl:value-of select="substring-before($node/@smilref, '#')"/>
  </xsl:function>

  <xsl:template match="/">
    <d:info>
      <xsl:apply-templates select="*"/>
    </d:info>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates select="." mode="check-linearity"/>
    <xsl:apply-templates select="." mode="check-children"/>
    <xsl:apply-templates select="." mode="check-id"/>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="node()" mode="check-linearity">
  </xsl:template>
  <xsl:template match="*[@smilref]" mode="check-linearity">
    <xsl:variable name="smil" select="dt:smil(.)"/>
    <xsl:variable name="status">
      <xsl:choose>
  	<xsl:when test="dt:smil(preceding::*[@smilref][1]) != $smil
  			and (preceding::*[dt:smil(@smilref) = $smil])">
  	  <xsl:value-of select="'not linear'"/>
  	</xsl:when>
  	<xsl:otherwise>
  	  <xsl:value-of select="'ok'"/>
  	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <d:smilref smilref="{@smilref}" id="{@id}" status="{$status}"/>
  </xsl:template>

  <xsl:template match="node()" mode="check-id">
  </xsl:template>
  <xsl:template match="*[@smilref]" mode="check-id">
    <xsl:variable name="smil" select="dt:smil(.)"/>
    <xsl:variable name="status">
      <xsl:choose>
  	<xsl:when test="@id">
  	  <xsl:value-of select="'ok'"/>
  	</xsl:when>
  	<xsl:otherwise>
  	  <xsl:value-of select="'no id'"/>
  	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <d:idcheck smilref="{@smilref}" status="{$status}"/>
  </xsl:template>

  <xsl:template match="node()" mode="check-children">
  </xsl:template>
  <xsl:template match="*[not(descendant::*[starts-with(local-name(), 'level')])]" mode="check-children">
    <xsl:variable name="status">
      <xsl:choose>
  	<xsl:when test="count(distinct-values(//*[@smilref]/dt:smil(@smilref))) > 1">
  	  <xsl:value-of select="'different smils'"/>
  	</xsl:when>
  	<xsl:otherwise>
  	  <xsl:value-of select="'ok'"/>
  	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <d:children node="{local-name()}" id="{if (@id) then @id else 'none'}" status="{$status}"/>
  </xsl:template>

</xsl:stylesheet>
