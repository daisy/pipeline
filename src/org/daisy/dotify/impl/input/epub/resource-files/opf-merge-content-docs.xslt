<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:html="http://www.w3.org/1999/xhtml"  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="opf html">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:strip-space elements="opf:manifest"/>
	
	<xsl:key name="manifesthref" match="opf:item" use="@href"/>
	<xsl:key name="manifest" match="opf:item" use="@id"/>
	<xsl:key name="spine" match="opf:itemref" use="@idref"/>

	<xsl:template match="/">
		<!-- There must be at least one language, according to http://www.idpf.org/epub/30/spec/epub30-publications.html#sec-metadata-elem -->
		<xsl:variable name="lang" select="/opf:package/opf:metadata/dc:language[1]/text()"/>
		<xsl:if test="count(/opf:package/opf:metadata/dc:language)&gt;1">
			<xsl:message terminate="no">WARNING: Multiple languages detected, using '<xsl:value-of select="$lang"/>'</xsl:message>
		</xsl:if>
		<html xml:lang="{normalize-space($lang)}">
			<!-- Get the head part from the first content document -->
			<xsl:copy-of select="document(
								key('manifest', /opf:package/opf:spine[1]/opf:itemref[1]/@idref)[1]/@href
							)/html:html/html:head"/>
			<body><xsl:apply-templates select="//opf:spine"/></body>
		</html>
	</xsl:template>
	
	<xsl:template match="opf:spine">
		<xsl:for-each select="opf:itemref">
			<xsl:for-each select="key('manifest', @idref)">
<!--				<xsl:if test="not(@properties) or @properties!='nav'"> -->
					<xsl:variable name="content" select="document(@href)"/>
					<!-- Can't use default namespace here, allthough it is also html -->
					<div id="{@href}">
						<xsl:apply-templates select="$content//html:body/node()" mode="html">
							<xsl:with-param name="this" select="/"/>
						</xsl:apply-templates>
					</div>
<!--				</xsl:if> -->
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*" mode="html">
		<xsl:param name="this"/>
		<xsl:copy>
			 <xsl:copy-of select="@*"/>
			 <xsl:apply-templates mode="html">
				 <xsl:with-param name="this" select="$this"/>
			 </xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="html:a" mode="html">
		<xsl:param name="this"/>
		<xsl:variable name="seen">
			<xsl:for-each select="$this//opf:item[@href=current()/@href]">
				<!-- Switching to opf context -->
				<xsl:if test="@media-type='application/xhtml+xml' and count(key('spine', @id))>0">A</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="string-length($seen)>0">
				<xsl:copy>
					<xsl:attribute name="href"><xsl:value-of select="concat('#', @href)"/></xsl:attribute>
					<xsl:copy-of select="@*[not(name()='href')]"/>
					 <xsl:apply-templates mode="html">
						  <xsl:with-param name="this" select="$this"/>
					 </xsl:apply-templates>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					 <xsl:copy-of select="@*"/>
					 <xsl:apply-templates mode="html">
						  <xsl:with-param name="this" select="$this"/>
					 </xsl:apply-templates>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
