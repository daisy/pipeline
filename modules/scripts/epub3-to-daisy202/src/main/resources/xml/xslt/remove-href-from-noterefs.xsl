<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>

	<!--
	    list of IDs of noterefs contained in this HTML file
	-->
	<xsl:variable name="noteref-list" as="xs:string*">
		<xsl:variable name="base-uri" select="pf:normalize-uri(pf:html-base-uri(/*))"/>
		<xsl:sequence select="collection()/d:fileset/d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.)))=$base-uri]/d:anchor/@id"/>
	</xsl:variable>

	<xsl:template match="a[@href][@id=$noteref-list]">
		<xsl:copy>
			<xsl:sequence select="@* except @href"/>
			<xsl:sequence select="node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
