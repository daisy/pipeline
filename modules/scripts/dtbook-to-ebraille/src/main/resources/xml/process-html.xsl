<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:param name="stylesheet-links" as="xs:string*" select="()"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Fix aria-label of page breaks
	-->
	<xsl:template match="*[@role='doc-pagebreak']/@aria-label[matches(.,'\.\s*$')]">
		<xsl:attribute name="aria-label" select="replace(.,'\.(\s*$)','$1')"/>
	</xsl:template>

	<!--
	    Add links to CSS
	-->
	<xsl:template match="html:head">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:variable name="base" select="pf:base-uri(/*)"/>
			<xsl:for-each select="$stylesheet-links">
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href" select="pf:relativize-uri(.,$base)"/>
				</link>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
