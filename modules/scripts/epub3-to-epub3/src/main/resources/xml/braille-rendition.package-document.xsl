<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
	
	<xsl:variable name="css.fileset" select="collection()[2]"/>
	<xsl:variable name="html" select="collection()[position() &gt; 2]"/>
	
	<xsl:template mode="#default add-ids" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!--
	    Update dcterms:modified
	-->
	<xsl:template match="opf:metadata/opf:meta[@property='dcterms:modified']">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:value-of select="format-dateTime(adjust-dateTime-to-timezone(current-dateTime(), xs:dayTimeDuration('PT0H')),
			                                      '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="opf:metadata/opf:meta[@name='dcterms:modified']">
		<xsl:copy>
			<xsl:sequence select="@* except @content"/>
			<xsl:attribute name="content"
			               select="format-dateTime(adjust-dateTime-to-timezone(current-dateTime(), xs:dayTimeDuration('PT0H')),
			                                      '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"/>
		</xsl:copy>
	</xsl:template>
	
	<!--
	    Update dc:language
	-->
	<xsl:template match="opf:metadata/dc:language"/>
	<xsl:template match="opf:metadata">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:for-each select="distinct-values($html//@xml:lang)">
				<dc:language>
					<xsl:value-of select="."/>
				</dc:language>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
	<!--
	    Add CSS files
	-->
	<xsl:template match="opf:manifest">
		<xsl:variable name="output-base-uri" select="pf:base-uri(/*)"/>
		<xsl:variable name="manifest-with-css">
			<xsl:copy>
				<xsl:sequence select="@*|node()"/>
				<xsl:for-each select="$css.fileset//d:file">
					<xsl:element name="item" xmlns="http://www.idpf.org/2007/opf">
						<xsl:attribute name="href" select="pf:relativize-uri(
						                                     resolve-uri(@href,base-uri(.)),
						                                     $output-base-uri)"/>
						<xsl:attribute name="media-type" select="'text/css'"/>
					</xsl:element>
				</xsl:for-each>
			</xsl:copy>
		</xsl:variable>
		<xsl:apply-templates mode="add-ids" select="$manifest-with-css"/>
	</xsl:template>
	
	<xsl:template mode="add-ids" match="opf:manifest">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'item_'"/>
			<xsl:with-param name="for-elements" select="opf:item[not(@id)]"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="add-ids" match="opf:item[not(@id)]">
		<xsl:copy>
			<xsl:call-template name="pf:generate-id"/>
			<xsl:apply-templates mode="add-ids" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
