<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="xsl xs opf">

	<xsl:param name="update-lang-attributes" as="xs:string" required="yes"/>
	<xsl:param name="update-identifier-in-content-docs" as="xs:string" required="yes"/>
	<xsl:param name="update-title-in-content-docs" as="xs:string" required="yes"/>

	<xsl:variable name="package-doc" as="element(opf:package)" select="collection()[2]/*"/>

	<xsl:variable name="language" as="xs:string?"
	              select="if ($update-lang-attributes='true' and count($package-doc/opf:metadata/dc:language)=1)
	                      then $package-doc/opf:metadata/dc:language
	                      else ()"/>
	<xsl:variable name="identifier" as="xs:string?"
	              select="if ($update-identifier-in-content-docs='true')
	                      then $package-doc/opf:metadata/dc:identifier[@id=$package-doc/@unique-identifier]
	                      else ()"/>
	<xsl:variable name="title" as="xs:string?"
	              select="if ($update-title-in-content-docs='true' and exists($package-doc/opf:metadata/dc:title))
	                      then ($package-doc/opf:metadata/dc:title)[1]
	                      else ()"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="body">
		<xsl:sequence select="."/>
	</xsl:template>

	<xsl:template match="/html">
		<xsl:copy>
			<xsl:sequence select="@* except (@lang|@xml:lang)"/>
			<xsl:choose>
				<xsl:when test="exists($language)">
					<xsl:attribute name="lang" select="$language"/>
					<xsl:attribute name="xml:lang" select="$language"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="@lang|@xml:lang"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not(exists(head)) and (exists($identifier) or exists($title))">
				<xsl:variable name="head" as="element()">
					<head/>
				</xsl:variable>
				<xsl:apply-templates select="$head"/>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/html/head">
		<xsl:copy>
			<xsl:if test="not(exists(title)) and exists($title)">
				<title>{$title}</title>
			</xsl:if>
			<xsl:if test="not(exists(meta[@name='dc:identifier'])) and exists($identifier)">
				<meta name="dc:identifier" content="{$identifier}"/>
			</xsl:if>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/html/head/title">
		<xsl:choose>
			<xsl:when test="exists($title)">
				<xsl:copy>
					<xsl:value-of select="$title"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="/html/head/meta[@name='dc:identifier']">
		<xsl:choose>
			<xsl:when test="exists($identifier) and preceding-sibling::meta[@name='dc:identifier']">
				<!-- skip -->
			</xsl:when>
			<xsl:when test="exists($identifier)">
				<xsl:copy>
					<xsl:sequence select="@name"/>
					<xsl:attribute name="content" select="$identifier"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
