<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:_xsl="http://www.w3.org/1999/XSL/TransformAlias"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
	exclude-result-prefixes="_xsl">
	
	<xsl:namespace-alias stylesheet-prefix="_xsl" result-prefix="xsl"/>
	
	<xsl:output method="xml" encoding="utf-8"/>
	
	<xsl:param name="predicate" as="xs:string" select="'@*/*'"/>

	<xsl:variable name="match"
		select="concat('normalize-space(.)!=&quot;&quot; and ', $predicate)"/>
	<xsl:variable name="transition-to"
		select="concat('not(preceding::text()[normalize-space(.)!=&quot;&quot;][1][', $predicate, '])')"/>
	<xsl:variable name="transition-from"
		select="concat('not(following::text()[normalize-space(.)!=&quot;&quot;][1][', $predicate, '])')"/>

	<xsl:template match="/">
		
		<_xsl:stylesheet version="2.0">
			
			<!-- FIXME ignore elements with display:none -->
			
			<_xsl:output method="xml" encoding="utf-8"/>
			
			<_xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
			
			<_xsl:param name="announcement" as="xs:string" select="''"/>
			<_xsl:param name="deannouncement" as="xs:string" select="''"/>
			
			<_xsl:template match="text()">
				<_xsl:choose>
					<_xsl:when test="{$match}">
						<_xsl:if test="{$transition-to}">
							<_xsl:value-of select="$announcement"/>
						</_xsl:if>
						<_xsl:sequence select="."/>
						<_xsl:if test="{$transition-from}">
							<_xsl:value-of select="$deannouncement"/>
						</_xsl:if>
					</_xsl:when>
					<_xsl:otherwise>
						<_xsl:sequence select="."/>
					</_xsl:otherwise>
				</_xsl:choose>
			</_xsl:template>
			
			<_xsl:template match="*">
				<_xsl:copy>
					<_xsl:apply-templates select="@*|node()"/>
				</_xsl:copy>
			</_xsl:template>
			
			<_xsl:template match="@*|comment()|processing-instruction()">
				<_xsl:sequence select="."/>
			</_xsl:template>
		
		</_xsl:stylesheet>
	</xsl:template>
</xsl:stylesheet>
