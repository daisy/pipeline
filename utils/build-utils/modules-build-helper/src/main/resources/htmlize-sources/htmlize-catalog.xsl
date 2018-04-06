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
	
	<xsl:variable name="output-uri"
	              select="concat(resolve-uri(pf:relativize-uri(base-uri(/*),$input-base-uri),$output-base-uri), '/index.html')"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html vocab="http://www.daisy.org/ns/pipeline/" typeof="source">
				<head>
					<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
					<meta charset="utf-8"/>
					<meta http-equiv="content-language" content="en"/>
					<link rel="stylesheet" type="text/css" href="http://daisy.github.io/pipeline/css/nxml-mode.css"/>
					<link rel="shortcut icon" href="http://www.daisy.org/sites/default/files/favicon_0.ico"/>
					<link rev="doc" href="../{replace(base-uri(/*),'.*/([^/]+)$','$1')}"/>
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
		<span about="../{@uri}">
			<xsl:choose>
				<xsl:when test="@px:script='true'">
					<xsl:attribute name="typeof" select="'script'"/>
				</xsl:when>
				<xsl:when test="@px:data-type='true'">
					<xsl:attribute name="typeof" select="'data-type'"/>
					<xsl:variable name="data-type-doc" select="document(@uri,.)"/>
					<meta property="id" content="{string($data-type-doc/*/@id)}"/>
					<xsl:variable name="data-type-xml" as="node()*">
						<xsl:apply-templates mode="serialize" select="$data-type-doc/*"/>
					</xsl:variable>
					<!--
					    Note: backslashes must be escaped (not sure if this is a bug in the ruby RDF library)
					-->
					<meta property="definition" content="{replace(string-join($data-type-xml/string(),''),'\\','\\\\')}"/>
				</xsl:when>
			</xsl:choose>
			<xsl:next-match/>
		</span>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="cat:uri/@name">
		<a href="../{parent::*/@uri}" class="apidoc">
			<span rel="alias" resource="{.}">
				<xsl:value-of select="."/>
			</span>
		</a>
	</xsl:template>

	<xsl:template mode="attribute-value" match="cat:uri/@uri">
		<a href="../{.}" class="source">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
</xsl:stylesheet>
