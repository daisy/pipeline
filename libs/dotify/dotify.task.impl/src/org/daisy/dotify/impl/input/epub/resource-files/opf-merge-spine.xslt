<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:html="http://www.w3.org/1999/xhtml"  exclude-result-prefixes="opf html">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:strip-space elements="opf:manifest"/>

	<xsl:param name="content" select="'package.opf.html'"/>
	
	<xsl:key name="manifest" match="opf:item" use="@id"/>
	<xsl:key name="spine" match="opf:itemref" use="@idref"/>

	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:call-template name="copy"/>
	</xsl:template>
	
	<xsl:template name="copy">
		<xsl:copy>
			 <xsl:copy-of select="@*"/>
			 <xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="opf:manifest">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:element name="item" namespace="http://www.idpf.org/2007/opf">
				<xsl:attribute name="id">content</xsl:attribute>
				<xsl:attribute name="href"><xsl:value-of select="$content"/></xsl:attribute>
				<xsl:attribute name="media-type">application/xhtml+xml</xsl:attribute>
			</xsl:element>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="opf:item">
		<xsl:if test="count(key('spine', @id))=0">
			<xsl:call-template name="copy"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="opf:spine">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<!-- We just want one -->
			<xsl:element name="itemref" namespace="http://www.idpf.org/2007/opf">
				<xsl:attribute name="linear">yes</xsl:attribute>
				<xsl:attribute name="idref">content</xsl:attribute>
			</xsl:element>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
