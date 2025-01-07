<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:t="org.daisy.pipeline.braille.css.xpath.StyledText"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl"/>

	<xsl:param name="original-text" as="document-node(element(dtb:dtbook))*"/>
	<xsl:param name="braille-translator" as="xs:string" required="yes"/>

	<!-- pagenum IDs are unique, so no need to incorporate the base URI -->
	<xsl:key name="id" match="dtb:pagenum[@id]" use="@id"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Add original text of page numbers
	-->
	<xsl:template match="html:nav[@role='doc-pagelist']//html:a">
		<xsl:variable name="original-pagenum" as="element(dtb:pagenum)"
		              select="key('id',substring-after(@href,'#'),$original-text)"/>
		<xsl:copy>
			<xsl:attribute name="title" select="string($original-pagenum)"/>
			<xsl:apply-templates select="node()|(@* except title)"/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Drop any periods at the end of page numbers (added by zedai-to-html to help AT)
	-->
	<xsl:template match="html:nav[@role='doc-pagelist']//html:a/text()[matches(.,'\.\s*$')]">
		<xsl:value-of select="replace(.,'\.(\s*$)','$1')"/>
	</xsl:template>

	<!--
	    Insert link to package.opf
	-->
	<xsl:template match="html:head">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<link rel="publication" href="package.opf" type="application/oebps-package+xml"/>
			<xsl:text>
                        </xsl:text>
		</xsl:copy>
	</xsl:template>

	<!--
	    Translate generated headings "Table of contents" and "List of pages" to braille
	-->
	<xsl:template match="html:html/@xml:lang|
	                     html:html/@lang">
		<xsl:attribute name="{name()}" select="if (matches(.,'^[a-zA-Z]{2,8}-Brai'))
		                                       then .
		                                       else replace(.,'^([a-zA-Z]{2,8})(-.+)?$','$1-Brai$2')"/>
	</xsl:template>

	<xsl:template match="html:nav[@role=('doc-toc','doc-pagelist')][html:h1]">
		<xsl:copy>
			<xsl:attribute name="aria-label" select="normalize-space(string(html:h1))"/>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="html:nav[@role=('doc-toc','doc-pagelist')]/html:h1/text()">
		<xsl:variable name="lang" select="replace((//html:html/(@xml:lang|@lang)/string(.),'und')[1],'-Brai','')"/>
		<xsl:value-of select="t:getText(
		                        pf:text-transform(concat('(input:text-css)(output:braille)',
		                                                 $braille-translator,
		                                                 '(document-locale:',$lang,')'),
		                                          t:of(string(.))))"/>
	</xsl:template>

</xsl:stylesheet>
