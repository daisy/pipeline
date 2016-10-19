<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:p="http://www.w3.org/ns/xproc"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="input-base-uri"/>
	<xsl:param name="output-base-uri"/>
	
	<xsl:include href="serialize.xsl"/>
	<!--
	    TODO: change to http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl
	-->
	<xsl:include href="../lib/uri-functions.xsl"/>
	
	<xsl:output name="html" method="html"/>
	
	<xsl:variable name="input-uri" select="base-uri(/*)"/>
	<xsl:variable name="output-uri" select="concat(resolve-uri(pf:relativize-uri($input-uri,$input-base-uri),$output-base-uri), '.html')"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html vocab="http://www.daisy.org/ns/pipeline/" typeof="source">
				<xsl:variable name="source" select="pf:relativize-uri(base-uri(/*),$output-uri)"/>
				<head>
					<link rev="doc" href="{$source}"/>
				</head>
				<body>
					<div class="code" about="{$source}">
						<xsl:apply-templates mode="serialize" select="/*"/>
					</div>
				</body>
			</html>
		</xsl:result-document>
	</xsl:template>

	<xsl:template mode="attribute-value"
	              match="p:import/@href|
	                     p:xslt/p:input[@port='stylesheet']/p:document/@href">
		<a href="{pf:relativize-uri(resolve-uri(.,base-uri(/*)),$output-uri)}" class="source">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
</xsl:stylesheet>
