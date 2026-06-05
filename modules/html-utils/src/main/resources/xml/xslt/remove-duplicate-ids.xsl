<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                exclude-result-prefixes="#all">

	<xsl:key name="ids" match="*[@id]" use="@id"/>

	<xsl:template match="@id">
		<xsl:if test="key('ids',.)[1] is ..">
			<xsl:sequence select="."/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
