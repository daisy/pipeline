<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:include href="htmlize-xproc.xsl"/>
	
	<xsl:template mode="serialize" match="/p:*/p:option[not(@pxd:output=('result','temp') or @pxd:type='anyFileURI')]">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'option'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/p:*/p:input|
	                                      /p:*/p:option[not(@pxd:output=('result','temp')) and @pxd:type='anyFileURI']">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'input'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/p:*/p:output|
	                                      /p:*/p:option[@pxd:output='result']">
		<xsl:call-template name="set-rel">
			<xsl:with-param name="rel" select="'output'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize" match="/p:*/p:option[@pxd:output='temp']">
		<!--
		    ignore
		-->
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/p:*/p:input/@port|
	                                            /p:*/p:output/@port|
	                                            /p:*/p:option/@name">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'id'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/p:*/p:input/@sequence|
	                                            /p:*/p:output/@sequence|
	                                            /p:*/p:option[@pxd:type='anyFileURI']/@pxd:sequence">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'sequence'"/>
			<xsl:with-param name="datatype" select="'xsd:boolean'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/p:*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@required">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'required'"/>
			<xsl:with-param name="datatype" select="'xsd:boolean'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:variable name="STRING_RE">^\s*('([^']*)'|"([^"]*)")\s*$</xsl:variable>
	
	<xsl:template mode="attribute-value" match="/p:*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@select">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'default'"/>
			<xsl:with-param name="content" select="replace(.,$STRING_RE,'$2$3')"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/p:*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI')]/@pxd:data-type|
	                                            /p:*/p:option[not(@pxd:output='result' or @pxd:type='anyFileURI' or @pxd:data-type)]/@pxd:type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'data-type'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/p:*/p:input/@pxd:media-type|
	                                            /p:*/p:output/@pxd:media-type|
	                                            /p:*/p:option[@pxd:output='result' or @pxd:type='anyFileURI']/@pxd:media-type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'media-type'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/p:*/p:option/p:documentation/*[@pxd:role='name']|
	                     /p:*/p:input/p:documentation/*[@pxd:role='name']|
	                     /p:*/p:output/p:documentation/*[@pxd:role='name']">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'name'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="serialize"
	              match="/p:*/p:option/p:documentation/*[@pxd:role='desc']|
	                     /p:*/p:input/p:documentation/*[@pxd:role='desc']|
	                     /p:*/p:output/p:documentation/*[@pxd:role='desc']">
		<xsl:variable name="content" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'desc'"/>
			<xsl:with-param name="content" select="string-join($content/string(),'')"/>
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
