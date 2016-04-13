<?xml version="1.0" encoding="utf-8"?>
<?xslt-doc-file doc-files/dtb2obfl_braille.html?>
<!--
	TODO:
		- komplexa sub, sup
		- länkar, e-postadresser
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="dtb xs obfl"
	xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
	xmlns="http://www.daisy.org/ns/2011/obfl"
	xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias">

	<xsl:import href="dtbook2obfl_layout.xsl" />
	<xsl:import href="dtbook_table_grid.xsl" />
	<xsl:import href="book-formats.xsl"/>
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>
	<xsl:param name="toc-indent-multiplier" select="1"/>
	<xsl:param name="splitterMax" select="10"/>
	<xsl:param name="toc-depth" select="6"/>
	<xsl:param name="volume-toc" as="xs:boolean" select="true()"/>
	<xsl:param name="show-braille-page-numbers" as="xs:boolean" select="true()"/>
	<xsl:param name="show-print-page-numbers" as="xs:boolean" select="true()"/>
	<xsl:param name="table-split-columns" select="10"/>

	<xsl:param name="l10nLang" select="'en'"/>
	<xsl:param name="l10nTocHeadline" select="'Table Of Contents'"/>
	<xsl:param name="l10nTocDescription" select="''"/>
	<xsl:param name="l10nTocVolumeStart" select="'Volume {0}'"/>
	<xsl:param name="l10nTocVolumeHeading" select="'Contents of Volume {0}'"/>
	<xsl:param name="l10nTocVolumeXofY" select="'Volume {0} of {1}'"/>
	<xsl:param name="l10nTocOneVolume" select="'One Volume'"/>
	<xsl:param name="l10nEndnotesHeading" select="'Footnotes'"/>
	<xsl:param name="l10nEndnotesPageStart" select="'Page {0}'"/>
	<xsl:param name="l10nEndnotesPageHeader" select="'Footnotes'"/>
	<xsl:param name="l10ntable" select="'Table'"/>
	<xsl:param name="l10ntablepart" select="'Table part'"/>
	
	<xsl:key name="noterefs" match="dtb:noteref" use="substring-after(@idref, '#')"/>

	<xsl:template match="/">
		<xsl:variable name="result">
		<obfl version="2011-1" hyphenate="{$hyphenate}">
			<xsl:attribute name="xml:lang"><xsl:value-of select="/dtb:dtbook/@xml:lang"/></xsl:attribute>
			<xsl:call-template name="insertMetadata"/>
			<xsl:call-template name="insertLayoutMaster"/>
			<xsl:call-template name="insertProcessorRenderer"/>
			<xsl:call-template name="insertTOCVolumeTemplate"/>
			<xsl:call-template name="insertNoteCollection"/>
			<xsl:apply-templates/>
		</obfl>
		</xsl:variable>
		<xsl:apply-templates select="$result" mode="breakOutXMLData"/>
	</xsl:template>
	
	<xsl:template match="obfl:xml-data" mode="breakOutXMLData">
		<xsl:for-each select="ancestor::obfl:*[ancestor::obfl:sequence]">
			<xsl:text disable-output-escaping="yes">&lt;/</xsl:text><xsl:value-of select="local-name()"/>
			<xsl:text disable-output-escaping="yes">></xsl:text>
		</xsl:for-each>
		<xsl:copy-of select="."/>
		<xsl:for-each select="ancestor::obfl:*[ancestor::obfl:sequence]">
			<xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:value-of select="local-name()"/>
			<xsl:for-each select="@*[not(name()='id' or name()='break-before')]">
				<xsl:value-of select="concat(' ', name(), '=', '&quot;', . , '&quot;')"></xsl:value-of>
			</xsl:for-each>
			<xsl:text disable-output-escaping="yes">></xsl:text>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*|processing-instruction()|comment()" mode="breakOutXMLData">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates mode="breakOutXMLData"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="insertLayoutMaster">
		<xsl:copy-of select="obfl:insertLayoutMaster(
			count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]]), 
			count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]]))"/>
	</xsl:template>
	
	<xsl:template name="insertTOCVolumeTemplate">
		<xsl:variable name="insertToc" select="$toc-depth > 0 and (//dtb:level1[@class='toc'] or //dtb:level1[dtb:list[@class='toc']])" as="xs:boolean"/>
		<xsl:if test="$insertToc">
			<table-of-contents name="full-toc">
				<xsl:apply-templates select="//dtb:level1" mode="toc"/>
			</table-of-contents>
		</xsl:if>
		<xsl:variable name="additionalPreContent"><xsl:if test="$insertToc"><xsl:apply-templates select="//dtb:frontmatter" mode="pre-volume-mode"/></xsl:if></xsl:variable>
		<xsl:copy-of select="obfl:insertVolumeTemplate(
			/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:doctitle,
			/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:docauthor,
			count(//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]]),
			count(//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]]),
			$insertToc,
			$additionalPreContent)"/>
	</xsl:template>

	<xsl:template name="insertNoteCollection">
		<xsl:copy-of select="obfl:insertNoteCollection(
			//dtb:note[key('noterefs', @id)[ancestor::dtb:frontmatter]],
			//dtb:note[key('noterefs', @id)[not(ancestor::dtb:frontmatter)]])"/>
	</xsl:template>
	
	<xsl:template match="dtb:pagenum">
		<xsl:if test="$show-print-page-numbers">
			<xsl:next-match/>
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
						and ($toc-depth=0 or not(//dtb:level1[@class='toc'] or //dtb:level1[dtb:list[@class='toc']]))"><!--  -->
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
	
	<xsl:template match="dtb:level1[@class='toc' or dtb:list[@class='toc']]"></xsl:template>
	<xsl:template match="dtb:level1[@class='toc' or dtb:list[@class='toc']]" mode="toc"></xsl:template>

	<xsl:template match="dtb:level1[(@class='backCoverText' or @class='rearjacketcopy' or @class='colophon') and (parent::dtb:frontmatter or parent::dtb:rearmatter)]" mode="toc"></xsl:template>
	
	<xsl:template match="dtb:level1[
		count(descendant::dtb:note)>0 and
		count(descendant::*[not(ancestor::dtb:note) and (self::dtb:level2 or self::dtb:level3 or self::dtb:level4 or self::dtb:level5 or self::dtb:level6 or self::dtb:h1 or self::dtb:h2 or self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6 or self::dtb:note or self::dtb:pagenum)])
		=count(descendant::*[not(ancestor::dtb:note)])]" mode="toc"/>
	
	<xsl:template match="dtb:level2" mode="toc" priority="0.6">
		<xsl:if test="$toc-depth > 1">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="dtb:level3" mode="toc" priority="0.6">
		<xsl:if test="$toc-depth > 2">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="dtb:level4" mode="toc" priority="0.6">
		<xsl:if test="$toc-depth > 3">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="dtb:level5" mode="toc" priority="0.6">
		<xsl:if test="$toc-depth > 4">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="dtb:level6" mode="toc" priority="0.6">
		<xsl:if test="$toc-depth > 5">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>
	
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
	<xsl:if test="$show-print-page-numbers">
		<xsl:text> (</xsl:text><xsl:value-of select="preceding::dtb:pagenum[1]/text()"/><xsl:text>)</xsl:text>
	</xsl:if>
	<xsl:if test="$show-braille-page-numbers">
		<xsl:text> </xsl:text><leader position="100%" align="right" pattern="."/><page-number ref-id="{generate-id(.)}"><xsl:if test="ancestor::dtb:frontmatter"><xsl:attribute name="number-format">roman</xsl:attribute></xsl:if></page-number>
	</xsl:if>
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
	
	<xsl:template name="insertProcessorRenderer">
		<xsl:copy-of select="obfl:insertProcessorRenderer()"/>
	</xsl:template>

	<xsl:template match="dtb:table">
		<xml-data renderer="table-renderer" xmlns:dotify="http://brailleapps.github.io/ns/dotify">
			<dotify:node>
				<block keep="all" keep-with-next="1"><xsl:value-of select="concat('== ', $l10ntable, ' ')"/><leader position="100%" pattern="="/></block>
				<xsl:variable name="table">
					<xsl:apply-templates select="." mode="splitTable">
						<xsl:with-param name="maxColumns" select="$table-split-columns"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:apply-templates select="dtb:caption"/>
				<xsl:for-each select="$table/dtb:table">
					<xsl:apply-templates select="." mode="matrixTable"/>
					<xsl:if test="following-sibling::dtb:table">
						<block keep="all" keep-with-next="1"><xsl:value-of select="concat(':: ', $l10ntablepart, ' ')"/><leader position="100%" pattern=":"/></block>
					</xsl:if>
				</xsl:for-each>
				<block><leader align="right" position="100%" pattern="="/></block>
				<xsl:apply-templates select="descendant::dtb:pagenum"/>
			</dotify:node>
		</xml-data>
	</xsl:template>
	
	<xsl:template match="dtb:table" mode="matrixTable">
		<table table-col-spacing="1">
			<xsl:choose>
				<xsl:when test="dtb:thead"> 
					<thead>
						<xsl:apply-templates select="dtb:thead"/>
					</thead>
					<tbody>
						<xsl:apply-templates select="dtb:tbody/dtb:tr" mode="matrixRow"/>
						<xsl:apply-templates select="dtb:tr" mode="matrixRow"/>
						<!-- pagenums are moved after the table -->
						<xsl:apply-templates select="dtb:tfoot/dtb:tr" mode="matrixRow"/>
					</tbody>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="dtb:tbody/dtb:tr" mode="matrixRow"/>
					<xsl:apply-templates select="dtb:tr" mode="matrixRow"/>
					<!-- pagenums are moved after the table -->
					<xsl:apply-templates select="dtb:tfoot/dtb:tr" mode="matrixRow"/>
				</xsl:otherwise>
			</xsl:choose>
		</table>
	</xsl:template>
	
	<xsl:template match="dtb:tr" mode="matrixRow">
		<tr>
			<xsl:apply-templates mode="matrixCell"/>
		</tr>
	</xsl:template>
	
	<xsl:template match="dtb:td | dtb:th" mode="matrixCell">
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

</xsl:stylesheet>