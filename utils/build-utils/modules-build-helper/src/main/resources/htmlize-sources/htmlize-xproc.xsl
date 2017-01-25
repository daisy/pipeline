<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="input-base-uri"/>
	<xsl:param name="output-base-uri"/>
	<xsl:param name="catalog-xml-uri"/>
	
	<xsl:include href="serialize.xsl"/>
	<!--
	    TODO: change to http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl
	-->
	<xsl:include href="../lib/uri-functions.xsl"/>
	
	<xsl:output name="html" method="html"/>
	
	<xsl:variable name="input-uri" select="base-uri(/*)"/>
	<xsl:variable name="output-uri" select="concat(resolve-uri(pf:relativize-uri($input-uri,$input-base-uri),$output-base-uri), '/index.html')"/>
	
	<xsl:variable name="catalog-xml" select="doc($catalog-xml-uri)"/>
	<xsl:variable name="entry-in-catalog" select="$catalog-xml//cat:uri[resolve-uri(@uri,base-uri(.))=$input-uri]"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html vocab="http://www.daisy.org/ns/pipeline/" typeof="source">
				<xsl:variable name="source" select="concat('../',replace($input-uri,'.*/([^/]+)$','$1'))"/>
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
		<a href="../{.}" class="source">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:option/p:pipeinfo/pxd:data-type">
		<xsl:variable name="data-type-xml" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:variable name="id" select="concat('data-type-',parent::*/parent::*/@name)"/>
		<span typeof="data-type" id="{$id}" resource="../{replace($input-uri,'.*/([^/]+)$','$1')}#{$id}">
			<link rel="doc" href="#{$id}"/>
			<xsl:if test="not(@id|child::*/@id)">
				<xsl:if test="not($entry-in-catalog)">
					<xsl:message terminate="yes">Error</xsl:message>
				</xsl:if>
				<xsl:call-template name="set-property">
					<xsl:with-param name="property" select="'id'"/>
					<xsl:with-param name="content" select="concat(/*/@type,'-',parent::*/parent::*/@name)"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:call-template name="set-property">
				<xsl:with-param name="property" select="'definition'"/>
				<xsl:with-param name="content" select="replace(string-join($data-type-xml/string(),''),'\\','\\\\')"/>
			</xsl:call-template>
		</span>
	</xsl:template>
	
	<xsl:template mode="attribute-value"
	              match="/*/p:option/p:pipeinfo/pxd:data-type/@id|
	                     /*/p:option/p:pipeinfo/pxd:data-type/*[not(@id)]/@id">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'id'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="set-rel">
		<xsl:param name="rel" required="yes"/>
		<span rel="{$rel}">
			<xsl:next-match/>
		</span>
	</xsl:template>
	
	<xsl:template name="set-property">
		<xsl:param name="property" required="yes"/>
		<xsl:param name="content" select="string(.)"/>
		<xsl:param name="datatype" select="()"/>
		<span property="{$property}">
			<xsl:if test="exists($datatype)">
				<xsl:attribute name="datatype" select="$datatype"/>
			</xsl:if>
			<xsl:if test="self::* or not($content=string(.))">
				<xsl:attribute name="content" select="$content"/>
			</xsl:if>
			<xsl:next-match/>
		</span>
	</xsl:template>
	
</xsl:stylesheet>
