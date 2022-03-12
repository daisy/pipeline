<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns="http://www.daisy.org/ns/2008/pef"
                xpath-default-namespace="http://www.daisy.org/ns/2008/pef"
                exclude-result-prefixes="#all">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="page">
		<xsl:sequence select="."/>
	</xsl:template>

	<xsl:template match="section[not(following-sibling::*)]/page[not(following-sibling::*)]">
		<xsl:next-match/>
		<xsl:variable name="pages-in-section" as="xs:integer" select="count(../page)"/>
		<xsl:variable name="last-page-empty" as="xs:boolean" select="string-length(replace(string-join(row/string(),''),'⠀',''))=0"/>
		<xsl:variable name="duplex" as="xs:boolean" select="(ancestor::*[@duplex])[last()]/@duplex='true'"/>
		<xsl:if test="not($last-page-empty) and $duplex and ($pages-in-section mod 2 = 0)">
			<page/>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
