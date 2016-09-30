<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:axs="http://www.w3.org/2001/XMLSchema"
	xmlns:aobfl="http://www.daisy.org/ns/2011/obfl"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	xmlns:tmp="http://brailleapps.github.io/ns/dotify/result"
	exclude-result-prefixes="dotify tmp">

	<xsl:import href="dtbook_table_grid.xsl"/>
	<xsl:param name="debug" select="false()" as="axs:boolean"/>
	<xsl:param name="rowspanName" select="'row-span'" as="axs:string"/>
	<xsl:param name="colspanName" select="'col-span'" as="axs:string"/>
	<xsl:param name="table-split-columns" select="8"/>
	<xsl:param name="l10ntablepart" select="'Table part'"/>
	
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dotify:node">
		<aobfl:xml-processor-result>
			<xsl:apply-templates/>
		</aobfl:xml-processor-result>
	</xsl:template>
	
	<xsl:template match="*:table">
		<xsl:variable name="table">
			<xsl:apply-templates select="." mode="splitTable">
				<xsl:with-param name="maxColumns" select="$table-split-columns"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:for-each select="$table/*:table">
			<xsl:apply-templates select="." mode="asBlock"/>
			<xsl:if test="following-sibling::*:table">
				<aobfl:block keep="page" keep-with-next="1"><xsl:value-of select="concat(':: ', $l10ntablepart, ' ')"/><aobfl:leader position="100%" pattern=":"/></aobfl:block>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="aobfl:table" mode="asBlock">
		<xsl:variable name="grid">
			<xsl:apply-templates select="." mode="makeGrid">
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:variable name="sortedGrid">
			<xsl:for-each select="$grid/tmp:cell">
				<xsl:sort select="axs:integer(@row)"/>
				<xsl:sort select="axs:integer(@col)"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:variable>
		<aobfl:block>
			<xsl:apply-templates select="$sortedGrid/tmp:cell[@col=1]">
				<xsl:with-param name="table" select="."/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>

	<xsl:template match="tmp:cell[@col=1]">
		<xsl:param name="table" required="yes"/>
		<aobfl:block>
			<xsl:call-template name="copyCell">
				<xsl:with-param name="cell" select="."/>
				<xsl:with-param name="table" select="$table"/>
			</xsl:call-template>
			<xsl:variable name="cr" select="@row"/>
			<xsl:apply-templates select="following-sibling::tmp:cell[@row=$cr][1]" mode="thisRow">
				<xsl:with-param name="table" select="$table"/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>

	<xsl:template match="tmp:cell[@col!=1]" mode="thisRow">
		<xsl:param name="table" required="yes"/>
		<aobfl:block margin-left="2">
			<xsl:call-template name="copyCell">
				<xsl:with-param name="cell" select="."/>
				<xsl:with-param name="table" select="$table"/>
			</xsl:call-template>
			<xsl:variable name="cr" select="@row"/>
			<xsl:apply-templates select="following-sibling::tmp:cell[@row=$cr][1]" mode="thisRow">
				<xsl:with-param name="table" select="$table"/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>
	
	<xsl:template match="tmp:cell" mode="thisRow">
		<xsl:message terminate="yes">Internal error.</xsl:message>
	</xsl:template>
	
	<xsl:template name="copyCell">
		<xsl:param name="cell" required="yes"/>
		<xsl:param name="table" required="yes"/>
		<!-- Only copy contents once, otherwise leave empty -->
		<xsl:choose>
			<xsl:when test="$cell/@col-offset=0 and $cell/@row-offset=0">
				<xsl:variable name="node" select="$table//*[generate-id()=$cell/@id]"/>
				<xsl:if test="count($node)!=1">Error in stylesheet.</xsl:if>
				<xsl:copy-of select="$node/node()"/>
				<xsl:if test="count($node/node())=0">&#x2014;</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<aobfl:style name="table-cell-continued"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>