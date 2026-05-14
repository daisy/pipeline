<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

	<!-- ================ -->
	<!-- Paragraph styles -->
	<!-- ================ -->

	<xsl:template mode="paragraph-style" priority="1" match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6">
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:em">
		<xsl:sequence select="'Emphasis'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:strong">
		<xsl:sequence select="'Strong'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:doctitle">
		<xsl:sequence select="'Title'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:covertitle">
		<xsl:sequence select="'CovertitleDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:prodnote">
		<xsl:sequence select="'Prodnote-RequiredDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:prodnote[@render='optional']">
		<xsl:sequence select="'Prodnote-OptionalDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:sidebar">
		<xsl:sequence select="'Sidebar-RequiredDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:sidebar[@render='optional']">
		<xsl:sequence select="'Sidebar-OptionalDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:sidebar/dtb:hd">
		<xsl:sequence select="'Sidebarheader-RequiredDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:sidebar[@render='optional']/dtb:hd">
		<xsl:sequence select="'Sidebarheader-OptionalDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:bridgehead">
		<xsl:sequence select="'BridgeheadDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:div">
		<xsl:sequence select="'DivDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:blockquote">
		<xsl:sequence select="'BlockquoteDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:blockquote/dtb:author">
		<xsl:sequence select="'Blockquote-AuthorDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:epigraph">
		<xsl:sequence select="'EpigraphDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:epigraph/dtb:author">
		<xsl:sequence select="'Epigraph-AuthorDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:poem">
		<xsl:sequence select="'PoemDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:poem/dtb:title">
		<xsl:sequence select="'Poem-TitleDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:poem/dtb:author">
		<xsl:sequence select="'Poem-AuthorDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:poem/dtb:byline">
		<xsl:sequence select="'Poem-BylineDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:poem/dtb:hd">
		<xsl:sequence select="'Poem-HeadingDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="0.9" match="dtb:author">
		<xsl:sequence select="'AuthorDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="0.9" match="dtb:byline">
		<xsl:sequence select="'BylineDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:dateline">
		<xsl:sequence select="'DatelineDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:address">
		<xsl:sequence select="'AddressDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:dd">
		<xsl:sequence select="'DefinitionDataDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:dt">
		<xsl:sequence select="'DefinitionTermDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:kbd">
		<xsl:sequence select="'KeyboardInputDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:span">
		<xsl:sequence select="'SpanDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:samp">
		<xsl:sequence select="'SampleDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:code">
		<xsl:sequence select="'CodeDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:sent">
		<xsl:sequence select="'SentDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:w">
		<xsl:sequence select="'WordDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:q">
		<xsl:sequence select="'QuotationDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:imggroup/dtb:caption">
		<xsl:sequence select="'Image-CaptionDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:table/dtb:caption">
		<xsl:sequence select="'Table-CaptionDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:cite">
		<xsl:sequence select="'CitationDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:dfn">
		<xsl:sequence select="'DefinitionDAISY'"/>
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:linenum">
		<xsl:sequence select="'LineNumberDAISY'"/>
	</xsl:template>

	<xsl:template mode="paragraph-style" priority="1" match="dtb:pagenum">
	</xsl:template>

	<xsl:template mode="text-style" priority="1" match="dtb:pagenum">
		<xsl:sequence select="'PageNumberDAISY'"/>
	</xsl:template>

	<!--
	    Don't convert dl to list
	-->

	<xsl:template mode="office:text office:annotation text:section" priority="1" match="dtb:dl">
		<xsl:apply-templates mode="#current"/>
	</xsl:template>

	<xsl:template mode="office:text office:annotation text:section" priority="1" match="dtb:dd|dtb:dt">
		<xsl:call-template name="text:p"/>
	</xsl:template>

</xsl:stylesheet>
