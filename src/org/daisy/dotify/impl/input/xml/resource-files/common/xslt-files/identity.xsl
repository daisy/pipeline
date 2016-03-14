<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	xmlns="http://www.daisy.org/ns/2011/obfl"
	exclude-result-prefixes="dotify">
	<!-- Makes a verbatim copy of the input, which is assumed to be in obfl namespace -->
	
	<xsl:template match="dotify:node">
		<xml-processor-result>
			<xsl:copy-of select="*"/>
		</xml-processor-result>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:copy-of select="*"/>
	</xsl:template>
	
</xsl:stylesheet>