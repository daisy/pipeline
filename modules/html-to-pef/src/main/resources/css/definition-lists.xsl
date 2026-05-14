<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<!-- Group dt and dd inside li -->

	<xsl:template match="dl">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:for-each-group select="node()" group-starting-with="dt[preceding-sibling::*[1][self::dd]]">
				<li>
					<xsl:apply-templates select="current-group()"/>
				</li>
			</xsl:for-each-group>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
