<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
	exclude-result-prefixes="xs xd"
	version="1.0">
	
	<xsl:strip-space elements="obfl:obfl"/>
	
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:copy-of select="@*[not(local-name()='id')]"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>