<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:c="http://www.w3.org/ns/xproc-step">
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/c:zipfile">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates select="c:file">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
