<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:html="http://www.w3.org/1999/xhtml"  exclude-result-prefixes="opf html" xmlns="http://www.daisy.org/ns/2011/obfl">
	
	<!-- 2013-09-10: This is a placeholder, the implementation has been started yet. -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:param name="page-width" select="10" as="xs:integer"/>
	<xsl:param name="page-height" select="10" as="xs:integer"/>
	<xsl:param name="inner-margin" select="0" as="xs:integer"/>
	<xsl:param name="outer-margin" select="0" as="xs:integer"/>
	<xsl:param name="row-spacing" select="1" as="xs:decimal"/>
	<xsl:param name="duplex" select="true()" as="xs:boolean"/>
	<xsl:param name="toc-indent-multiplier" select="1"/>
	<xsl:param name="splitterMax" select="10"/>
	
	<xsl:param name="l10nrearjacketcopy" select="'Rear jacket copy'"/>
	<xsl:param name="l10nimagedescription" select="'Image description'"/>
	<xsl:param name="l10ncolophon" select="'Colophon'"/>
	<xsl:param name="l10ncaption" select="'Caption'"/>
	
	<xsl:key name="manifest" match="opf:item" use="@id"/>
	
	<xsl:template match="/">
		<obfl version="2011-1">
			<xsl:attribute name="xml:lang"><xsl:value-of select="/opf:package/@xml:lang"/></xsl:attribute>
			<meta xmlns:dc="http://purl.org/dc/elements/1.1/">
					<xsl:for-each select="opf:package/opf:metadata/dc:*">
						<xsl:element name="{name()}" namespace="http://purl.org/dc/elements/1.1/">
							<xsl:value-of select="text()"/>
						</xsl:element>
					</xsl:for-each>
			</meta>
			<!--
			<head>
				<title><xsl:value-of select="opf:package/opf:metadata/dc:title" /></title>
			</head>-->
			<xsl:call-template name="insertLayoutMaster"/>
			<body>
				<xsl:apply-templates select="//opf:spine"/>
			</body>
		</obfl>
	</xsl:template>
	
	<xsl:template name="insertLayoutMaster">
		<!-- This is copied from dtbook2flow, perhaps it could be imported? -->
		<layout-master name="front" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header>
					<field><string value="&#xA0;&#xA0;"/><current-page style="roman"/></field>
				</header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<field><string value=""/></field>
					<field><current-page style="roman"/></field>
				</header>
				<footer></footer>
			</default-template>
		</layout-master>
		<layout-master name="main" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header>
					<field><string value="&#xA0;&#xA0;"/><current-page style="default"/></field>
					<field>
						<marker-reference marker="pagenum-turn" direction="forward" scope="page-content"/>
						<marker-reference marker="pagenum" direction="backward" scope="sequence"/>
					</field>
				</header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<field><string value="&#xA0;&#xA0;"/>
						<marker-reference marker="pagenum-turn" direction="forward" scope="page-content"/>
						<marker-reference marker="pagenum" direction="backward" scope="sequence"/>
					</field>
					<field><current-page style="default"/></field>
				</header>
				<footer></footer>
			</default-template>
		</layout-master>
		<layout-master name="plain" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<default-template>
				<header><field><string value=""/></field></header>
				<footer></footer>
			</default-template>
		</layout-master>
		<layout-master name="cover" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}" border-style="solid" border-width="1" border-align="outer">
			<default-template>
				<header></header>
				<footer></footer>
			</default-template>
		</layout-master>
	</xsl:template>
	
	<xsl:template match="opf:spine">
		<xsl:for-each select="opf:itemref">
			<xsl:for-each select="key('manifest', @idref)">
				<xsl:if test="not(@properties) or @properties!='nav'">
					<xsl:variable name="content" select="document(@href)"/>
					<!-- Can't use default namespace here, allthough it is also html -->
					<xsl:copy-of select="$content//html:body/node()" />
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>
