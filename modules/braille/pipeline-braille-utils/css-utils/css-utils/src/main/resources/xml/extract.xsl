<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="attribute-name" required="yes"/>
	
	<xsl:include href="library.xsl"/>
	
	<xsl:template match="/">
		<xsl:apply-templates select="/*"/>
		<xsl:result-document href="irrelevant">
			<c:result content-type="text/plain">
				<xsl:variable name="styles" as="element()*">
					<xsl:for-each select="//*/@*[local-name()=$attribute-name and namespace-uri()='']">
						<xsl:variable name="id" select="(parent::*/@id/string(.),parent::*/@xml:id/string(.),parent::*/generate-id(.))[1]"/>
						<css:rule selector="#{$id}" style="{.}"/>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="styles" as="element()*">
					<xsl:for-each select="distinct-values($styles/@style)">
						<xsl:variable name="style" as="xs:string" select="."/>
						<css:rule selector="{string-join($styles[@style=$style]/@selector,', ')}" serial="{$style}">
							<xsl:sequence select="css:deep-parse-stylesheet($style)"/>
						</css:rule>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="styles" as="element()*">
					<xsl:apply-templates mode="serialize-page-styles" select="$styles"/>
				</xsl:variable>
				<xsl:variable name="page-styles" as="element()*">
					<xsl:for-each select="distinct-values($styles//css:rule[@selector='@page']/@serial)">
						<xsl:variable name="style" as="xs:string" select="."/>
						<xsl:variable name="name" select="concat('page-',position())"/>
						<xsl:for-each select="$styles//css:rule[@selector='@page'][@serial=$style][1]">
							<xsl:copy>
								<xsl:attribute name="name" select="$name"/>
								<xsl:attribute name="selector" select="concat('@page ',$name)"/>
								<xsl:sequence select="@* except @selector"/>
								<xsl:sequence select="node()"/>
							</xsl:copy>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="styles" as="element()*">
					<xsl:apply-templates mode="substitute-named-pages" select="$styles">
						<xsl:with-param name="page-styles" tunnel="yes" select="$page-styles"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:value-of select="css:serialize-stylesheet(($page-styles,$styles),(),1,'&#x9;')"/>
				<xsl:text>&#xa;</xsl:text>
			</c:result>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template mode="serialize-page-styles" match="css:rule[@selector='@page']">
		<xsl:copy>
			<xsl:attribute name="serial" select="css:serialize-stylesheet(.)"/>
			<xsl:sequence select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template mode="substitute-named-pages" match="css:rule[@selector='@page']">
		<xsl:param name="page-styles" as="element()*" tunnel="yes" required="yes"/>
		<xsl:variable name="serial" as="xs:string" select="@serial"/>
		<css:property name="page" value="{$page-styles[@serial=$serial]/@name}"/>
	</xsl:template>
	
	<xsl:template match="*[not(@id)][@*[local-name()=$attribute-name and namespace-uri()='']]">
		<xsl:copy>
			<xsl:attribute name="id" select="(@xml:id/string(.),generate-id(.))[1]"/>
			<!--
			    XML elements may only have a single attribute of type ID
			-->
			<xsl:apply-templates mode="#current" select="@* except @xml:id"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|node()" mode="#default substitute-named-pages serialize-page-styles">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
