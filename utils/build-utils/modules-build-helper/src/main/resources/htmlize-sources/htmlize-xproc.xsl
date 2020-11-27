<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
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
	<xsl:variable name="source-uri" select="resolve-uri(pf:relativize-uri($input-uri,$input-base-uri),$output-base-uri)"/>
	
	<xsl:variable name="catalog-xml" select="doc($catalog-xml-uri)"/>
	<xsl:variable name="entry-in-catalog" select="$catalog-xml//cat:uri[resolve-uri(@uri,base-uri(.))=$input-uri]"/>
	
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
						<xsl:apply-templates mode="serialize" select="/*"/>
					</div>
				</body>
			</html>
		</xsl:result-document>
	</xsl:template>

	<xsl:template mode="attribute-value"
	              match="p:import/@href|
	                     p:xslt/p:input[@port='stylesheet']/p:document/@href">
		<a href="{pf:relativize-uri(resolve-uri(.,$source-uri),$output-uri)}">
			<xsl:if test="not(.='http://xmlcalabash.com/extension/steps/library-1.0.xpl')">
				<xsl:attribute name="class" select="'source'"/>
			</xsl:if>
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/*/p:option/p:pipeinfo/pxd:type">
		<xsl:variable name="data-type-xml" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:variable name="id" select="concat('data-type-',parent::*/parent::*/@name)"/>
		<xsl:choose>
			<xsl:when test="$entry-in-catalog[@name or @px:content-type='script']">
				<!--
				    The (possibly inherited) type is defined in a script or a public
				    file. process-catalog will move the definition into a separate file and generate
				    the ID "[root type]-[option name]" for it. Use the same ID to attach the
				    definition to it in the RDF metadata. Also serialize the definition.
				-->
				<span typeof="data-type" id="{$id}" resource="{pf:relativize-uri($source-uri,$output-uri)}#{$id}">
					<link rel="doc" href="#{$id}"/>
					<!-- FIXME: make @type optional -->
					<xsl:if test="not(/*/@type)">
						<xsl:message terminate="yes">missing @type: <xsl:sequence select="/*"/></xsl:message>
					</xsl:if>
					<span property="id"
					      content="{concat(/*/@type,'-',
					                       for $name in parent::*/parent::*/@name return
					                       if (contains($name,':')) then substring-after($name,':') else $name)}">
						<xsl:call-template name="set-property">
							<xsl:with-param name="property" select="'definition'"/>
							<xsl:with-param name="content" select="replace(string-join($data-type-xml/string(),''),'\\','\\\\')"/>
						</xsl:call-template>
					</span>
				</span>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
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
