<?xml version="1.0" encoding="utf-8"?>
<!--
	Add levels
		Version
			2008-10-31

		Description
			Adds a non-breaking space into empty table cells.

		Nodes
			td

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Linus Ericson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	
	<xsl:template match="dtb:td[not(* or text())]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:text>&#160;</xsl:text>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:th[not(* or text())]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:text>&#160;</xsl:text>
		</xsl:copy>
	</xsl:template>
		
</xsl:stylesheet>
