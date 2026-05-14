<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_core.xsl"/>
	<xsl:import href="dtbook_to_rtf_hyperlink_bookmarks.xsl"/>
	<xsl:import href="dtbook_to_rtf_table.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>
	<xsl:import href="dtbook_to_rtf_list.xsl"/>
	<xsl:import href="dtbook_to_rtf_images.xsl"/>
	<xsl:import href="dtbook_to_rtf_note_and_anno.xsl"/>
	<xsl:import href="dtbook_to_rtf_metadata.xsl"/>
	<xsl:param name="inclTOC">false</xsl:param>
	<xsl:param name="inclPagenum">false</xsl:param>
	<xsl:param name="sourceFile" />
	<xsl:output method="text" encoding="Windows-1252"/>
	<xsl:strip-space elements="*"/>
	
	<!-- Due to performance issue when the source file is directly passed as input
	to the xproc step, we need to save temporary the input file and then open it
	with doc function -->
	<xsl:template name="start">
		<xsl:apply-templates select="doc($sourceFile)"/>
	</xsl:template>
	
	<xsl:template match="/*">
		<c:data xmlns:c="http://www.w3.org/ns/xproc-step">
			<xsl:next-match/>
		</c:data>
	</xsl:template>
	
	<xsl:template match="dtbook|dtb:dtbook">
		<xsl:text>{\rtf1\ansi\ansicpg1252\deff0 </xsl:text>
		<xsl:call-template name="FONTS"/>
		<xsl:call-template name="COLORS"/>
		<xsl:call-template name="STYLESHEETS"/>
		<xsl:call-template name="META_INFO"/>
		<xsl:call-template name="DOC_PROPS"/>
		<xsl:apply-templates select="book|dtb:book"/>
		<xsl:text>}</xsl:text>
	</xsl:template> 
	
	<!-- #### book ELEMENT #### -->
	<xsl:template match="book|dtb:book">
		<xsl:apply-templates select="frontmatter|dtb:frontmatter"/>
		<xsl:if test="$inclTOC='true'">
			<xsl:call-template name="TOC"/>
		</xsl:if>
		<xsl:apply-templates select="bodymatter|dtb:bodymatter"/>
		<xsl:apply-templates select="rearmatter|dtb:rearmatter"/>
	</xsl:template>

	<!-- ##### matches all elements, bookmark insertion ##### -->
	<xsl:template match="*">
		<xsl:call-template name="BOOKMARK_START"/>
		<xsl:apply-imports/>
		<xsl:call-template name="BOOKMARK_END"/>
	</xsl:template>

	<xsl:template name="TOC">
		<xsl:call-template name="H1_STYLE"/>
		<xsl:text>\keep\keepn </xsl:text>
		<xsl:call-template name="TOC_NAME"/>
		<xsl:text>\par&#xa;\pard </xsl:text>
		<xsl:apply-templates mode="toc"/>
	</xsl:template>

	<xsl:template name="TOC_NAME">
		<xsl:variable name="lang" select="//book/@lang|//book/@xml:lang|//dtb:book/@lang|//dtb:book/@xml:lang"/>
		<xsl:choose>
			<xsl:when test="starts-with($lang, 'da')">
				<xsl:call-template name="rtf-encode">
					<xsl:with-param name="str">Indhold</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="rtf-encode">
					<xsl:with-param name="str">Table of Contents</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*" mode="toc">
		<xsl:apply-templates mode="toc"/>
	</xsl:template>

	<xsl:template match="text()" mode="toc"/>

	<xsl:template match="levelhd|dtb:hd[parent::dtb:level]" mode="toc">
		<xsl:call-template name="TOC_ITEM">
			<xsl:with-param name="depth" select="count(ancestor::level|ancestor::dtb:level)"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="TOC_ITEM">
		<xsl:param name="depth">1</xsl:param>
		<xsl:text>{</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"/>
		<xsl:text>\sb0\sa0\tqr\tldot\tx9354</xsl:text>
		<xsl:value-of select="concat('\li', 283*$depth, ' ')"/>
		<xsl:text>{\field</xsl:text>
		<xsl:text>{\*\fldinst HYPERLINK \\l </xsl:text>
		<xsl:value-of select="@id"/>
		<xsl:text>}</xsl:text>
		<xsl:text>{\fldrslt </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
		<xsl:text>}</xsl:text>
		<xsl:text>\tab </xsl:text>
		<xsl:text>{\field\flddirty</xsl:text>
		<xsl:text>{\*\fldinst PAGEREF </xsl:text>
		<xsl:value-of select="@id"/>
		<xsl:text>\\h }</xsl:text>
		<xsl:text>{\fldrslt ??}</xsl:text>
		<xsl:text>}</xsl:text>
		<xsl:text>\par}&#xa;</xsl:text>
	</xsl:template>


	<xsl:template match="h1|dtb:h1" mode="toc">
		<xsl:call-template name="TOC_ITEM">
			<xsl:with-param name="depth">1</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="h2|dtb:h2" mode="toc">
		<xsl:call-template name="TOC_ITEM">
			<xsl:with-param name="depth">1</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="h3|dtb:h3" mode="toc">
		<xsl:call-template name="TOC_ITEM">
			<xsl:with-param name="depth">1</xsl:with-param>
		</xsl:call-template>
	</xsl:template>


</xsl:stylesheet>
