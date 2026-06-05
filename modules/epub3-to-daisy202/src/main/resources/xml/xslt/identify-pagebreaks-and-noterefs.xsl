<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

	<xsl:variable name="pagebreaks-from-nav" as="document-node(element(d:fileset))?" select="collection()[2]"/>

	<xsl:template match="/*" priority="1">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'page_'"/>
			<xsl:with-param name="for-elements" select="//*[self::span|self::div|self::a|self::hr|self::br]
			                                               [@epub:type/tokenize(.,'\s+')='pagebreak']
			                                               [not(*)]
			                                               [not(@id)]"/>
			<xsl:with-param name="in-use" select="//*/@id"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="/*">
		<xsl:apply-templates mode="convert" select="."/>
		<xsl:result-document href="page-and-noteref-list">
			<d:file>
				<xsl:attribute name="href" select="base-uri(/*)"/>
				<xsl:apply-templates mode="list" select="."/>
			</d:file>
		</xsl:result-document>
	</xsl:template>

	<!--
	    Convert to span with class page-normal|page-front|page-special, and make sure it has a text value.
	    This step is not strictly needed, but done to give page numbers the same format as in the NCC.
	-->
	<xsl:template mode="convert"
	              match="*[self::span|self::div|self::a|self::hr|self::br]
	                      [@epub:type/tokenize(.,'\s+')='pagebreak']
	                      [not(*)]">
		<span>
			<xsl:apply-templates mode="#current" select="@* except (@class|@epub:type)"/>
			<xsl:if test="not(@id)">
				<xsl:call-template name="pf:generate-id"/>
			</xsl:if>
			<xsl:variable name="value" as="text()">
				<xsl:choose>
					<xsl:when test="normalize-space(string(.))">
						<xsl:apply-templates mode="#current"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@title"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="types" as="xs:string*" select="@epub:type/tokenize(.,'\s+')[not(.='')]"/>
			<xsl:variable name="classes" as="xs:string*" select="@class/tokenize(.,'\s+')[not(.='')]"/>
			<xsl:variable name="classes" as="xs:string*">
				<xsl:sequence select="$classes[not(.=('page-normal','page-front','page-special'))]"/>
				<xsl:choose>
					<xsl:when test="$classes=('page-normal','page-front','page-special')">
						<xsl:sequence select="$classes[.=('page-normal','page-front','page-special')][1]"/>
					</xsl:when>
					<xsl:when test="matches(string($value),'^[0-9]+$')">
						<xsl:sequence select="'page-normal'"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:sequence select="'page-special'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:attribute name="class" select="string-join($classes,' ')"/>
			<xsl:if test="count($types) &gt; 1">
				<xsl:attribute name="epub:type" select="string-join($types[not(.='pagebreak')],' ')"/>
			</xsl:if>
			<!--
			    Not creating a child text node if there wasn't one in the source HTML, because this
			    could result in an error in augment-smil.xsl if the the page number element is
			    inside a heading element.
			-->
			<!-- <xsl:sequence select="$value"/> -->
			<xsl:apply-templates mode="#current"/>
		</span>
	</xsl:template>

	<xsl:template mode="convert" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="list"
	              match="*[self::span|self::div|self::a|self::hr|self::br]
	                      [@epub:type/tokenize(.,'\s+')='pagebreak']
	                      [not(*)]">
		<xsl:if test="not(exists($pagebreaks-from-nav))">
			<d:anchor>
				<xsl:sequence select="@id"/>
				<xsl:if test="not(@id)">
					<xsl:call-template name="pf:generate-id"/>
				</xsl:if>
				<xsl:variable name="title" as="xs:string">
					<xsl:choose>
						<xsl:when test="normalize-space(string(.))">
							<xsl:sequence select="normalize-space(string(.))"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:sequence select="@title"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:attribute name="title" select="$title"/>
				<xsl:variable name="classes" as="xs:string*" select="@class/tokenize(.,'\s+')[not(.='')]"/>
				<xsl:attribute name="class">
					<xsl:choose>
						<xsl:when test="$classes=('page-normal','page-front','page-special')">
							<xsl:sequence select="$classes[.=('page-normal','page-front','page-special')][1]"/>
						</xsl:when>
						<xsl:when test="matches(string($title),'^[0-9]+$')">
							<xsl:sequence select="'page-normal'"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:sequence select="'page-special'"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</d:anchor>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="list" match="a[@href][@id][@epub:type/tokenize(.,'\s+')='noteref']">
		<d:anchor>
			<xsl:sequence select="@id"/>
			<xsl:attribute name="class" select="'noteref'"/>
		</d:anchor>
	</xsl:template>

	<xsl:template mode="list" match="*">
		<xsl:apply-templates mode="#current" select="*"/>
	</xsl:template>

</xsl:stylesheet>
