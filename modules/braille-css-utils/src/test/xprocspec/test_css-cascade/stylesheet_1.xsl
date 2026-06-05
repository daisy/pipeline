<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
	<xsl:param name="uppercase" as="xs:string" select="'false'"/>
	<xsl:variable name="_uppercase" as="xs:boolean" select="$uppercase='true'"/>
	<xsl:template match="*">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="text()">
		<xsl:value-of select="if ($_uppercase) then upper-case(.) else ."/>
	</xsl:template>
</xsl:stylesheet>
