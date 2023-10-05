<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/2001/10/synthesis"
                xpath-default-namespace="http://www.w3.org/2001/10/synthesis"
                exclude-result-prefixes="#all">

	<xsl:output omit-xml-declaration="yes"/>

	<xsl:param name="voice" required="yes" as="xs:string"/>

	<!--
	    Format the SSML according to the Cognitive Speech service's rules:
	    https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-synthesis-markup-structure
	-->

	<xsl:template match="*">
		<speak version="1.0">
			<xsl:sequence select="/*/@xml:lang"/>
			<!-- xml:lang will normally be present on <s> elements, but we don't assume this is always the case -->
			<xsl:if test="not(/*/@xml:lang)">
				<xsl:attribute name="xml:lang" select="'und'"/>
			</xsl:if>
			<voice name="{$voice}">
				<xsl:apply-templates mode="copy" select="."/>
				<break time="250ms"/>
			</voice>
		</speak>
	</xsl:template>

	<xsl:template mode="copy" match="speak">
		<xsl:apply-templates mode="#current" select="node()"/>
	</xsl:template>

	<!-- rename mark to bookmark: not needed: regular SSML marks also supported -->
	<!--
	<xsl:template mode="copy" match="mark">
		<bookmark>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</bookmark>
	</xsl:template>

	<xsl:template mode="copy" match="mark/@name">
		<xsl:attribute name="mark" select="string(.)"/>
	</xsl:template>
	-->

	<!-- unwrap token -->
	<xsl:template mode="copy" match="token">
		<xsl:apply-templates mode="#current" select="node()"/>
	</xsl:template>

	<xsl:template mode="copy" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
