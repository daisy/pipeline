<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>

	<xsl:variable name="content-doc" as="element()" select="/*"/>
	<xsl:variable name="doc-uri" as="xs:string" select="pf:normalize-uri(base-uri($content-doc))"/>
	<xsl:variable name="pagebreaks-from-nav" as="document-node(element(d:fileset))?" select="collection()[2]"/>
	<xsl:variable name="pagebreaks-from-nav-in-doc" as="element(d:file)?"
	              select="$pagebreaks-from-nav//d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.)))=$doc-uri]"/>

	<xsl:key name="id" match="*[@id]" use="@id"/>

	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="exists($pagebreaks-from-nav-in-doc)">
				<xsl:apply-templates select="/*"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="/*"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*[@id][exists(key('id',@id,$pagebreaks-from-nav-in-doc))]">
		<xsl:variable name="from-nav" as="element(d:anchor)?" select="key('id',@id,$pagebreaks-from-nav-in-doc)"/>
		<xsl:copy>
			<xsl:apply-templates select="@* except @epub:type"/>
			<xsl:if test="not(@title)">
				<xsl:sequence select="$from-nav/@title"/>
			</xsl:if>
			<xsl:attribute name="epub:type"
			               select="string-join(distinct-values((@epub:type/tokenize(.,'\s+')[not(.='')],'pagebreak')),' ')"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
