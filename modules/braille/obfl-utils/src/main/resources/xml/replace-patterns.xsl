<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:variable name="context" select="collection()[2]"/>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*[matches(.,'^/.*/$')]">
		<xsl:variable name="self" select="."/>
		<xsl:variable name="corresponding-attribute-in-context" as="attribute()?"
		              select="pxi:element-at-index($context/*,pxi:index-of-element(parent::*))/@*[name()=name($self)]"/>
		<xsl:variable name="pattern" select="substring(.,3,string-length(.)-3)"/>
		<xsl:choose>
			<xsl:when test="$corresponding-attribute-in-context[matches(.,$pattern)]">
				<xsl:attribute name="{name(.)}" select="string($corresponding-attribute-in-context)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:function name="pxi:element-at-index" as="element()?">
		<xsl:param name="base" as="element()?"/>
		<xsl:param name="index" as="xs:integer*"/>
		<xsl:choose>
			<xsl:when test="not(exists($base))"/>
			<xsl:when test="exists($index)">
				<xsl:sequence select="pxi:element-at-index($base/child::*[position()=$index[1]],$index[position()&gt;1])"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$base"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<xsl:function name="pxi:index-of-element" as="xs:integer*">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="for $a in ($element/ancestor-or-self::*[parent::*])
		                      return 1 + count($a/preceding-sibling::*)"/>
	</xsl:function>
	
</xsl:stylesheet>
