<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all"
                version="2.0">

	<xsl:include href="htmlize-xproc.xsl"/>

	<xsl:template mode="serialize" match="/*">
		<xsl:if test="not($entry-in-catalog/@px:content-type='params')">
			<xsl:message terminate="yes">Error</xsl:message>
		</xsl:if>
		<xsl:next-match/>
	</xsl:template>

	<xsl:template match="/*/p:option" mode="serialize" priority="0.9">
		<span about="{pf:relativize-uri($source-uri,$output-uri)
		             }#{
		              if (contains(@name,':')) then substring-after(@name,':') else @name}">
			<xsl:next-match/>
		</span>
	</xsl:template>
	
	<xsl:template match="/*/p:option[not(@select)]" mode="serialize" priority="1">
		<xsl:if test="not(.='false')">
			<xsl:message terminate="yes">expected @select: <xsl:sequence select="."/></xsl:message>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="attribute-value" match="/*/p:option/@name">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'id'"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="/*/p:option/@pxd:type" mode="serialize" priority="1">
		<xsl:if test="not(parent::*/p:pipeinfo/pxd:type)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="attribute-value" match="/*/p:option/@required">
		<xsl:if test="not(.='false')">
			<xsl:message terminate="yes">expected required='false': <xsl:sequence select=".."/></xsl:message>
		</xsl:if>
	</xsl:template>

	<xsl:variable name="STRING_RE">^\s*('([^']*)'|"([^"]*)")\s*$</xsl:variable>

	<xsl:template mode="attribute-value" match="/*/p:option/@select">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'default'"/>
			<xsl:with-param name="content" select="replace(.,$STRING_RE,'$2$3')"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="attribute-value" match="/*/p:option/@pxd:type">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'data-type'"/>
			<xsl:with-param name="content" select="replace(.,'^xsd?:','')"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template priority="0.6"
	              mode="serialize"
	              match="/*/p:option/p:pipeinfo/pxd:type">
		<span rel="data-type">
			<xsl:next-match/>
		</span>
	</xsl:template>

	<xsl:template mode="serialize"
	              match="/*/p:option/p:documentation/*[@pxd:role='name']">
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'name'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="serialize"
	              match="/*/p:option/p:documentation/*[@pxd:role='desc']">
		<xsl:variable name="content" as="node()*">
			<xsl:apply-templates mode="serialize"/>
		</xsl:variable>
		<xsl:variable name="content" select="string-join($content/string(),'')"/>
		<xsl:call-template name="set-property">
			<xsl:with-param name="property" select="'desc'"/>
			<xsl:with-param name="content" select="if (@xml:space='preserve') then $content else normalize-space($content)"/>
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
