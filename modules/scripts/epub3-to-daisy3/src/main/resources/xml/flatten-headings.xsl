<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

	<xsl:key name="original-id" match="d:anchor" use="string(@original-id)"/>

	<xsl:template match="/" priority="1">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'heading_'"/>
			<xsl:with-param name="for-elements" select="//levelhd[not(@id)]|
			                                            //hd[not(@id)]|
			                                            //h1[not(@id)]|
			                                            //h2[not(@id)]|
			                                            //h3[not(@id)]|
			                                            //h4[not(@id)]|
			                                            //h5[not(@id)]|
			                                            //h6[not(@id)]"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="/">
		<!--
		    Mapping from IDs of unwrapped elements to IDs of their parent heading elements, as a set
		    of d:anchor elements. (Note that there may be d:anchor with the same @id and different
		    @original-id.)
		-->
		<xsl:variable name="id-mapping">
			<xsl:document>
				<d:fileset>
					<d:file href="{base-uri(/*)}">
						<xsl:apply-templates mode="id-mapping" select="*"/>
					</d:file>
				</d:fileset>
			</xsl:document>
		</xsl:variable>
		<xsl:apply-templates select="/*">
			<xsl:with-param name="id-mapping" tunnel="yes" select="$id-mapping"/>
		</xsl:apply-templates>
		<xsl:result-document href="mapping">
			<xsl:sequence select="$id-mapping"/>
		</xsl:result-document>
	</xsl:template>

	<!--
	    these are the heading elements that navPoint are created from in px:daisy3-create-ncx
	-->
	<xsl:template match="levelhd|hd|h1|h2|h3|h4|h5|h6">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:if test="not(@id)">
				<xsl:call-template name="pf:generate-id"/>
			</xsl:if>
			<xsl:apply-templates mode="flatten"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="a/@href[starts-with(.,'#')]">
		<xsl:param name="id-mapping" tunnel="yes" required="yes"/>
		<xsl:variable name="anchor" as="element(d:anchor)?" select="key('original-id',substring(.,2),$id-mapping)"/>
		<xsl:choose>
			<xsl:when test="exists($anchor)">
				<xsl:attribute name="href" select="concat('#',$anchor/@id)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="flatten" match="*" priority="1">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>

	<xsl:template mode="#default flatten" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="id-mapping" match="*">
		<xsl:apply-templates mode="#current" select="*"/>
	</xsl:template>

	<xsl:template mode="id-mapping" match="levelhd|hd|h1|h2|h3|h4|h5|h6">
		<xsl:variable name="id" as="xs:string">
			<xsl:choose>
				<xsl:when test="@id">
					<xsl:sequence select="@id"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="pf:generate-id"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:for-each select=".//*[@id]">
			<d:anchor original-id="{@id}" id="{$id}"/>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>

