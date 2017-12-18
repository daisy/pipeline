<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:epub="http://www.idpf.org/2007/ops"
	xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	exclude-result-prefixes="html epub xs obfl dotify"
	xmlns="http://www.daisy.org/ns/2011/obfl">
	<xsl:import href="html2obfl_base.xsl"/>
	<xsl:import href="book-formats.xsl"/>
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<xsl:param name="default-paragraph-separator" select="'indent'" as="xs:string"/> <!-- empty-line or indent -->
	<xsl:param name="toc-depth" select="6" dotify:desc="The maximum depth of generated toc (A positive integer)" dotify:default="6"/>
	<xsl:param name="toc-indent-multiplier" select="1" dotify:desc="Indentation for each toc level"  dotify:default="1"/>
	<!-- TODO: should also support value 'keep' to keep the original toc -->
	<xsl:param name="toc-policy" select="'replace'" dotify:desc="The toc generation policy" dotify:default="replace" dotify:values="replace/always"/>
	<xsl:param name="remove-title-page" select="'true'" 
			dotify:desc="Removes the title page from the text flow (@epub:type=&quot;titlepage&quot; or @epub:type=&quot;halftitlepage&quot;)" 
			dotify:values="true/false"
			dotify:default="true"/>

	<xsl:key name="noterefs" match="html:a[epub:noteref(.)]" use="substring-after(@href, '#')"/>

	<xsl:variable name="footnotesInFrontmatter" select="
		//*[epub:note(.)][key('noterefs', @id)[epub:getMatterForElement(.)='frontmatter']]"/>
	<!-- //dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]] -->
	<xsl:variable name="footnotesNotInFrontmatter" select="
		//*[epub:note(.)][key('noterefs', @id)[epub:getMatterForElement(.)!='frontmatter']]"/>
	<!-- //dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]] -->
	
	<xsl:variable name="isEpub" select="count(//*[@epub:type])>0" as="xs:boolean"/>

	<xsl:template match="/">
		<obfl version="2011-1" hyphenate="{$hyphenate}">
			<xsl:attribute name="xml:lang" select="html:lang(/html:html)"/>
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
		<xsl:variable name="insertToc" select="$toc-depth > 0 and (//*[epub:types(.)=('toc')] or $toc-policy='always')" as="xs:boolean"/>
		<xsl:if test="$insertToc">
			<xsl:call-template name="insertToc"/>
		</xsl:if>
		<xsl:variable name="firstInFirstVolumeContent">
			<xsl:if test="$colophon-metadata-placement='begin'">
				<xsl:call-template name="insertColophon"/>
			</xsl:if>
			<xsl:if test="$rear-cover-placement='begin'">
				<xsl:call-template name="insertCoverCopy"/>
			</xsl:if>
		</xsl:variable>
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
			$additionalPreContent,
			$firstInFirstVolumeContent)"/>
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
	
	<!-- Noterefs -->
	<xsl:template match="html:a[epub:noteref(.)]" priority="10">
		<xsl:apply-templates select="." mode="inline-mode"/>
		<xsl:variable name="afix">
			<xsl:choose>
				<xsl:when test="epub:getMatterForElement(.)='frontmatter'">.A</xsl:when>
				<xsl:otherwise>.B</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="starts-with(@href, '#')"><anchor item="{concat(substring-after(@href, '#'), $afix)}"/></xsl:when>
			<xsl:otherwise><xsl:message terminate="no">Only fragment identifier supported: <xsl:value-of select="@href"/></xsl:message></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Remove notes elements from the flow -->
	<xsl:template match="html:*[epub:note(.)]" priority="10"/>

	<!-- Remove emptied notes level -->
	<xsl:template match="html:*[epub:notes(.)]"/>
	
	<xsl:template match="html:*[epub:note(.)]" mode="collectNotes">
		<xsl:param name="afix"/>
		<item id="{concat(@id, $afix)}">
			<xsl:variable name="note">
				<xsl:apply-templates/>
			</xsl:variable>
			<xsl:for-each select="$note/node()[self::* or self::text()[normalize-space()!='']]">
				<xsl:choose>
					<xsl:when test="self::text()"> <!-- and not whitespace only -->
						<xsl:choose>
							<xsl:when test="position()=1">
								<block text-indent="3" block-indent="3"><xsl:copy-of select="."/></block>
							</xsl:when>
							<xsl:otherwise>
								<block margin-left="3"><xsl:copy-of select="."/></block>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when> 
					<xsl:when test="position()=1 and count(text())>0"> <!-- and an element -->
						<xsl:copy>
							<xsl:copy-of select="@*[not(local-name()='first-line-indent' or local-name()='text-indent' or local-name()='block-indent')]"/>
							<xsl:attribute name="text-indent">3</xsl:attribute>
							<xsl:attribute name="block-indent">3</xsl:attribute>
							<xsl:copy-of select="node()"/>
						</xsl:copy>
					</xsl:when>
					<xsl:otherwise>
						<block margin-left="3">
							<xsl:copy-of select="."/>
						</block>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</item>
	</xsl:template>
	
	<xsl:template match="html:body">
		<xsl:if test="*[epub:types(.)=('frontmatter') and not(epub:types(.)=('cover', 'colophon', 'toc'))]">
			<sequence master="front" initial-page-number="1">
				<xsl:apply-templates select="*[epub:types(.)=('frontmatter') and not(epub:types(.)=('cover', 'colophon', 'toc'))]"/>
			</sequence>
		</xsl:if>
		<sequence master="main" initial-page-number="1">
			<!-- Put everything that isn't specifically front- or backmatter here. -->
			<xsl:apply-templates select="text()[normalize-space(.)!='']|processing-instruction()|comment()|*[not(epub:types(.)=('frontmatter', 'cover', 'toc', 'colophon', 'backmatter'))]"/>
		</sequence>
		<xsl:if test="*[epub:types(.)=('backmatter') and not(epub:types(.)=('toc'))]">
			<sequence master="main">
				<xsl:apply-templates select="*[epub:types(.)=('backmatter') and not(epub:types(.)=('toc'))]"/>
			</sequence>
		</xsl:if>
		<xsl:if test="not($colophon-metadata-placement='begin')">
			<xsl:call-template name="insertColophon"/>
		</xsl:if>
		<xsl:if test="not($rear-cover-placement='begin')">
			<xsl:call-template name="insertCoverCopy"/>
		</xsl:if>
	</xsl:template>

	<xsl:template name="insertColophon">
		<xsl:copy-of select="obfl:insertColophon(//html:*[epub:types(.)=('colophon')])"/>
	</xsl:template>
	
	<xsl:template name="insertCoverCopy">
		<xsl:copy-of select="obfl:insertBackCoverTextAndRearJacketCopy(//html:*[epub:types(.)=('cover')])"/>
	</xsl:template>

	<xsl:template match="html:h1[epub:types(parent::*)=('part')]" mode="block-mode">
		<block>
			<xsl:apply-templates select="." mode="apply-block-attributes"/>
			<block><leader position="100%" align="right" pattern=":"/></block>
			<block margin-left="2" margin-right="2" text-indent="2"><xsl:apply-templates/></block>
			<block><leader position="100%" align="right" pattern=":"/></block>
		</block>
	</xsl:template>

	<xsl:template match="html:h1" mode="apply-block-attributes">
		<xsl:attribute name="margin-top" select="if ($row-spacing=2) then 2 else 3"/>
		<xsl:attribute name="margin-bottom">1</xsl:attribute>		
		<xsl:if test="$row-spacing=2">
			<xsl:attribute name="border-bottom-style">solid</xsl:attribute>
			<xsl:attribute name="border-align">inner</xsl:attribute>
		</xsl:if>
		<xsl:attribute name="keep">page</xsl:attribute>
		<xsl:attribute name="keep-with-next">1</xsl:attribute>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
		<xsl:choose>
			<xsl:when test="not($isEpub)">
				<xsl:attribute name="break-before">page</xsl:attribute>
			</xsl:when>
			<xsl:when test="epub:types(parent::*)=('part')">
				<xsl:attribute name="keep-with-next-sheets">1</xsl:attribute>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- epub only -->
	<xsl:template match="html:*[parent::html:body and epub:types(.)=('chapter', 'part')]" mode="apply-block-attributes">
		<xsl:if test="not(html:h1)">
			<xsl:attribute name="margin-top" select="if ($row-spacing=2) then 2 else 3"/>
		</xsl:if>
		<xsl:attribute name="break-before">page</xsl:attribute>
		<xsl:attribute name="keep-with-previous-sheets">1</xsl:attribute>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
	</xsl:template>
		
	<xsl:template match="html:h2" mode="apply-block-attributes">
		<xsl:attribute name="margin-top" select="if ($row-spacing=2) then 1 else 2"/>
		<xsl:attribute name="margin-bottom">1</xsl:attribute>
		<xsl:if test="$row-spacing=2">
			<xsl:attribute name="border-bottom-style">solid</xsl:attribute>
			<xsl:attribute name="border-align">inner</xsl:attribute>
		</xsl:if>
		<xsl:attribute name="keep">page</xsl:attribute>
		<xsl:attribute name="keep-with-next">1</xsl:attribute>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
	</xsl:template>
	
	<xsl:template match="html:h3 | html:h4 | html:h5 | html:h6" mode="apply-block-attributes">
		<xsl:attribute name="margin-top">1</xsl:attribute>
		<xsl:attribute name="margin-bottom">1</xsl:attribute>		
		<xsl:attribute name="keep">page</xsl:attribute>
		<xsl:attribute name="keep-with-next">1</xsl:attribute>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
	</xsl:template>

	<xsl:template match="html:section" mode="apply-block-attributes">
		<xsl:variable name="level" select="count(ancestor-or-self::html:section)+1"/>
		<xsl:choose>
			<xsl:when test="$level=2 and not(html:h2)">
				<xsl:attribute name="margin-top" select="if ($row-spacing=2) then 1 else 2"/>
			</xsl:when>
			<xsl:when test="not(html:h3 or html:h4 or html:h5 or html:h6)">
				<xsl:attribute name="margin-top">1</xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
	</xsl:template>
	
	<!-- Override default processing -->
	<xsl:template match="html:p" mode="block-mode" priority="10">
		<xsl:variable name="tokens" select="tokenize(@class, '\s+')"/>
		<xsl:if test="$tokens='precedingseparator'">
			<block keep="page" keep-with-next="1" padding-top="1" padding-bottom="1"><xsl:text>---</xsl:text></block>
		</xsl:if>
		<block>
			<xsl:if test="$tokens='precedingemptyline'">
				<xsl:attribute name="padding-top">1</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="$tokens='indented'"><xsl:attribute name="first-line-indent">2</xsl:attribute></xsl:when>
				<xsl:when test="not($tokens='precedingemptyline' or $tokens='precedingseparator' or $tokens='no-indent')">
					<xsl:if test="(preceding-sibling::*[not(epub:types(.)='pagebreak')][1])[self::html:p]">
						<xsl:choose>
							<xsl:when test="$default-paragraph-separator='empty-line'"><xsl:attribute name="margin-top">1</xsl:attribute></xsl:when>
							<xsl:otherwise><xsl:attribute name="first-line-indent">2</xsl:attribute></xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates/>
		</block>
	</xsl:template>
	
	<!-- Lists -->
	<xsl:template match="html:ul|html:ol" mode="apply-block-attributes">
		<xsl:if test="not(ancestor::html:ul or ancestor::html:ol)">
			<xsl:attribute name="margin-top">1</xsl:attribute>
		</xsl:if>
		<xsl:attribute name="margin-bottom">1</xsl:attribute>
		<xsl:attribute name="margin-left">2</xsl:attribute>
		<xsl:attribute name="list-type">
			<xsl:choose>
				<xsl:when test="ancestor::*[epub:types(.)=('index')]">pl</xsl:when>
				<xsl:otherwise><xsl:value-of select="local-name()"/></xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
		<xsl:if test="self::html:ol[@type]">
			<xsl:attribute name="list-style"><xsl:value-of select="@type"/></xsl:attribute>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="html:li" mode="apply-block-attributes">
		<xsl:choose>
			<xsl:when test="ancestor::*[epub:types(.)=('index')]"> <!-- parent::dtb:list/@type='pl' -->
				<xsl:variable name="indent_max" select="max((parent::*/html:li)/string-length(substring-before(descendant::text()[1], ' '))) + 1"/>
				<xsl:variable name="indent_min" select="min((parent::*/html:li)/string-length(substring-before(descendant::text()[1], ' '))) + 1"/>
				<xsl:variable name="indent" select="if ($indent_min=$indent_max and $indent_min &lt; 5) then $indent_min else 3"/>
				<xsl:attribute name="text-indent"><xsl:value-of select="$indent"/></xsl:attribute>
				<xsl:attribute name="block-indent"><xsl:value-of select="$indent"/></xsl:attribute>
			</xsl:when>
			<xsl:when test="parent::html:ul">
				<xsl:attribute name="first-line-indent">2</xsl:attribute>
				<xsl:attribute name="text-indent">2</xsl:attribute>
				<xsl:attribute name="block-indent">2</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="parent::html:ol and @value">
					<xsl:attribute name="list-item-label"><xsl:value-of select="@value"/></xsl:attribute>
				</xsl:if>
				<xsl:attribute name="first-line-indent">3</xsl:attribute>
				<xsl:attribute name="text-indent">3</xsl:attribute>
				<xsl:attribute name="block-indent">3</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Override default processing -->
	<xsl:template match="html:dt">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	
	<!-- Override default processing -->
	<xsl:template match="html:dd">
		<xsl:apply-templates select="." mode="block-mode"/>
	</xsl:template>
	
	<xsl:template match="html:dd" mode="apply-block-attributes">
		<xsl:attribute name="text-indent">3</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="html:dd" mode="block-mode">
		<xsl:variable name="contents"><xsl:apply-templates/></xsl:variable>
		<xsl:choose>
			<!-- The following is done to guard against block inside style, but it will only catch the simplest cases -->
			<xsl:when test="count($contents/obfl:block)=count($contents/node()[not(self::text() and normalize-space()='')])">
				<xsl:for-each select="$contents/obfl:block">
					<block>
						<xsl:apply-templates select="." mode="apply-block-attributes"/>
						<style name="dd"><xsl:copy-of select="node()"/></style>
					</block>					
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<block>
					<xsl:apply-templates select="." mode="apply-block-attributes"/>
					<style name="dd"><xsl:copy-of select="$contents"/></style>
				</block>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="text()">
		<xsl:choose>
			<!-- cases NOT handled by applyStyle/applyFlatStyle -->
			<xsl:when test="
				ancestor::*[@xml:lang or @lang][1][not(self::html:html)]
				and not(ancestor::html:em or ancestor::html:strong or ancestor::html:i or ancestor::html:b)
				and not(count(parent::node())=1 and (parent::html:sub or parent::html:sup))">
					<span><xsl:attribute name="xml:lang"><xsl:value-of select="ancestor::*[@xml:lang or @lang][1]/(@xml:lang, @lang)[1]"/></xsl:attribute><xsl:value-of select="."/></span>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="html:em | html:strong" mode="inline-mode">
		<xsl:call-template name="applyStyle"/>
	</xsl:template>
	
	<xsl:template match="html:i" mode="inline-mode">
		<xsl:call-template name="applyStyle">
			<xsl:with-param name="name" select="'em'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="html:b" mode="inline-mode">
		<xsl:call-template name="applyStyle">
			<xsl:with-param name="name" select="'strong'"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="html:sub | html:sup" mode="inline-mode">
		<xsl:call-template name="applyFlatStyle"/>
	</xsl:template>
	
	<!-- Remove title page if set to remove -->
	<xsl:template match="html:div[epub:types(.)=('titlepage', 'halftitlepage')]" priority="1">
		<!-- The test is negative, because a misspelled value should result in keeping the title page. -->
		<xsl:if test="$remove-title-page!='true'">
			<xsl:next-match />
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="applyStyle">
		<xsl:param name="name" select="name()" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="count(node())=0">
				<xsl:text> </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="ancestor-or-self::*[html:has-lang(.)][1][not(self::html:html)] and not(ancestor::html:em or ancestor::html:strong or ancestor::html:i or ancestor::html:b)">
						<span><xsl:attribute name="xml:lang"><xsl:value-of select="html:lang(.)"/></xsl:attribute><style name="{$name}"><xsl:apply-templates/></style></span>
					</xsl:when>
					<xsl:otherwise><style name="{$name}"><xsl:apply-templates/></style></xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="applyFlatStyle">
		<xsl:choose>
			<!-- text contains a single string -->
			<xsl:when test="count(node())=1 and text()">
				<xsl:choose>
					<xsl:when test="ancestor-or-self::*[html:has-lang(.)][1][not(self::html:html)] and not(ancestor::html:em or ancestor::html:strong or ancestor::html:i or ancestor::html:b)">
						<span><xsl:attribute name="xml:lang"><xsl:value-of select="html:lang(.)"/></xsl:attribute><style name="{name()}"><xsl:apply-templates/></style></span>
					</xsl:when>
					<xsl:otherwise><style name="{name()}"><xsl:apply-templates/></style></xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- Otherwise -->
			<xsl:otherwise>
				<xsl:message terminate="no">Error: sub/sub contains a complex expression for which there is no specified formatting.</xsl:message>
				<xsl:apply-templates/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- TOC is auto-generated (in epub) -->
	<xsl:template match="*[epub:types(.)=('toc')]"/>

	<xsl:template match="*" mode="toc-entry-attributes">
		<xsl:attribute name="ref-id" select="generate-id(.)"/>
		<xsl:attribute name="block-indent" select="$toc-indent-multiplier"/>
		<xsl:attribute name="text-indent" select="2*$toc-indent-multiplier"/>
		<xsl:attribute name="keep">page</xsl:attribute>
		<xsl:apply-templates select="." mode="toc-hd"/>
	</xsl:template>
	
	<xsl:template name="insertToc">
		<table-of-contents name="full-toc">
			<xsl:choose>
				<xsl:when test="//html:*[epub:types(.)=('part')]">
					<xsl:for-each-group select="//*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][not(ancestor::*[epub:types(.)=('toc', 'titlepage') or epub:notes(.)])]" 
						group-starting-with="html:h1[ancestor::*[epub:types(.)=('part')]]">
						<xsl:choose>
							<xsl:when test="current-group()[1][self::html:h1 and ancestor::*[epub:types(.)=('part')]]">
								<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
									<xsl:for-each-group select="current-group()[not(self::html:h1 and ancestor::*[epub:types(.)=('part')]) and $toc-depth > 1]" group-starting-with="html:h1[not(ancestor::*[epub:types(.)=('part')])]">
										<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
											<xsl:call-template name="insertTocLevels">
												<xsl:with-param name="seq" select="current-group()[not(self::html:h1)]"/>
												<xsl:with-param name="level-offset" select="1"/>
											</xsl:call-template>
										</toc-entry>
									</xsl:for-each-group>
								</toc-entry>
							</xsl:when>
							<xsl:otherwise>
								<xsl:for-each-group select="current-group()" group-starting-with="html:h1">
									<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
										<xsl:call-template name="insertTocLevels">
											<xsl:with-param name="seq" select="current-group()[not(self::html:h1)]"></xsl:with-param>
										</xsl:call-template>
									</toc-entry>
								</xsl:for-each-group>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each-group>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each-group select="//*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][not(ancestor::*[epub:types(.)=('cover', 'colophon', 'toc', 'titlepage') or epub:notes(.)])]" group-starting-with="html:h1">
						<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
								<xsl:call-template name="insertTocLevels">
									<xsl:with-param name="seq" select="current-group()[not(self::html:h1)]"></xsl:with-param>
								</xsl:call-template>
						</toc-entry>
					</xsl:for-each-group>
				</xsl:otherwise>
			</xsl:choose>
		</table-of-contents>
	</xsl:template>
	
	<xsl:template name="insertTocLevels">
		<xsl:param name="seq" as="element()*" required="yes"/>
		<xsl:param name="level-offset" as="xs:integer" select="0"/>
		<xsl:for-each-group select="$seq[$toc-depth > 1 + $level-offset]" group-starting-with="html:h2">
			<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
				<xsl:for-each-group select="current-group()[not(self::html:h2) and $toc-depth > 2 + $level-offset]" group-starting-with="html:h3">
					<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
						<xsl:for-each-group select="current-group()[not(self::html:h3) and $toc-depth > 3 + $level-offset]" group-starting-with="html:h4">
							<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
								<xsl:for-each-group select="current-group()[not(self::html:h4) and $toc-depth > 4 + $level-offset]" group-starting-with="html:h5">
									<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/>
										<xsl:for-each-group select="current-group()[not(self::html:h5) and $toc-depth > 5 + $level-offset]" group-starting-with="html:h6">
											<toc-entry><xsl:apply-templates select="." mode="toc-entry-attributes"/></toc-entry>
										</xsl:for-each-group>
									</toc-entry>
								</xsl:for-each-group>
							</toc-entry>
						</xsl:for-each-group>
					</toc-entry>
				</xsl:for-each-group>
			</toc-entry>
		</xsl:for-each-group>
	</xsl:template>
	
	<xsl:template match="*" mode="toc-hd">
		<xsl:apply-templates mode="toc-text"/>
		<xsl:if test="$show-print-page-numbers">
			<xsl:text> (</xsl:text><xsl:value-of select="preceding::html:*[@epub:type='pagebreak'][1]/@title"/><xsl:text>)</xsl:text>
		</xsl:if>
		<xsl:if test="$show-braille-page-numbers">
			<xsl:text> </xsl:text><leader position="100%" align="right" pattern="."/><page-number ref-id="{generate-id(.)}">
				<!-- FIXME: roman numerals <xsl:if test="ancestor::dtb:frontmatter"><xsl:attribute name="number-format">roman</xsl:attribute></xsl:if>--></page-number>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="*" mode="toc-text">
		<xsl:apply-templates mode="toc-text"/>
	</xsl:template>
	<xsl:template match="text()" mode="toc-text">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="html:br" mode="toc-text">
		<xsl:text> </xsl:text>
	</xsl:template>

</xsl:stylesheet>
