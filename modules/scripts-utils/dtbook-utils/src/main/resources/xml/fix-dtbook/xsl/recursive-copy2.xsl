<?xml version="1.0" encoding="UTF-8"?>
<!--	
		Recursive copy
		Copies input to output by processing every element.
		Intended to be included in other stylesheets.
-->
<!--
		Joel Håkansson, TPB
		Version 2006-03-24
 -->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="*|comment()|processing-instruction()">
		<xsl:call-template name="copy"/>
	</xsl:template>

	<xsl:template name="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>