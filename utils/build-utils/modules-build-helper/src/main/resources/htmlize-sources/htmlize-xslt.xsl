<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="input-base-uri"/>
	<xsl:param name="output-base-uri"/>
	
	<xsl:include href="serialize.xsl"/>
	<xsl:include href="../lib/uri-functions.xsl"/>
	
	<xsl:output name="html" method="html"/>
	
	<xsl:variable name="input-uri" select="base-uri(/*)"/>
	<xsl:variable name="output-uri" select="concat(resolve-uri(pf:relativize-uri($input-uri,$input-base-uri),$output-base-uri), '/index.html')"/>
	<xsl:variable name="source-uri" select="resolve-uri(pf:relativize-uri($input-uri,$input-base-uri),$output-base-uri)"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html vocab="http://www.daisy.org/ns/pipeline/" typeof="source">
				<head>
					<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
					<meta charset="utf-8"/>
					<meta http-equiv="content-language" content="en"/>
					<link rel="stylesheet" type="text/css" href="http://daisy.github.io/pipeline/css/nxml-mode.css"/>
					<link rel="shortcut icon" href="http://www.daisy.org/sites/default/files/favicon_0.ico"/>
					<link rev="doc" href="{pf:relativize-uri($source-uri,$output-uri)}"/>
				</head>
				<body>
					<div class="code" about="{pf:relativize-uri($source-uri,$output-uri)}">
						<xsl:apply-templates select="/*" mode="serialize"/>
					</div>
				</body>
			</html>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template match="xsl:import/@href|
	                     xsl:include/@href"
	              mode="attribute-value">
		<a href="{pf:relativize-uri(resolve-uri(.,$source-uri),$output-uri)}" class="source">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
</xsl:stylesheet>
