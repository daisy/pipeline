<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:dc10="http://purl.org/dc/elements/1.0/"
                xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="oebpackage:manifest/oebpackage:item[@id='ncx']">
		<xsl:copy>
			<xsl:apply-templates  select="@*|node()"/>
			<xsl:attribute name="media-type" select="'application/x-dtbncx+xml'"/>
		</xsl:copy>
	</xsl:template>

	<!-- update dc:Format metadata -->
	<xsl:template match="oebpackage:dc-metadata/dc10:Format/text()">
		<xsl:text>ANSI/NISO Z39.86-2005</xsl:text>
	</xsl:template>

	<!-- update dc namespace from 1.0 to 1.1 -->
	<xsl:template match="oebpackage:dc-metadata">
		<xsl:element namespace="http://openebook.org/namespaces/oeb-package/1.0/" name="{local-name()}">
			<xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="dc10:*">
		<xsl:element namespace="http://purl.org/dc/elements/1.1/" name="dc:{local-name()}">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
