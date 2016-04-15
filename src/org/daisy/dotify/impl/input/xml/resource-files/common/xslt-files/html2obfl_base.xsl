<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:html="http://www.w3.org/1999/xhtml"
		xmlns:epub="http://www.idpf.org/2007/ops"
		xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
		exclude-result-prefixes="html epub xs obfl"
		xmlns="http://www.daisy.org/ns/2011/obfl">
	<xsl:import href="default-modes.xsl"/>
	<xsl:import href="obfl-functions.xsl"/>
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	
	<xsl:param name="hyphenate" select="true()" as="xs:boolean"/>
	<xsl:param name="page-width" select="10" as="xs:integer"/>
	<xsl:param name="page-height" select="10" as="xs:integer"/>
	<xsl:param name="inner-margin" select="0" as="xs:integer"/>
	<xsl:param name="outer-margin" select="0" as="xs:integer"/>
	<xsl:param name="row-spacing" select="1" as="xs:decimal"/>
	<xsl:param name="duplex" select="true()" as="xs:boolean"/>

	<xsl:template match="/">
		<obfl version="2011-1" hyphenate="{$hyphenate}">
			<xsl:attribute name="xml:lang"><xsl:value-of select="/html:html/@xml:lang"/></xsl:attribute>
			<xsl:call-template name="insertMetadata"/>
			<xsl:call-template name="insertLayoutMaster"/>
			<xsl:variable name="result">
				<xsl:apply-templates/>
			</xsl:variable>
			<xsl:copy-of select="obfl:wrapSequence($result, 'main')"/>
		</obfl>
	</xsl:template>
	
	<!-- display: none -->
	<xsl:template match="html:area | html:base | html:basefont | html:datalist | html:head | html:link | html:meta |
						 html:noembed | html:noframes | html:param | html:rp | html:script | html:source | html:style |
						 html:template | html:track | html:title"></xsl:template>
	<xsl:template match="html:img | html:col | html:colgroup"></xsl:template>	
	<xsl:template match="html:html"><xsl:apply-templates/></xsl:template>

<!-- default templates / -->
	<xsl:template name="insertLayoutMaster">
		<layout-master name="main" page-width="{$page-width}" 
			page-height="{$page-height}" inner-margin="{$inner-margin}"
			outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<default-template>
				<header></header>
				<footer></footer>
			</default-template>
		</layout-master>
	</xsl:template>
	<xsl:template name="insertMetadata"/>
<!-- / default templates -->

<!-- sequence elements / -->
	<xsl:template match="html:body">
		<xsl:apply-templates select="." mode="sequence-mode"/>
	</xsl:template>
	<xsl:template match="html:body" mode="apply-sequence-attributes">
		<xsl:attribute name="master">main</xsl:attribute>
		<xsl:attribute name="initial-page-number">1</xsl:attribute>
	</xsl:template>
<!-- / sequence elements -->

<!-- block elements / -->
	<xsl:template match="html:address | html:blockquote | html:center | html:div | html:figure | html:figcaption | html:footer | html:form | html:header |
		html:legend | html:listing | html:p | html:plaintext | html:pre | html:xmp ">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	
	<xsl:template match="html:list | html:linegroup | html:poem 
								 | html:annotation | html:dl | html:imggroup | html:note">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	<xsl:template match="html:caption | html:h1 | html:h2 | html:h3 | html:h4 | html:h5 | html:h6 | html:li |
		html:bridgehead | html:covertitle | html:docauthor | html:doctitle | html:prodnote | html:hd | html:author | html:line | 
		html:epigraph | html:sidebar | html:byline | html:dateline | html:title">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>

<!-- / block elements -->

<!-- inline elements / -->
	<xsl:template match="html:a | html:cite | html:dfn | html:em | html:i | html:var | html:b | html:strong | html:code  | html:kbd | html:samp | html:tt | html:big | html:small 
		| html:sub | html:sup | html:ruby | html:rb | html:rt | html:rbc | html:rtc |
		html:bdo | html:w |  
								html:sent | html:span | html:acronym | html:abbr | html:q  |
								html:annoref | html:noteref | html:linenum | html:lic | html:dt | html:dd">
		<xsl:apply-templates select="." mode="inline-mode"/>
		<xsl:if test="self::html:noteref">
			<xsl:choose>
				<xsl:when test="starts-with(@idref, '#')"><anchor item="{substring-after(@idref, '#')}"/></xsl:when>
				<xsl:otherwise><xsl:message terminate="no">Only fragment identifier supported: <xsl:value-of select="@idref"/></xsl:message></xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
<!-- / inline elements -->

<!-- special / -->
	<xsl:template match="html:hr">
	<block>
		</block>
	</xsl:template>
	<xsl:template match="html:br"><br/></xsl:template>
	<xsl:template match="html:wbr">&#x200b;</xsl:template>
	
	<!-- Page numbers -->
	<xsl:template match="html:*[@epub:type='pagebreak']" priority="10">
		<marker class="pagenum" value="{@title}"/>
		<xsl:variable name="preceding-pagenum" select="preceding::html:*[@epub:type='pagebreak'][1]"/>
		<!-- This should be true for all normal pages, but false for a sequence of "unnumbered page" or similar. -->
		<xsl:if test="@title!=$preceding-pagenum/@title">
			<xsl:variable name="preceding-marker">
				<xsl:if test="not($preceding-pagenum) or epub:getMatterForElement($preceding-pagenum)=epub:getMatterForElement(.)">
					<xsl:value-of select="$preceding-pagenum/@title"/><xsl:text>&#x2013;</xsl:text>
				</xsl:if>
			</xsl:variable>
			<marker class="pagenum-turn" value="{$preceding-marker}"/>
		</xsl:if>
	</xsl:template>

	<!-- gets epub:type frontmatter/bodymatter/rearmatter for ancestor element -->
	<xsl:function name="epub:getMatterForElement" as="xs:string">
		<xsl:param name="node" as="item()"/>
		<xsl:value-of select="$node/ancestor::*[@epub:type][last()]/tokenize(@epub:type, '\s')[.='bodymatter' or .='frontmatter' or .='rearmatter']"/>
	</xsl:function>
	
	<xsl:function name="epub:types" as="xs:string*">
		<xsl:param name="node" as="element()"/>
		<xsl:sequence select="tokenize($node/@epub:type, '\s+')" />
	</xsl:function>
	
	<xsl:template match="html:table">
		<table table-col-spacing="1">
			<xsl:choose>
				<xsl:when test="html:thead"> 
					<thead>
						<xsl:apply-templates select="html:thead"/>
					</thead>
					<tbody>
						<xsl:apply-templates select="html:tbody/html:tr" mode="matrixRow"/>
						<xsl:apply-templates select="html:tr" mode="matrixRow"/>
						<!-- pagenums are moved after the table -->
						<xsl:apply-templates select="html:tfoot/html:tr" mode="matrixRow"/>
					</tbody>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="html:tbody/html:tr" mode="matrixRow"/>
					<xsl:apply-templates select="html:tr" mode="matrixRow"/>
					<!-- pagenums are moved after the table -->
					<xsl:apply-templates select="html:tfoot/html:tr" mode="matrixRow"/>
				</xsl:otherwise>
			</xsl:choose>
		</table>
	</xsl:template>
	
	<xsl:template match="html:tr" mode="matrixRow">
		<tr>
			<xsl:apply-templates mode="matrixCell"/>
		</tr>
	</xsl:template>
	
	<xsl:template match="html:td | html:th" mode="matrixCell">
		<td>
			<xsl:if test="@colspan">
				<xsl:attribute name="col-span" select="@colspan"/>
			</xsl:if>
			<xsl:if test="@rowspan">
				<xsl:attribute name="row-span" select="@rowspan"/>
			</xsl:if>
			<xsl:apply-templates/>
		</td>
	</xsl:template>
<!-- / special -->

</xsl:stylesheet>
