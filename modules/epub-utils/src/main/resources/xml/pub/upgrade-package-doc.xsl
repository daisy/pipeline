<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:import href="opf2-to-opf3-metadata.xsl"/>

	<xsl:param name="compatibility-mode" required="yes"/>

	<xsl:template match="package/@version">
		<xsl:attribute name="version" select="'3.0'"/>
	</xsl:template>

	<xsl:template match="guide">
		<xsl:if test="$compatibility-mode='true'">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="tours"/>

	<xsl:template match="item/@required-namespace|
	                     item/@fallback-style"/>

</xsl:stylesheet>
