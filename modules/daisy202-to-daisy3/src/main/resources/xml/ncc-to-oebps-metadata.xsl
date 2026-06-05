<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns="http://openebook.org/namespaces/oeb-package/1.0/"
                exclude-result-prefixes="html xs">

	<xsl:template match="/html:html">
		<metadata>
			<xsl:apply-templates select="html:head"/>
		</metadata>
	</xsl:template>

	<xsl:template match="/html:html/html:head">
		<dc-metadata xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/">
			<xsl:namespace name="dc">http://purl.org/dc/elements/1.1/</xsl:namespace>
			<dc:Title>
				<!-- mandatory in NCC -->
				<xsl:value-of select="html:title"/>
			</dc:Title>
			<dc:Publisher>
				<!-- mandatory in NCC -->
				<xsl:value-of select="html:meta[@name='dc:publisher']/@content"/>
			</dc:Publisher>
			<dc:Date>
				<!-- mandatory in NCC -->
				<!-- assuming format starts with yyyy-mm-dd -->
				<xsl:value-of select="substring(html:meta[@name='dc:date']/@content,1,10)" />
			</dc:Date>
			<dc:Language>
				<!-- mandatory in NCC -->
				<xsl:value-of select="html:meta[@name='dc:language']/@content"/>
			</dc:Language>
			<xsl:apply-templates mode="dc-metadata" select="*/@name"/>
		</dc-metadata>
		<x-metadata>
			<xsl:apply-templates mode="x-metadata" select="*/@name"/>
		</x-metadata>
	</xsl:template>

	<!-- dc-metadata optional -->

	<xsl:template mode="dc-metadata x-metadata" match="@name"/>

	<xsl:template mode="dc-metadata" match="@name[.='dc:creator']">
		<dc:Creator><xsl:value-of select="../@content" /></dc:Creator>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:subject']">
		<dc:Subject><xsl:value-of select="../@content" /></dc:Subject>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:contributor']">
		<dc:Contributor><xsl:value-of select="../@content" /></dc:Contributor>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:source']">
		<dc:Source><xsl:value-of select="../@content" /></dc:Source>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:coverage']">
		<dc:Coverage><xsl:value-of select="../@content" /></dc:Coverage>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:description']">
		<dc:Description><xsl:value-of select="../@content" /></dc:Description>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:type']">
		<dc:Type><xsl:value-of select="../@content" /></dc:Type>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:relation']">
		<dc:Relation><xsl:value-of select="../@content" /></dc:Relation>
	</xsl:template>

	<xsl:template mode="dc-metadata" match="@name[.='dc:rights']">
		<dc:Rights><xsl:value-of select="../@content" /></dc:Rights>
	</xsl:template>

	<!-- x-metadata optional -->

	<xsl:template mode="x-metadata" match="@name[.='ncc:sourceDate']">
		<meta name="dtb:sourceDate" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:sourceEdition']">
		<meta name="dtb:sourceEdition" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:sourcePublisher']">
		<meta name="dtb:sourcePublisher" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:sourceRights']">
		<meta name="dtb:sourceRights" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:sourceTitle']">
		<meta name="dtb:sourceTitle" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:producer']">
		<meta name="dtb:producer" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:narrator']">
		<meta name="dtb:narrator" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:producedDate']">
		<meta name="dtb:producedDate" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:revision']">
		<meta name="dtb:revision" content="{../@content}" />
	</xsl:template>

	<xsl:template mode="x-metadata" match="@name[.='ncc:revisionDate']">
		<meta name="dtb:revisionDate" content="{../@content}" />
	</xsl:template>

</xsl:stylesheet>
