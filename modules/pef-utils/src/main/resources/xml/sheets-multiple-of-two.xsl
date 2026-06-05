<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns="http://www.daisy.org/ns/2008/pef"
                xpath-default-namespace="http://www.daisy.org/ns/2008/pef"
                exclude-result-prefixes="#all">

	<!--
	    Insert empty pages if number of sheets in volume is not divisible by two
	-->

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
		<xsl:variable name="sheets-in-volume" as="xs:integer" select="sum(for $section in ../../section return
		                                                                  if (($section/ancestor-or-self::*[@duplex])[last()]/@duplex='true')
		                                                                  then xs:integer(ceiling(count($section/page) div 2.))
		                                                                  else count(page))"/>
		<xsl:if test="not($sheets-in-volume mod 2 = 0)">
			<xsl:variable name="duplex" as="xs:boolean" select="(ancestor::*[@duplex])[last()]/@duplex='true'"/>
			<xsl:if test="$duplex">
				<xsl:variable name="pages-in-section" as="xs:integer" select="count(../page)"/>
				<xsl:if test="$pages-in-section mod 2 = 1">
					<page/>
				</xsl:if>
			</xsl:if>
			<page/>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
