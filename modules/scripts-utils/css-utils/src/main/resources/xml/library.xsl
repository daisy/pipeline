<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                version="2.0" exclude-result-prefixes="#all">

	<!--
	    pf:media-query-matches is implemented in Java (org.daisy.pipeline.css.saxon.impl.MediaQueryMatches)
	-->
	<xsl:template name="pf:media-query-matches">
		<xsl:param name="media-query" as="xs:string" required="yes"/>
		<xsl:param name="medium" as="xs:string" required="yes"/>
		<xsl:sequence select="pf:media-query-matches($media-query,$medium)"/>
	</xsl:template>

</xsl:stylesheet>
