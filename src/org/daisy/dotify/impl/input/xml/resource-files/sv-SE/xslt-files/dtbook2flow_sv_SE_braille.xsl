<?xml version="1.0" encoding="utf-8"?>
<?xslt-doc-file doc-files/dtb2obfl_braille.html?>
<!--
	TODO:
		- komplexa sub, sup
		- länkar, e-postadresser
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="dtb xs" xmlns="http://www.daisy.org/ns/2011/obfl">

	<xsl:import href="dtbook2flow_sv_SE.xsl" />
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<xsl:param name="toc-indent-multiplier" select="1"/>
	<xsl:param name="splitterMax" select="10"/>

	<xsl:param name="l10nLang" select="'en'"/>
	<xsl:param name="l10nTocHeadline" select="'Table Of Contents'"/>
	<xsl:param name="l10nTocDescription" select="''"/>
	<xsl:param name="l10nTocVolumeStart" select="'Volume {0}'"/>
	<xsl:param name="l10nTocVolumeHeading" select="'Contents of Volume {0}'"/>
	<xsl:param name="l10nTocVolumeXofY" select="'Volume {0} of {1}'"/>
	<xsl:param name="l10nTocOneVolume" select="'One Volume'"/>
	<xsl:param name="l10nEndnotesHeadling" select="'Footnotes'"/>
	<xsl:param name="l10nEndnotesPageStart" select="'Page {0}'"/>
	<xsl:param name="l10nEndnotesPageHeader" select="'Footnotes'"/>
	
	<xsl:key name="noterefs" match="dtb:noteref" use="substring-after(@idref, '#')"/>

	<xsl:template match="/">
		<obfl version="2011-1" hyphenate="{$hyphenate}">
			<xsl:attribute name="xml:lang"><xsl:value-of select="/dtb:dtbook/@xml:lang"/></xsl:attribute>
			<xsl:call-template name="insertMetadata"/>
			<xsl:call-template name="insertLayoutMaster"/>
			<xsl:call-template name="insertNoteCollection"/>
			<xsl:apply-templates/>
		</obfl>
	</xsl:template>
	
	<xsl:template name="insertLayoutMaster">
		<layout-master name="front" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header>
					<field><string value="&#xA0;&#xA0;"/><current-page style="roman"/></field>
				</header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<!-- This looks weird, but it is correct. If row-spacing is double, then offset the header
					 of every front page as to avoid embossing on the same row on front and back -->
					<xsl:if test="$row-spacing=2">
						<xsl:attribute name="row-spacing">1</xsl:attribute>
					</xsl:if>
					<field><string value=""/></field>
					<field><current-page style="roman"/></field>
				</header>
				<footer></footer>
			</default-template>
			<xsl:call-template name="insertFrontPageArea"/>
		</layout-master>
		<layout-master name="main" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header>
					<field><string value="&#xA0;&#xA0;"/><current-page style="default"/></field>
					<field>
						<marker-reference marker="pagenum-turn" direction="forward" scope="page-content"/>
						<marker-reference marker="pagenum" direction="backward" scope="sequence"/>
					</field>
				</header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<!-- This looks weird, but it is correct. If row-spacing is double, then offset the header
					 of every front page as to avoid embossing on the same row on front and back -->
					<xsl:if test="$row-spacing=2">
						<xsl:attribute name="row-spacing">1</xsl:attribute>
					</xsl:if>
					<field><string value="&#xA0;&#xA0;"/>
						<marker-reference marker="pagenum-turn" direction="forward" scope="page-content"/>
						<marker-reference marker="pagenum" direction="backward" scope="sequence"/>
					</field>
					<field><current-page style="default"/></field>
				</header>
				<footer></footer>
			</default-template>
			<xsl:call-template name="insertMainPageArea"/>
		</layout-master>
		<layout-master name="plain" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header><field><string value=""/></field></header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<!-- This looks weird, but it is correct. If row-spacing is double, then offset the header
					 of every front page as to avoid embossing on the same row on front and back -->
					<xsl:if test="$row-spacing=2">
						<xsl:attribute name="row-spacing">1</xsl:attribute>
					</xsl:if>
					<field><string value=""/></field>
				</header>
				<footer></footer>
			</default-template>
		</layout-master>
		<layout-master name="notes" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
			<template use-when="(= (% $page 2) 0)">
				<header><field><string value="&#xA0;&#xA0;"/><string value="{$l10nEndnotesPageHeader} "/><current-page style="default"/></field></header>
				<footer></footer>
			</template>
			<default-template>
				<header>
					<!-- This looks weird, but it is correct. If row-spacing is double, then offset the header
					 of every front page as to avoid embossing on the same row on front and back -->
					<xsl:if test="$row-spacing=2">
						<xsl:attribute name="row-spacing">1</xsl:attribute>
					</xsl:if>
					<field><string value=""/></field>
					<field><string value="{$l10nEndnotesPageHeader} "/><current-page style="default"/></field>
				</header>
				<footer></footer>
			</default-template>
		</layout-master>
		<layout-master name="cover" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="1" duplex="{$duplex}" border-style="solid" border-width="1" border-align="outer">
			<default-template>
				<header></header>
				<footer></footer>
			</default-template>
		</layout-master>
		<xsl:choose>
			<xsl:when test="//dtb:level1[@class='toc'] or //dtb:level1[dtb:list[@class='toc']]">
			<table-of-contents name="full-toc">
				<xsl:apply-templates select="//dtb:level1" mode="toc"/>
			</table-of-contents>
			<volume-template volume-number-variable="volume" volume-count-variable="volumes" use-when="(= $volume 1)" sheets-in-volume-max="{$splitterMax}">
				<pre-content>
					<xsl:call-template name="coverPage"/>
					<toc-sequence master="front" toc="full-toc" range="document" initial-page-number="1">
						<on-toc-start>
							<block padding-bottom="1"><xsl:value-of select="$l10nTocHeadline"/></block>
							<xsl:if test="$l10nTocDescription!=''">
								<block padding-bottom="1"><xsl:value-of select="$l10nTocDescription"/></block>
							</xsl:if>
						</on-toc-start>
						<on-volume-start use-when="(&amp; (> $volumes 1) (= $started-volume-number 1))">
							<block keep="all" keep-with-next="1" padding-bottom="0"><evaluate expression="(format &quot;{$l10nTocVolumeStart}&quot; $started-volume-number)"/></block>
						</on-volume-start>
						<on-volume-start use-when="(&amp; (> $volumes 1) (> $started-volume-number 1))">
							<block keep="all" keep-with-next="1" padding-top="1" padding-bottom="0"><evaluate expression="(format &quot;{$l10nTocVolumeStart}&quot; $started-volume-number)"/></block>
						</on-volume-start>
					</toc-sequence>
					<xsl:apply-templates select="//dtb:frontmatter" mode="pre-volume-mode"/>
				</pre-content>
				<post-content>
					<xsl:call-template name="postContentNotes"/>
				</post-content>
			</volume-template>
			<volume-template volume-number-variable="volume" volume-count-variable="volumes" use-when="(> $volume 1)" sheets-in-volume-max="{$splitterMax}">
				<pre-content>
					<xsl:call-template name="coverPage"/>
					<toc-sequence master="front" toc="full-toc" range="volume" initial-page-number="1">
						<on-toc-start>
							<block padding-bottom="1"><evaluate expression="(format &quot;{$l10nTocVolumeHeading}&quot; $volume)"/></block>
						</on-toc-start>
					</toc-sequence>
				</pre-content>
				<post-content>
					<xsl:call-template name="postContentNotes"/>
				</post-content>
			</volume-template>
			</xsl:when>
			<xsl:otherwise>
				<volume-template sheets-in-volume-max="{$splitterMax}">
					<pre-content>
						<xsl:call-template name="coverPage"/>
					</pre-content>
					<post-content>
						<xsl:call-template name="postContentNotes"/>
					</post-content>
				</volume-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="insertFrontPageArea">
		<xsl:if test="count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]])>0">
			<page-area align="bottom" max-height="10" collection="footnotes-front">
				<fallback>
					<rename collection="footnotes-front" to="endnotes-front"/>
					<xsl:if test="count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])>0">
						<rename collection="footnotes" to="endnotesB"/>
					</xsl:if>
				</fallback>
				<before><leader position="100%" pattern="."/></before>
				<after></after>
			</page-area>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="insertMainPageArea">
		<xsl:if test="count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])>0">
			<page-area align="bottom" max-height="10" collection="footnotes">
				<fallback>
					<rename collection="footnotes" to="endnotes"/>
					<xsl:if test="count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]])>0">
						<rename collection="footnotes-front" to="endnotes-frontB"/>
					</xsl:if>
				</fallback>
				<before><leader position="100%" pattern="."/></before>
				<after></after>
			</page-area>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="postContentNotes">
		<xsl:if test="count(//dtb:note)>0">
			<dynamic-sequence master="notes">
				<block padding-top="3"><xsl:value-of select="$l10nEndnotesHeadling"/></block>
				<xsl:if test="count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]])>0">
					<list-of-references collection="endnotes-front" range="volume">
						<on-page-start>
							<block padding-top="1" keep="all" keep-with-next="1"><evaluate expression="(format &quot;{$l10nEndnotesPageStart}&quot; (numeral-format roman $started-page-number))"/></block>
						</on-page-start>
					</list-of-references>
					<xsl:if test="count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])>0">
						<list-of-references collection="endnotes-frontB" range="volume">
							<on-page-start>
								<block padding-top="1" keep="all" keep-with-next="1"><evaluate expression="(format &quot;{$l10nEndnotesPageStart}&quot; (numeral-format roman $started-page-number))"/></block>
							</on-page-start>
						</list-of-references>
					</xsl:if>
				</xsl:if>
				<xsl:if test="count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])>0">
					<list-of-references collection="endnotes" range="volume">
						<on-page-start>
							<block padding-top="1" keep="all" keep-with-next="1"><evaluate expression="(format &quot;{$l10nEndnotesPageStart}&quot; $started-page-number)"/></block>
						</on-page-start>
					</list-of-references>
					<xsl:if test="count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]])>0">
						<list-of-references collection="endnotesB" range="volume">
							<on-page-start>
								<block padding-top="1" keep="all" keep-with-next="1"><evaluate expression="(format &quot;{$l10nEndnotesPageStart}&quot; $started-page-number)"/></block>
							</on-page-start>
						</list-of-references>
					</xsl:if>
				</xsl:if>
			</dynamic-sequence>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="coverPage">
		<sequence master="cover">
			<xsl:choose>
				<xsl:when test="/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:doctitle">
					<block align="center" padding-top="3" padding-bottom="1" margin-left="2" margin-right="2"><xsl:value-of select="/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:doctitle"/></block>
				</xsl:when>
				<xsl:otherwise>
					<block align="center" padding-top="3"  margin-left="2" margin-right="2">&#x00a0;</block>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="count(/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:docauthor)>3">
					<block align="center"  margin-left="2" margin-right="2"><xsl:value-of select="/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:docauthor[0]"/></block>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:docauthor">
						<block align="center" margin-left="2" margin-right="2"><xsl:value-of select="."/></block>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<block align="center" margin-left="2" margin-right="2" vertical-align="before" vertical-position="100%" hyphenate="false"><evaluate expression="
			(if (&gt; $volumes 1) 
				(format &quot;{$l10nTocVolumeXofY}&quot; (int2text (round $volume) {$l10nLang}) (int2text (round $volumes) {$l10nLang}))
				&quot;{$l10nTocOneVolume}&quot;)"/></block>
		</sequence>
	</xsl:template>
	
	<xsl:template name="insertNoteCollection">
		<xsl:if test="count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]])>0">
			<collection name="footnotes-front">
				<xsl:apply-templates select="//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]]" mode="collectNotes">
					<xsl:with-param name="afix">.A</xsl:with-param>
				</xsl:apply-templates>
				<!-- 
				<item id="note1" text-indent="4">1).</item>  -->
			</collection>
		</xsl:if>
		<xsl:if test="count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])>0">
			<collection name="footnotes">
				<xsl:apply-templates select="//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]]" mode="collectNotes">
					<xsl:with-param name="afix">.B</xsl:with-param>
				</xsl:apply-templates>
				<!-- 
				<item id="note1" text-indent="4">1).</item>  -->
			</collection>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="dtb:noteref" priority="10">
		<xsl:apply-templates select="." mode="inline-mode"/>
		<xsl:variable name="afix">
			<xsl:choose>
				<xsl:when test="ancestor::dtb:frontmatter">.A</xsl:when>
				<xsl:otherwise>.B</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="starts-with(@idref, '#')"><anchor item="{concat(substring-after(@idref, '#'), $afix)}"/></xsl:when>
			<xsl:otherwise><xsl:message terminate="no">Only fragment identifier supported: <xsl:value-of select="@idref"/></xsl:message></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:note" mode="collectNotes">
			<xsl:param name="afix"/>
			<item id="{concat(@id, $afix)}">
				<xsl:variable name="note">
					<xsl:apply-templates/>
				</xsl:variable>
				<xsl:for-each select="$note/node()[self::* or self::text()]">
					<xsl:choose>
						<xsl:when test="self::text()">
							<xsl:message terminate="yes">Unexpected text contents in "note" element.</xsl:message>
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

	<!-- Don't output a sequence if there is nothing left when doctitle, docauthor and level1@class='backCoverText', level1@class='rearjacketcopy' and level1@class='colophon' has been moved -->
	<xsl:template match="dtb:frontmatter" mode="sequence-mode">
		<xsl:if test="(*[not(self::dtb:doctitle or self::dtb:docauthor or self::dtb:level1[@class='backCoverText' or @class='rearjacketcopy' or @class='colophon' or @class='toc' or dtb:list[@class='toc']])])
						and not(//dtb:level1[@class='toc'] or //dtb:level1[dtb:list[@class='toc']])"><!--  -->
			<sequence>
				<xsl:apply-templates select="." mode="apply-sequence-attributes"/>
				<xsl:apply-templates/>
			</sequence>
		</xsl:if>
	</xsl:template>
	
		<!-- Don't output a sequence if there is nothing left when level1@class='backCoverText', level1@class='rearjacketcopy' and level1@class='colophon' has been moved -->
	<xsl:template match="dtb:rearmatter" mode="sequence-mode">
		<xsl:if test="*[not(self::dtb:level1[@class='backCoverText' or @class='rearjacketcopy' or @class='colophon' or @class='toc' or dtb:list[@class='toc']
		or count(descendant::dtb:note)>0 and count(descendant::*[not(ancestor::dtb:note) and (self::dtb:level2 or self::dtb:level3 or self::dtb:level4 or self::dtb:level5 or self::dtb:level6 or self::dtb:h1 or self::dtb:h2 or self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6 or self::dtb:note or self::dtb:pagenum)])=count(descendant::*[not(ancestor::dtb:note)])
							])]"><!--  -->
			<sequence>
				<xsl:apply-templates select="." mode="apply-sequence-attributes"/>
				<xsl:apply-templates/>
			</sequence>
		</xsl:if>
	</xsl:template>
	
		<!-- Don't output a sequence if there is nothing left when doctitle, docauthor and level1@class='backCoverText', level1@class='rearjacketcopy' and level1@class='colophon' has been moved -->
	<xsl:template match="dtb:frontmatter" mode="pre-volume-mode">
		<xsl:if test="*[not(self::dtb:doctitle or self::dtb:docauthor or self::dtb:level1[@class='backCoverText' or @class='rearjacketcopy' or @class='colophon' or @class='toc' or dtb:list[@class='toc']])]">
			<sequence master="front">
				<block break-before="page">
					<xsl:apply-templates/>
					<!-- 
					<xsl:variable name="tree">
						<xsl:apply-templates/>
					</xsl:variable>
					<xsl:apply-templates select="$tree" mode="strip-id"/> -->
				</block>
			</sequence>
		</xsl:if>
	</xsl:template>

	<xsl:template match="*|comment()|processing-instruction()" mode="strip-id">
		<xsl:call-template name="copy-without-id"/>
	</xsl:template>
	
	<xsl:template name="copy-without-id">
		<xsl:copy>
			<xsl:copy-of select="@*[name()!='id']"/>
			<xsl:apply-templates mode="strip-id"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:level1[@class='toc']"></xsl:template>
	<xsl:template match="dtb:level1[dtb:list[@class='toc']]"></xsl:template>
	<xsl:template match="dtb:level1[@class='toc']" mode="toc"></xsl:template>
	<xsl:template match="dtb:level1[dtb:list[@class='toc']]" mode="toc"></xsl:template>

	<xsl:template match="dtb:level1[(@class='backCoverText' or @class='rearjacketcopy' or @class='colophon') and (parent::dtb:frontmatter or parent::dtb:rearmatter)]" mode="toc"></xsl:template>
	
	<xsl:template match="dtb:level1[
		count(descendant::dtb:note)>0 and
		count(descendant::*[not(ancestor::dtb:note) and (self::dtb:level2 or self::dtb:level3 or self::dtb:level4 or self::dtb:level5 or self::dtb:level6 or self::dtb:h1 or self::dtb:h2 or self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6 or self::dtb:note or self::dtb:pagenum)])
		=count(descendant::*[not(ancestor::dtb:note)])]" mode="toc"/>
	
	<xsl:template match="dtb:level1|dtb:level2" mode="toc">
		<xsl:if test="dtb:h1|dtb:h2">
<!--
			<xsl:choose>
				<xsl:when test="self::dtb:level1 and @class='part'"><toc-entry ref-id="{generate-id(.)}" padding-bottom="1" keep="all"><xsl:apply-templates select="dtb:h1" mode="toc-hd"/></toc-entry><xsl:apply-templates mode="toc"/></xsl:when>
				<xsl:otherwise>--><toc-entry ref-id="{generate-id(dtb:h1|dtb:h2)}" block-indent="{$toc-indent-multiplier}" text-indent="{2*$toc-indent-multiplier}" keep="all"><xsl:apply-templates select="dtb:h1|dtb:h2" mode="toc-hd"/><xsl:apply-templates mode="toc"/></toc-entry><!--</xsl:otherwise>
			</xsl:choose>-->
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="dtb:level3|dtb:level4|dtb:level5|dtb:level6" mode="toc">
		<xsl:if test="dtb:h3|dtb:h4|dtb:h5|dtb:h6">
			<toc-entry ref-id="{generate-id(dtb:h3|dtb:h4|dtb:h5|dtb:h6)}" block-indent="{$toc-indent-multiplier}" text-indent="{$toc-indent-multiplier}" keep="all"><xsl:apply-templates select="dtb:h3|dtb:h4|dtb:h5|dtb:h6" mode="toc-hd"/>
			<xsl:if test="dtb:level3 and ancestor::dtb:level1[@class='part']"><xsl:apply-templates mode="toc"/></xsl:if>
			</toc-entry>
			<xsl:if test="not(dtb:level3 and ancestor::dtb:level1[@class='part'])"><xsl:apply-templates mode="toc"/></xsl:if>
		</xsl:if>
	</xsl:template>
	<!--
	<xsl:template name="addBottomMarginIfPart">
	
		<xsl:if test="(following::*[self::dtb:level1|self::dtb:level2|self::dtb:level3|self::dtb:level4|self::dtb:level5|self::dtb:level6][1])[self::dtb:level1[@class='part']]">
			<xsl:attribute name="padding-bottom">1</xsl:attribute>
		</xsl:if>
	</xsl:template>
-->
	
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6" mode="toc-hd">
<!--		<xsl:value-of select="descendant::text()"/>-->
	<xsl:apply-templates mode="toc-text"/>
	<!-- <xsl:if test="not(self::dtb:h1 and ancestor::dtb:level1[@class='part'])"> -->
		<xsl:text> (</xsl:text><xsl:value-of select="preceding::dtb:pagenum[1]/text()"/><xsl:text>) </xsl:text><leader position="100%" align="right" pattern="."/><page-number ref-id="{generate-id(.)}"><xsl:if test="ancestor::dtb:frontmatter"><xsl:attribute name="style">roman</xsl:attribute></xsl:if></page-number>
		<!--  </xsl:if>  -->
	</xsl:template>

	<xsl:template match="*" mode="toc-text">
		<xsl:apply-templates mode="toc-text"/>
	</xsl:template>
	<xsl:template match="text()" mode="toc-text">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="dtb:br" mode="toc-text">
		<xsl:text> </xsl:text>
	</xsl:template>
	
	<xsl:template match="node()" mode="toc"/>

</xsl:stylesheet>
