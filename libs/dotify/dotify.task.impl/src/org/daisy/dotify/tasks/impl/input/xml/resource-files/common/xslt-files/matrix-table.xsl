<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	xmlns:axs="http://www.w3.org/2001/XMLSchema"
	xmlns:aobfl="http://www.daisy.org/ns/2011/obfl"
	exclude-result-prefixes="axs dotify">

	<xsl:import href="split-table.xsl"/>
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
			<xsl:copy>
				<xsl:copy-of select="@*"/>
				<xsl:apply-templates/>
			</xsl:copy>
			<xsl:if test="following-sibling::*:table">
				<aobfl:block keep="page" keep-with-next="1"><xsl:value-of select="concat(':: ', $l10ntablepart, ' ')"/><aobfl:leader position="100%" pattern=":"/></aobfl:block>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="aobfl:th">
		<aobfl:td>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</aobfl:td>
	</xsl:template>
	
</xsl:stylesheet>