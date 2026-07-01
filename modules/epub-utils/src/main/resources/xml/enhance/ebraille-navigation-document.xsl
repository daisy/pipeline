<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml">

	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:param name="package-document-base" as="xs:string" required="yes"/>

	<xsl:param name="output-base-uri" as="xs:string" select="base-uri(/*)"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Add link to package document (assumes there is a head element)
	-->
	<xsl:template match="head">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<link rel="publication" href="{pf:relativize-uri($package-document-base,$output-base-uri)}"
			      type="application/oebps-package+xml"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Ensure role attribute on "toc" and "page-list" nav elements
	-->
	<xsl:template match="nav[@epub:type='toc'][not(@role='doc-toc')]">
		<xsl:copy>
			<xsl:attribute name="role" select="'doc-toc'"/>
			<xsl:apply-templates select="(@* except @role)|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="nav[@epub:type='page-list'][not(@role='doc-pagelist')]">
		<xsl:copy>
			<xsl:attribute name="role" select="'doc-pagelist'"/>
			<xsl:apply-templates select="(@* except @role)|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Save original text of page numbers, which we'll restore after braille transcription
	-->
	<xsl:template match="nav[@epub:type='page-list']//a">
		<xsl:copy>
			<xsl:attribute name="__original-title__" select="string(.)"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
