<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

	<xsl:variable name="base" select="base-uri(/*)"/>
	<xsl:variable name="filename" select="replace(replace($base,'^.+/([^/]+)$','$1'),'\.[^\.]+$','')"/>

	<xsl:template match="/*">
		<xsl:variable name="fileset" as="element(d:fileset)">
			<d:fileset>
				<xsl:for-each select="collection()[2]/*/*">
					<d:file href="{pf:relativize-uri(resolve-uri(@href,base-uri(.)),$base)}"/>
				</xsl:for-each>
			</d:fileset>
		</xsl:variable>
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'_img_'"/>
			<xsl:with-param name="for-elements" select=".//svg:svg"/>
			<xsl:with-param name="in-use"
			                select="$fileset/d:file/@href[starts-with(.,$filename)]
			                                             [ends-with(.,'.svg')]
			                                       /substring(substring(.,1,string-length(.) - 4),
			                                                  string-length($filename) + 1)
			                                        [matches(.,'^_img_\d+$')]"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="svg:svg">
		<xsl:variable name="id" as="xs:string">
			<xsl:call-template name="pf:generate-id"/>
		</xsl:variable>
		<xsl:variable name="relative-path" select="concat($filename,$id,'.svg')"/>
		<xsl:result-document href="{resolve-uri($relative-path,$base)}">
			<xsl:sequence select="."/>
		</xsl:result-document>
		<img src="{$relative-path}" alt="{(svg:title/string(),svg:desc/string(),@aria-label,'image')[1]}"/>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
