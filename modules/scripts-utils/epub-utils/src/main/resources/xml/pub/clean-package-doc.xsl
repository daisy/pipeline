<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<!--
	    Remove OPF items that are not in the fileset
	-->

	<xsl:variable name="fileset" as="element(d:fileset)">
		<xsl:apply-templates mode="absolute-hrefs" select="collection()/d:fileset"/>
	</xsl:variable>

	<xsl:template match="package">
		<xsl:variable name="clean-manifest" as="element(manifest)">
			<xsl:apply-templates mode="manifest" select="manifest"/>
		</xsl:variable>
		<xsl:copy>
			<xsl:apply-templates select="@*|node()">
				<xsl:with-param name="clean-manifest" tunnel="yes" select="$clean-manifest"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template mode="manifest" match="item">
		<xsl:variable name="href" select="resolve-uri(@href,base-uri(.))"/>
		<xsl:if test="$fileset/d:file[@href=$href]">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="manifest">
		<xsl:param name="clean-manifest" as="element(manifest)" tunnel="yes" required="yes"/>
		<xsl:sequence select="$clean-manifest"/>
	</xsl:template>

	<xsl:template match="itemref">
		<xsl:param name="clean-manifest" as="element(manifest)" tunnel="yes" required="yes"/>
		<xsl:if test="$clean-manifest/item[@id=current()/@idref]">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="absolute-hrefs" match="d:file/@href">
		<xsl:attribute name="{name()}" select="resolve-uri(.,base-uri(..))"/>
	</xsl:template>

	<xsl:template mode="#all" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
