<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="input-base-uri"/>
	<xsl:param name="output-base-uri"/>
	
	<xsl:include href="serialize.xsl"/>
	<xsl:include href="../lib/uri-functions.xsl"/>
	
	<xsl:output name="html" method="html"/>
	
	<xsl:variable name="output-uri" select="concat(resolve-uri(pf:relativize-uri(base-uri(/*),$input-base-uri),$output-base-uri), '.html')"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html vocab="http://www.daisy.org/ns/pipeline/" typeof="source">
				<head>
					<link rev="doc" href="{pf:relativize-uri(base-uri(/*),$output-uri)}"/>
				</head>
				<body>
					<div class="code">
						<xsl:apply-templates mode="serialize" select="/*"/>
					</div>
				</body>
			</html>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template mode="serialize" match="cat:uri">
		<span about="{pf:relativize-uri(resolve-uri(@uri,base-uri(/*)),$output-uri)}">
			<xsl:if test="@px:script='true'">
				<xsl:attribute name="typeof" select="'script'"/>
			</xsl:if>
			<xsl:next-match/>
		</span>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="cat:uri/@name">
		<span property="alias">
			<xsl:value-of select="."/>
		</span>
	</xsl:template>

	<xsl:template mode="attribute-value" match="cat:uri/@uri">
		<a href="{pf:relativize-uri(resolve-uri(.,base-uri(/*)),$output-uri)}" class="apidoc">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
</xsl:stylesheet>
