<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:brl="http://www.daisy.org/z3986/2009/braille/">
	
	<xsl:template match="brl:*">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="brl:volume">
		<br class="braille-volume-break"/>
	</xsl:template>
	
	<xsl:template match="brl:volume[@brl:grade]">
		<br class="braille-volume-break-grade-{@brl:grade}"/>
	</xsl:template>
	
</xsl:stylesheet>
