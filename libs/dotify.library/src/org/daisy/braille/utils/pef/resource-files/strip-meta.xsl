<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pef="http://www.daisy.org/ns/2008/pef">

	<xsl:template match="pef:meta|comment()|processing-instruction()"/>
	
	<xsl:template match="*">
		<xsl:call-template name="copy"/>
	</xsl:template>
	
	<xsl:template match="text()[normalize-space()='']"/>
	
	<xsl:template name="copy">
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:sort select="name()"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>