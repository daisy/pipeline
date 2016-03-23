<?xml version="1.0" encoding="utf-8"?>
<?xslt-doc-file doc-files/dtb2obfl.html?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb xs" xmlns="http://www.daisy.org/ns/2011/obfl">

	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	
	<xsl:param name="hyphenate" select="true()" as="xs:boolean"/>

	<xsl:template match="/"><obfl version="2011-1" hyphenate="{$hyphenate}"><xsl:attribute name="xml:lang"><xsl:value-of select="/dtb:dtbook/@xml:lang"/></xsl:attribute><xsl:call-template name="insertMetadata"/><xsl:call-template name="insertLayoutMaster"/><xsl:apply-templates/></obfl></xsl:template>
	<xsl:template match="dtb:dtbook | dtb:book"><xsl:apply-templates/></xsl:template>
	<xsl:template match="dtb:head | dtb:meta | dtb:link | dtb:img | dtb:col | dtb:colgroup"></xsl:template>
	
<!-- sequence elements / -->
	<xsl:template match="dtb:frontmatter | dtb:bodymatter | dtb:rearmatter">
		<xsl:apply-templates select="." mode="sequence-mode"/>
	</xsl:template>
<!-- / sequence elements -->

<!-- block elements / -->
	<!-- No text elements -->
	<xsl:template match="dtb:level1 | dtb:level2 | dtb:level3 | dtb:level4 | dtb:level5 | dtb:level6 | dtb:level">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	<!-- Can be surrounded by text, but does not contain text -->
	<xsl:template match="dtb:list | dtb:blockquote | dtb:linegroup | dtb:poem |
								dtb:div | dtb:annotation | dtb:dl | dtb:imggroup | dtb:note">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	<!-- Can contain text, but isn't surrounded by text -->
	<xsl:template match="dtb:caption | dtb:h1 | dtb:h2 | dtb:h3 | dtb:h4 | dtb:h5 | dtb:h6 | dtb:li |
								dtb:bridgehead | dtb:covertitle | dtb:docauthor | dtb:doctitle">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	<!-- Can be surrounded by text and contain text -->
	<xsl:template match="dtb:address | dtb:prodnote | dtb:hd | dtb:p | dtb:author | dtb:line | 
								dtb:epigraph | dtb:sidebar | dtb:byline | dtb:dateline | dtb:title">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
<!-- / block elements -->

<!-- inlines that may alternatively be in block elements / -->
	<xsl:template match="dtb:a | dtb:cite | dtb:samp | dtb:kbd">
		<xsl:choose>
			<xsl:when test="parent::dtb:level1 or parent::dtb:level2 or parent::dtb:level3 or parent::dtb:level4 or parent::dtb:level5 or parent::dtb:level6 or parent::dtb:level or parent::dtb:div or parent::dtb:annotation">
			<xsl:message><xsl:apply-templates select="." mode="block-mode"/></xsl:message>
			</xsl:when>
			<xsl:otherwise><xsl:apply-templates select="." mode="inline-mode"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!-- / inlines that may alternatively be in block elements -->

<!-- inline elements / -->
	<xsl:template match="dtb:bdo | dtb:code | dtb:em | dtb:strong | dtb:sup | dtb:sub | dtb:w |  
								dtb:sent | dtb:span | dtb:acronym | dtb:abbr | dtb:q | dtb:dfn |
								dtb:annoref | dtb:noteref | dtb:linenum | dtb:lic | dtb:dt | dtb:dd">
		<xsl:apply-templates select="." mode="inline-mode"/>
		<xsl:if test="self::dtb:noteref">
			<xsl:choose>
				<xsl:when test="starts-with(@idref, '#')"><anchor item="{substring-after(@idref, '#')}"/></xsl:when>
				<xsl:otherwise><xsl:message terminate="no">Only fragment identifier supported: <xsl:value-of select="@idref"/></xsl:message></xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
<!-- / inline elements -->

<!-- special / -->
	<xsl:template match="dtb:pagenum">
		<marker class="pagenum" value="{text()}"/>
		<xsl:variable name="preceding-pagenum" select="preceding::dtb:pagenum[1]"/>
		<!-- @page='normal' or $preceding-pagenum/@page='normal' -->
		<!-- This should be true for all normal pages, but false for a sequence of "unnumbered page" or similar. -->
		<xsl:if test="text()!=$preceding-pagenum/text()">
			<xsl:variable name="preceding-marker">
				<xsl:if test="not($preceding-pagenum) or generate-id($preceding-pagenum/ancestor::dtb:level1/parent::*)=
							generate-id(ancestor::dtb:level1/parent::*)">
					<xsl:value-of select="$preceding-pagenum"/><xsl:text>&#x2013;</xsl:text>
				</xsl:if>
			</xsl:variable>
			<marker class="pagenum-turn" value="{$preceding-marker}"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="dtb:br"><br/></xsl:template>

<!-- / special -->

<!-- disallowed elements / -->
	<xsl:template match="dtb:table | dtb:tbody | dtb:thead | dtb:tfoot | dtb:tr | dtb:th | dtb:td">
		<xsl:message terminate="yes">Tables are not supported.</xsl:message>
	</xsl:template>
<!-- / disallowed elements -->

<!-- default mode templates / -->
	<xsl:template match="*" mode="sequence-mode">
		<sequence>
			<xsl:apply-templates select="." mode="apply-sequence-attributes"/>
			<xsl:apply-templates/>
		</sequence>
	</xsl:template>

	<xsl:template match="*" mode="block-mode">
		<block>
			<xsl:apply-templates select="." mode="apply-block-attributes"/>
			<xsl:apply-templates/>
		</block>
	</xsl:template>

	<xsl:template match="*" mode="inline-mode">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="*" mode="apply-sequence-attributes"/>
	<xsl:template match="*" mode="apply-block-attributes"/>
<!-- / default mode templates -->

</xsl:stylesheet>
