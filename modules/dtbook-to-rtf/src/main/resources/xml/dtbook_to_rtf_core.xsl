<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
	version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<!--
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>-->
	<xsl:output method="text" indent="yes" encoding="Windows-1252"/>
	<xsl:strip-space elements="*"/>

	<!-- ##### MAJOR STRUCTURAL ELEMENTS ##### -->

	<!-- #### frontmatter ELEMENT #### -->
	<xsl:template match="frontmatter|dtb:frontmatter">
		<xsl:text>\sectd\sbkodd\pgnstarts1\pgnlcrm{\footer\qc\plain\chpgn\par}&#xa;</xsl:text>
		<xsl:apply-templates/>
	</xsl:template>

	<!-- #### bodymatter ELEMENT #### -->
	<xsl:template match="bodymatter|dtb:bodymatter">
		<xsl:text>\sect\sectd\sbkodd\pgnstarts1\pgnrestart\pgndec{\footer\qc\plain\chpgn\par}&#xa;</xsl:text>
		<!--</xsl:text>-->
		<xsl:apply-templates/>
	</xsl:template>

	<!-- #### rearmatter ELEMENT #### -->
	<xsl:template match="rearmatter|dtb:rearmatter">
		<xsl:text>\sect\sectd\sbkodd\pgncont\pgndec{\footer\qc\plain\chpgn\par}
		</xsl:text>
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- #### level and level1-6 ELEMENTS #### -->
	<xsl:template match="level|level1|level2|level3|level4|level5|level6|dtb:level|dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6">
		<xsl:apply-templates/>
	</xsl:template>

	<!-- ##### LEVEL HEADING ELEMENTS -->
	
	<!-- ##### levelhd ELEMENT -->
	<xsl:template match="levelhd">
		<xsl:call-template name="LEVEL_HD">
			<xsl:with-param name="depth" select="count(ancestor::level)"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- #### hd inside level ELEMENT-->
	<xsl:template match="dtb:hd[parent::dtb:level]">
		<xsl:call-template name="LEVEL_HD">
			<xsl:with-param name="depth" select="count(ancestor::dtb:level)"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="LEVEL_HD">
		<xsl:param name="depth"/>
		<xsl:choose>
			<xsl:when test="$depth = 1">
				<xsl:call-template name="H1_STYLE"/>
			</xsl:when>
			<xsl:when test="$depth = 2">
				<xsl:call-template name="H2_STYLE"/>
			</xsl:when>
			<xsl:when test="$depth = 3">
				<xsl:call-template name="H3_STYLE"/>
			</xsl:when>
			<xsl:when test="$depth = 4">
				<xsl:call-template name="H4_STYLE"/>
			</xsl:when>
			<xsl:when test="$depth = 5">
				<xsl:call-template name="H5_STYLE"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="H6_STYLE"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>\keep\keepn </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\plain\par&#xa;\pard </xsl:text>
		</xsl:template>

		<!-- #### h1 ELEMENT #### -->
		<xsl:template match = "h1|dtb:h1">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="1"/>
			</xsl:call-template>
		</xsl:template>

		<!-- #### h2 ELEMENT #### -->
		<xsl:template match = "h2|dtb:h2">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="2"/>
			</xsl:call-template>
		</xsl:template>

		<!-- #### h3 ELEMENT #### -->
		<xsl:template match = "h3|dtb:h3">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="3"/>
			</xsl:call-template>
		</xsl:template>

		<!-- #### h4 ELEMENT #### -->
		<xsl:template match = "h4|dtb:h4">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="4"/>
			</xsl:call-template>
		</xsl:template>

		<!-- #### h5 ELEMENTS #### -->
		<xsl:template match = "h5|dtb:h5">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="5"/>
			</xsl:call-template>
		</xsl:template>

		<!-- #### h6 ELEMENTS #### -->
		<xsl:template match = "h6|dtb:h6">
			<xsl:call-template name="LEVEL_HD">
				<xsl:with-param name="depth" select="6"/>
			</xsl:call-template>
		</xsl:template>

		<!-- ##### BLOCK ELEMENTS ##### -->

		<!-- #### div or address ELEMENT #### -->
		<xsl:template match ="div|address|dtb:div|dtb:address">
			<xsl:choose>
				<xsl:when test="child::text()">
					<xsl:call-template name="NORMAL_STYLE"/>
					<xsl:apply-templates/>
					<xsl:text>\par&#xa;\pard </xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>
		
		<!-- #### p ELEMENT ##### -->
		<xsl:template match="p|dtb:p">
			<xsl:call-template name="NORMAL_STYLE"/>
			<xsl:apply-templates/>
			<xsl:text>\par&#xa;\pard </xsl:text>
		</xsl:template>
		
		<!-- #### notice,prodnote or sidebar ELEMENTS #### -->
		<xsl:template match="notice|prodnote|sidebar|dtb:prodnote|dtb:sidebar">
			<xsl:call-template name="BOX_STYLE"/>
			<xsl:apply-templates/>
			<xsl:text>\par&#xa;\pard </xsl:text>
		</xsl:template>
		
		<!-- #### blockquote ELEMENT ### -->
		<xsl:template match="blockquote|dtb:blockquote">
			<xsl:call-template name="BLOCKQUOTE_STYLE"/>
			<xsl:apply-templates/>
			<xsl:text>\par&#xa;\pard </xsl:text>
		</xsl:template>

		<!-- #### line ELEMENT #### -->
		<xsl:template match="line|dtb:line">
			<xsl:call-template name="NORMAL_STYLE"/>
			<xsl:text>\tx556 </xsl:text>
			<xsl:choose>
				<xsl:when test="linenum|dtb:linenum">
				</xsl:when>
				<!-- If inside a linegroup or stanze -->
				<xsl:when test="parent::div[@class='stanza']|parent::linegroup|parent::dtb:linegroup">
					<xsl:text>\li566\ri566 </xsl:text>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="linenum|dtb:linenum"/>
			<xsl:for-each select="*|text()">
				<xsl:if test="not(local-name()='linenum')">
					<xsl:apply-templates select="."/>
				</xsl:if>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="following-sibling::*">
					<xsl:text>\line&#xa;</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>\par&#xa;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:template>
		
			<!-- #### linegroup ELEMENT #### 
			<xsl:template match="dtb:linegroup">
				<xsl:apply-templates/>
			</xsl:template>-->

			<!-- #### linenum ELEMENT #### -->
			<xsl:template match="linenum|dtb:linenum">
				<xsl:apply-templates/>
				<xsl:text>:\tab </xsl:text>
			</xsl:template>

			<!-- #### poem ELEMENT (or div[@class='poem']) #### -->
			<xsl:template match="dtb:poem|div[@class='poem']">
				<xsl:call-template name="NORMAL_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>

			<!-- #### epigraph ELEMENT #### -->
			<xsl:template match="dtb:epigraph">
				<xsl:call-template name="BLOCKQUOTE_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>

			<!-- #### linegroup ELEMENT #### -->
			<xsl:template match="linegroup|dtb:linegroup">
				<xsl:call-template name="NORMAL_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>
			
			<!-- ##### GENERIC HEADINGS ##### -->

			<!-- #### bridgehead, caption, hd and title ELEMENTS #### -->
			<xsl:template match="caption|hd|dtb:bridgehead|dtb:caption|dtb:title">
				<xsl:text>\sb100\sa100</xsl:text>
				<xsl:call-template name="STRONG_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>

			<!-- #### author, byline and dateline ELEMENTS #### -->
			<xsl:template match="author|dtb:author|dtb:byline|dtb:dateline">
				<xsl:call-template name="EM_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>

			<!-- #### doctitle and covertitle ELEMENT #### -->
			<xsl:template match="doctitle|dtb:covertitle|dtb:doctitle">
				<xsl:call-template name="TITLE_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>

			<!-- #### docauthor ELEMENT #### -->
			<xsl:template match="docauthor|dtb:docauthor">
				<xsl:call-template name="SUBTITLE_STYLE"/>
				<xsl:apply-templates/>
				<xsl:text>\par&#xa;\pard </xsl:text>
			</xsl:template>


			<!-- ##### EMPTY ELEMENTS ##### -->

			<!-- #### br ELEMENT #### -->
			<xsl:template match="br|dtb:br">
				<xsl:text>\line&#xa;</xsl:text>
			</xsl:template>

			<!-- #### hr ELEMENT #### -->
			<xsl:template match="hr|dtb:hr">
				<xsl:text>\par&#xa;</xsl:text>

			</xsl:template>

			<!-- ##### INLINE ELEMENTS ##### -->

			<!-- #### strong ELEMENT #### -->
			<xsl:template match="strong|dtb:strong">
				<xsl:text>\b </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>\b0 </xsl:text>
			</xsl:template>

			<!-- #### em, dfn, cite (emphasized) ELEMENT #### -->
			<xsl:template match="em|dfn|cite|dtb:em|dtb:dfn|dtb:cite">
				<xsl:text>\i </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>\i0 </xsl:text>
			</xsl:template>

			<!-- #### q ELEMENT #### -->
			<xsl:template match="q|dtb:q">
				<xsl:text>\ldblquote </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>\rdblquote </xsl:text>
			</xsl:template>

			<!-- #### span, abbr, bdo and acronym ELEMENTS #### -->
			<xsl:template match="span|abbr|acronym|bdo|dtb:span|dtb:abbr|dtb:acronym|dtb:bdo">
				<xsl:apply-templates/>
			</xsl:template>

			<!-- #### code, kbd and samp ELEMENTS #### -->
			<xsl:template match="code|kbd|samp|dtb:code|dtb:kbd|dtb:samp">
				<xsl:text>{\f1 </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
			</xsl:template>

			<!-- #### sup ELEMENT #### -->
			<xsl:template match="sup|dtb:sup">
				<xsl:text>{\super </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
			</xsl:template>

			<!-- #### sub ELEMENT #### -->
			<xsl:template match="sub|dtb:sub">
				<xsl:text>{\sub </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
			</xsl:template>
			
			<!-- ##### WORD/SENTENCE STRUCTURE ##### -->
			
			<!-- #### sent ELEMENT (IGNORED) -->
			<xsl:template match="sent|dtb:sent">
				<xsl:apply-templates/>
			</xsl:template>

			<!-- #### w ELEMENT (IGNORED) -->
			<xsl:template match="w|dtb:w">
				<xsl:apply-templates/>
			</xsl:template>


			<!-- ##### PAGE NUMBER STRUCTUR ##### -->
			
			<!-- #### pagenum ELEMENT #### (ignored) -->
			<xsl:template match="pagenum|dtb:pagenum">
				<xsl:if test="$inclPagenum='true'">
					<xsl:text>&#xa;\pard\plain \ql \li0\ri0\sb120\sa80\widctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 \fs16 \i\lang1030\langfe1033\cgrid\langnp2057\langfenp1033&#xa;{</xsl:text>
					<xsl:apply-templates/>
					<xsl:text>&#xa;\par }&#xa;</xsl:text>
				</xsl:if>
			</xsl:template>

			<!-- ##### Text nodes - calls template rtf-encode in xslt dtbook_to_rtf_encode.xsl ##### -->
			<xsl:template match="text()">
				<xsl:call-template name="rtf-encode">
					<xsl:with-param name="str" select="."/>
				</xsl:call-template>
			</xsl:template>

		</xsl:stylesheet>