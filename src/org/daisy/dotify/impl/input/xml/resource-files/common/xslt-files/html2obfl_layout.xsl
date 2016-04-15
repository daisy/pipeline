<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:epub="http://www.idpf.org/2007/ops"
	xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
	exclude-result-prefixes="html epub xs obfl"
	xmlns="http://www.daisy.org/ns/2011/obfl">
	<xsl:import href="html2obfl_base.xsl"/>
	<xsl:import href="book-formats.xsl"/>
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<!-- FIXME: empty-sequence() below is temporary-->
	<xsl:variable name="footnotesInFrontmatter" as="empty-sequence()"/> <!-- //dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]] -->
															  <!-- //dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]] -->
	<xsl:variable name="footnotesNotInFrontmatter" as="empty-sequence()"/> <!-- //dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]] -->
															     <!-- //dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]] -->
	
	<xsl:template match="/">
		<obfl version="2011-1" hyphenate="{$hyphenate}">
			<xsl:attribute name="xml:lang" select="/html:html/@xml:lang"/>
			<xsl:call-template name="insertMetadata"/>
			<xsl:call-template name="insertLayoutMaster"/>
			<xsl:call-template name="insertProcessorRenderer"/>
			<xsl:call-template name="insertTOCVolumeTemplate"/>
			<xsl:call-template name="insertNoteCollection"/>
			<xsl:apply-templates/>
		</obfl>
	</xsl:template>
	
	<xsl:template name="insertLayoutMaster">
		<xsl:copy-of select="obfl:insertLayoutMaster(
			count($footnotesInFrontmatter), 
			count($footnotesNotInFrontmatter))"/>
	</xsl:template>

	<xsl:template name="insertProcessorRenderer">
		<xsl:copy-of select="obfl:insertProcessorRenderer()"/>
	</xsl:template>
	
	<xsl:template name="insertTOCVolumeTemplate">
		<!-- FIXME: Enable toc
		<xsl:variable name="insertToc" select="$toc-depth > 0 and (//dtb:level1[@class='toc'] or //dtb:level1[dtb:list[@class='toc']])" as="xs:boolean"/>
		<xsl:if test="$insertToc">
			<table-of-contents name="full-toc">
				<xsl:apply-templates select="//dtb:level1" mode="toc"/>
			</table-of-contents>
		</xsl:if>
		<xsl:variable name="additionalPreContent"><xsl:if test="$insertToc"><xsl:apply-templates select="//dtb:frontmatter" mode="pre-volume-mode"/></xsl:if></xsl:variable>-->
		<xsl:variable name="insertToc" select="false()" as="xs:boolean"/>
		<xsl:variable name="additionalPreContent" as="empty-sequence()"/>
		<!-- /dtb:dtbook/dtb:book/dtb:frontmatter/dtb:doctitle  -->
		<!-- /dtb:dtbook/dtb:book/dtb:frontmatter/dtb:docauthor -->
		<!-- FIXME: z3998:author seems to be on the form: surname, given name -->
		<xsl:copy-of select="obfl:insertVolumeTemplate(
			//*[@epub:type='fulltitle'][1],
			//*[@epub:type='z3998:author'],
			count($footnotesInFrontmatter),
			count($footnotesNotInFrontmatter),
			$insertToc,
			$additionalPreContent)"/>
	</xsl:template>
	
	<xsl:template name="insertNoteCollection">
		<xsl:if test="count($footnotesInFrontmatter)>0">
			<collection name="footnotes-front">
				<xsl:apply-templates select="$footnotesInFrontmatter" mode="collectNotes">
					<xsl:with-param name="afix">.A</xsl:with-param>
				</xsl:apply-templates>
				<!-- 
				<item id="note1" text-indent="4">1).</item>  -->
			</collection>
		</xsl:if>
		<xsl:if test="count($footnotesNotInFrontmatter)>0">
			<collection name="footnotes">
				<xsl:apply-templates select="$footnotesNotInFrontmatter" mode="collectNotes">
					<xsl:with-param name="afix">.B</xsl:with-param>
				</xsl:apply-templates>
				<!-- 
				<item id="note1" text-indent="4">1).</item>  -->
			</collection>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="html:h1" mode="apply-block-attributes">
		<xsl:attribute name="margin-top"><xsl:choose><xsl:when test="$row-spacing=2">2</xsl:when><xsl:otherwise>3</xsl:otherwise></xsl:choose></xsl:attribute>
		<xsl:attribute name="margin-bottom">1</xsl:attribute>		
		<xsl:if test="$row-spacing=2">
			<xsl:attribute name="border-bottom-style">solid</xsl:attribute>
			<xsl:attribute name="border-align">inner</xsl:attribute>
		</xsl:if>
		<xsl:attribute name="keep">all</xsl:attribute>
		<xsl:attribute name="keep-with-next">1</xsl:attribute>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>		
	</xsl:template>
	
</xsl:stylesheet>
