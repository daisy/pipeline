<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
	
	<xsl:param name="ebraille-compatibility" as="xs:string?" static="true"/> <!-- soft|strict -->
	
	<xsl:variable name="css.fileset" select="collection()[2]"/>
	<xsl:variable name="html" select="collection()[position() &gt; 2]"/>
	
	<xsl:template mode="#default add-ids" match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!--
	    Update dc:language
	-->
	<xsl:template match="metadata/dc:language"/>
	<xsl:template match="metadata">
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
	    Don't use dc:rights and dc:publisher of original for eBraille
	-->
	<xsl:template use-when="$ebraille-compatibility"
	              match="metadata/dc:rights[not(@refines)]|
	                     metadata/dc:publisher[not(@refines)]"/>

	<!--
	    Warn about rendition:layout "pre-paginated" and rendition:layout-pre-paginated properties
	-->
	<xsl:template use-when="$ebraille-compatibility='strict'"
	              match="spine/itemref[@property/tokenize(.,'\s+')='rendition:layout-pre-paginated']">
		<xsl:call-template name="pf:warn">
			<xsl:with-param name="msg" select="
			                                   'Spine items with property ''rendition:layout-pre-paginated'' are not allowed by eBraille'"/>
		</xsl:call-template>
		<xsl:next-match/>
	</xsl:template>

	<!--
	    Drop legacy elements (see also upgrade-package-doc.xsl)
	-->
	<xsl:template use-when="$ebraille-compatibility='strict'"
	              match="metadata/meta[@name]|
	                     manifest/item[@media-type='application/x-dtbncx+xml']|
	                     spine/@toc|
	                     guide|
	                     tours"/>
	<!--
	    Add CSS files
	-->
	<xsl:template match="manifest">
		<xsl:variable name="output-base-uri" select="pf:base-uri(/*)"/>
		<xsl:variable name="manifest-with-css">
			<xsl:copy>
				<xsl:apply-templates select="@*|node()"/>
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
	
	<xsl:template mode="add-ids" match="manifest">
		<xsl:call-template name="pf:next-match-with-generated-ids">
			<xsl:with-param name="prefix" select="'item_'"/>
			<xsl:with-param name="for-elements" select="item[not(@id)]"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="add-ids" match="item[not(@id)]">
		<xsl:copy>
			<xsl:call-template name="pf:generate-id"/>
			<xsl:apply-templates mode="add-ids" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
