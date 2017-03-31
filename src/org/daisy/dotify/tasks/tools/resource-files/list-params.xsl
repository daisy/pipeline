<?xml version="1.0" encoding="UTF-8"?>
<!-- 
		Lists xslt parameters that have a description attribute (@dotify:desc).
		Roughly respects xslt include/import precedence.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	exclude-result-prefixes="xs xd xsl dotify"
	version="2.0">
	<xsl:output indent="yes"  doctype-system="http://java.sun.com/dtd/properties.dtd" standalone="no"/>

	<xsl:template match="/">
		<properties>
			<xsl:variable name="list">
				<xsl:apply-templates/>
			</xsl:variable>
			<xsl:for-each select="distinct-values($list/*/@key)">
				<xsl:variable name="key" select="."/>
				<xsl:copy-of select="$list/*[@key=$key][last()]"/>
			</xsl:for-each>
		</properties>
	</xsl:template>
	<xsl:template match="/*">
		<!-- The root element -->
		<xsl:apply-templates select="xsl:import"/>
		<xsl:apply-templates select="xsl:include"/>
		<xsl:apply-templates select="xsl:param"/>
	</xsl:template>
	<xsl:template match="xsl:import|xsl:include">
		<xsl:apply-templates select="document(@href)/node()"/>
	</xsl:template>
	<xsl:template match="xsl:param">
		<xsl:if test="@dotify:desc">
			<!-- using tab as field separator, any tabs inside values will be converted to a regular space -->
			<entry key="{@name}"><xsl:value-of select="concat(normalize-space(@dotify:default), '&#0009;', normalize-space(@dotify:values), '&#0009;', normalize-space(@dotify:desc))"/></entry>
		</xsl:if>
	</xsl:template>
	<xsl:template match="node()">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>