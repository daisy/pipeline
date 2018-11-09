<?xml version="1.0" encoding="UTF-8"?>
<!--
	OBFL whitespace normalizer. DO NOT USE!

	Description
	Removes undesired whitespace that will effect the layout 
	process. Whitespace is often injected by mistake into the 
	input file, e.g. by "Pretty printing".
	
	NOTE: this is an attempt at adding support for style markers. However,
	it did not end up working as well as expected. Therefore, it has been
	replaced by a java implementation. This file has been uploaded in case 
	an XSLT-only version should ever be needed. Some bug fixing is required.

	xml:space="preserve" is supported as per http://www.xmlplease.com/xml/xmlspace/

	Parameters
		None

	Format (input -> output)
		OBFL -> OBFL

	Nodes
		text()
    
	Author: Joel Håkansson
	Version: 2013-08-01
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="no"/>
	<xsl:strip-space elements="*"/>
	<xsl:preserve-space elements="block toc-entry style span"/>
	
	<xsl:variable name="beginWs">^\s+</xsl:variable>
	<xsl:variable name="endWs">\s+$</xsl:variable>

	<!-- Remove whitespace: block, br -->
	<!-- Preserve whitespace: evaluate, page-number, leader -->
	<!-- Move whitespace: span, style, marker, anchor -->
	<xsl:template match="text()[ancestor::*[self::block or self::toc-entry]]">
		<xsl:variable name="context-id"><xsl:value-of select="generate-id((ancestor::*[self::block or self::toc-entry])[1])"/></xsl:variable>
		<xsl:variable name="preceding-text"><xsl:value-of select="(preceding::text()[1])[generate-id((ancestor::*[self::block or self::toc-entry])[1])=$context-id]"/></xsl:variable>
		<xsl:variable name="following-text"><xsl:value-of select="(following::text()[1])[generate-id((ancestor::*[self::block or self::toc-entry])[1])=$context-id]"/></xsl:variable>

		<xsl:choose>
			<xsl:when test="normalize-space()=''">
				<xsl:if test="(preceding-sibling::*[1])[self::leader or self::evaluate or self::page-number or self::span or self::style or self::marker] and
							 (following-sibling::*[1])[self::leader or self::evaluate or self::page-number or self::span or self::style]
							 "><xsl:text> </xsl:text></xsl:if>
				<!-- otherwise, don't output anything -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<!-- Remove whitespaces after block or br -->
					<xsl:when test="(preceding-sibling::*[1])[self::block or self::toc-entry or self::br]"/>
					<!-- Preserve whitespace around leader, evaluate and page-number -->
					<xsl:when test="(preceding-sibling::*[1])[self::leader or self::evaluate or self::page-number] and matches(., $beginWs)"><xsl:text> </xsl:text></xsl:when>
					<!-- Move span whitespace out of node -->
					<xsl:when test="(preceding-sibling::*[1])[self::span or self::style or self::marker or self::anchor] and (matches(., $beginWs) or matches($preceding-text, $endWs))"><xsl:text> </xsl:text></xsl:when>
				</xsl:choose>
				<xsl:value-of select="normalize-space()"/>
				<xsl:choose>
					<!-- Remove whitespaces before block or br -->
					<xsl:when test="(following-sibling::*[1])[self::block or self::toc-entry or self::br]"/>
					<!-- Preserve whitespace around leader, evaluate and page-number -->
					<xsl:when test="(following-sibling::*[1])[self::leader or self::evaluate or self::page-number] and matches(., $endWs)"><xsl:text> </xsl:text></xsl:when>
					<!-- Move span and style whitespace out of node, move marker whitespace -->
					<xsl:when test="(following-sibling::*[1])[self::span or self::style] and (matches(., $endWs) or matches($following-text, $beginWs))"><xsl:text> </xsl:text></xsl:when>
					<!-- Move marker whitespace before marker -->
					<xsl:when test="(following-sibling::*[1])[self::marker or self::anchor]"/>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    
    <xsl:template match="style|span">
   		<xsl:variable name="context-id"><xsl:value-of select="generate-id((ancestor::*[self::block or self::toc-entry])[1])"/></xsl:variable>
		<xsl:variable name="preceding-text"><xsl:value-of select="(preceding::text()[1])[generate-id((ancestor::*[self::block or self::toc-entry])[1])=$context-id]"/></xsl:variable>
		<xsl:variable name="following-text"><xsl:value-of select="(following::text()[1])[generate-id((ancestor::*[self::block or self::toc-entry])[1])=$context-id]"/></xsl:variable>
		<xsl:if test="(preceding-sibling::node())[1][self::span or self::style] and matches($preceding-text, $endWs)"><xsl:text> </xsl:text></xsl:if>
		<xsl:call-template name="copy"/>
		<xsl:if test="(following-sibling::node())[1][self::span or self::style] and normalize-space($following-text) !='' and matches($following-text, $beginWs)">
		<xsl:text> </xsl:text>
		</xsl:if>
    </xsl:template>

	<xsl:template match="*|comment()|processing-instruction()">
		<xsl:call-template name="copy"/>
	</xsl:template>

	<xsl:template name="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
