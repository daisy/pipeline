<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

	<xsl:template match="/smil[not(head)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<head>
				<xsl:call-template name="format"/>
				<xsl:call-template name="timeInThisSmil"/>
			</head>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/smil/head">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:call-template name="format"/>
			<xsl:call-template name="timeInThisSmil"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/smil/head/meta[@name='dc:format']">
		<xsl:call-template name="format"/>
	</xsl:template>

	<xsl:template match="/smil/head/meta[@name=('ncc:timeInThisSmil','ncc:totalElapsedTime')]">
		<!-- normally not contained in input -->
	</xsl:template>

	<xsl:template name="format">
		<meta name="dc:format" content="Daisy 2.02"/>
	</xsl:template>

	<xsl:template name="timeInThisSmil">
		<meta name="ncc:timeInThisSmil" content="{pf:smil-seconds-to-full-clock-value(
		                                            round(
		                                              pf:smil-clock-value-to-seconds(
		                                                /smil/body/seq/@dur)))}"/>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>

