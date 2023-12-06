<?xml version="1.0" encoding="UTF-8"?>
<!--
    
    Version
    2008-05-09
    
    Description
    Contains localized strings for use in other stylesheets.
    
    Nodes
    *
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Romain Deltour, DAISY
	
-->
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2.0">
	
	<xsl:template match="*" mode="localizedHeading">
		<xsl:choose>
			<xsl:when test="lang('ch')">
				<xsl:text>空的标题</xsl:text>
			</xsl:when>
			<xsl:when test="lang('de')">
				<xsl:text>Ohne Überschrift</xsl:text>
			</xsl:when>
			<xsl:when test="lang('es')">
				<xsl:text>Título vacío</xsl:text>
			</xsl:when>
			<xsl:when test="lang('fr')">
				<xsl:text>Titre vide</xsl:text>
			</xsl:when>
			<xsl:when test="lang('it')">
				<xsl:text>Intestazione vuota</xsl:text>
			</xsl:when>
			<xsl:when test="lang('ja')">
				<xsl:text>空のヘッディング</xsl:text>
			</xsl:when>
			<xsl:when test="lang('nl')">
				<xsl:text>Lege rubriek</xsl:text>
			</xsl:when>
			<xsl:when test="lang('pt')">
				<xsl:text>Título vazio</xsl:text>
			</xsl:when>
			<xsl:when test="lang('sv')">
				<xsl:text>Tom Rubrik</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>Empty Heading</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*" mode="localizedPageBreak">
		<xsl:choose>
			<xsl:when test="lang('en')">
				<xsl:text>Page break</xsl:text>
			</xsl:when>
			<xsl:when test="lang('fr')">
				<xsl:text>Saut de Page</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>Page break</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
