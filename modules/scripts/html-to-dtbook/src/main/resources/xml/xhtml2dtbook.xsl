<?xml version="1.0" encoding="UTF-8"?>
<!--
  org.daisy.util (C) 2005-2008 Daisy Consortium
  
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
--> 
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.daisy.org/z3986/2005/dtbook/" xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:pfunc="http://www.daisy.org/pipeline/xslt/function" exclude-result-prefixes="xs html pfunc">


	<xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-public="-//NISO//DTD dtbook 2005-2//EN" doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd"/>

	<!--  Input paramerets -->
	<xsl:param name="uid" as="xs:string" select="'[UID]'"/>
	<!-- uid of publication -->
	<xsl:param name="title" as="xs:string" select="'[DTB_TITLE]'"/>
	<!-- title of publication -->
	<xsl:param name="cssURI" as="xs:string" select="'[cssURI]'"/>
	<!-- URI to CSS of publication -->
	<xsl:param name="nccURI" as="xs:string" select="'[nccURI]'"/>
	<!-- URI to D202 NCC file -->
	<xsl:param name="transferDcMetadata" as="xs:string" select="'false'"/>
	<!-- transfer dc:* metadata from ncc file -->
	<xsl:param name="transformationMode" as="xs:string" select="'standalone'"/>
	<!-- 'standalone' for pure xhtml2dtbook, or 'DTBmigration' for DAISY 2.02 content doc to dtbook transform -->

	<xsl:variable name="nccURI.mod" as="xs:string" select="translate($nccURI,'\','/')"/>
	<xsl:variable name="cssURI.mod" as="xs:string" select="replace(translate($cssURI,'\','/'),'.*/(.+)','$1')"/>
	<!-- first, change \ to / and then remove everything up to and inlduing the last /-->
	<xsl:variable name="dtbMigration" as="xs:boolean" select="matches($transformationMode,'DTBmigration','i')"/>

	<xsl:variable name="regexpMatchTitle" as="xs:string" select="'^title$|^dc:title$|^dtb:title$'"/>
	<!--  Possible formats of title representation in metadata  -->
	<xsl:variable name="regexpMatchID" as="xs:string" select="'^id$|^dc:id$|^dtb:id$|^uid$|^dc:uid$|^dtb:uid$|^identifier$|^dc:identifier$|^dtb:identifier$'"/>
	<!--  Possible formats of id representation in metadata  -->
	<xsl:variable name="regexpMatchTitleAndID" as="xs:string" select="concat($regexpMatchTitle,'|',$regexpMatchID)"/>

	<xsl:variable name="smil" as="xs:string" select="'.smil#'"/>

	<xsl:variable name="xTitle" as="xs:string">
		<xsl:choose>
			<xsl:when test="$title eq '' or $title eq '[DTB_TITLE]'">
				<!-- unspecfied by user, so we could try to find it in the xhtml source doc -->
				<xsl:choose>
					<xsl:when test="html:html/html:head/html:title">
						<!-- There is a title element -->
						<xsl:value-of select="html:html/html:head/html:title"/>
					</xsl:when>
					<xsl:when test="//html:meta[matches(@name,$regexpMatchTitle,'i')]">
						<!-- There is some metadata represnting a title -->
						<xsl:value-of select="//html:meta[matches(@name,$regexpMatchTitle,'i')][1]/@content"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- Nothing usable in the source, so leave it empty -->
						<xsl:value-of select="''"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- specified by user, so let's use that one as title -->
				<xsl:value-of select="$title"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:variable name="xUID" as="xs:string">
		<xsl:choose>
			<xsl:when test="$uid eq '' or $uid eq '[UID]'">
				<!-- unspecfied by user, so we could try to find it in the xhtml source doc -->
				<xsl:choose>
					<xsl:when test="//html:meta[matches(@name,$regexpMatchID,'i')]">
						<!-- There is some metadata represnting a title -->
						<xsl:value-of select="//html:meta[matches(@name,$regexpMatchID,'i')][1]/@content"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- Nothing usable in the source, so leave it empty -->
						<xsl:value-of select="''"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- specified by user, so let's use that one as UID -->
				<xsl:value-of select="$uid"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>


	<xsl:template match="/">
		<!-- Processing instruction: Use CSS to display the DTBook XML -->
		<xsl:if test="$cssURI ne '[cssURI]' and $cssURI.mod ne ''">
			<xsl:processing-instruction name="xml-stylesheet">
				<xsl:text> type="text/css" href="</xsl:text>
				<xsl:value-of select="$cssURI.mod"/>
				<xsl:text>"</xsl:text>
			</xsl:processing-instruction>
		</xsl:if>
		<dtbook version="2005-3">
			<xsl:copy-of select="html:html/@xml:lang"/>
			<xsl:apply-templates>
				<xsl:with-param name="level" select="'document'" tunnel="yes"/>
			</xsl:apply-templates>
		</dtbook>
	</xsl:template>

	<xsl:template match="html:head">
		<head>
			<!-- 		<meta name="transformationMode" content="{$transformationMode}" />
			<meta name="dtbMigration" content="{$dtbMigration}" />
			<meta name="transferDcMetadata" content="{$transferDcMetadata}" />
			<meta name="nccURI" content="{$nccURI}" />
			<meta name="nccURI.mod" content="{$nccURI.mod}" /> 
			<meta name="cssURI" content="{$cssURI}" />
			<meta name="cssURI.mod" content="{$cssURI.mod}" /> 
			<meta name="title" content="{$title}" />
			<meta name="xTitle" content="{$xTitle}" />
			<meta name="uid" content="{$uid}" />
			<meta name="xUID" content="{$xUID}" /> -->
			<!-- 		<xsl:comment>
				transformationMode: <xsl:value-of select="$transformationMode" />
				title: <xsl:value-of select="$title" />
				xTitle: <xsl:value-of select="$xTitle" />
				uid: <xsl:value-of select="$uid" />
				xUID: <xsl:value-of select="$xUID" />
			</xsl:comment> -->
			<meta name="dtb:uid" content="{$xUID}"/>
			<meta name="dc:Title" content="{$xTitle}"/>
			<xsl:choose>
				<xsl:when test="
						$dtbMigration
						and matches($transferDcMetadata,'true','i') 
						and doc-available($nccURI.mod)
						and $nccURI ne '[nccURI]'">
					<!-- If requested, and we are doing dtbMigration and if ncc.html can be found, 
						transfer dc:* metadata (but not things that might look like a title or an id,
						and not dc:format either as its content will be wrong) from ncc.html -->
					<xsl:apply-templates
						select="
						doc($nccURI.mod)//html:head/html:meta[
							starts-with(@name,'dc:') 
							and 
							not(
								matches(
									@name,
									concat(
										$regexpMatchTitleAndID,
										'|^dc:format$'
										),
									'i'
								)
							)
						]"
						mode="metadata-from-ncc"/>
				</xsl:when>
				<xsl:otherwise>
					<!-- If not migrating, not requested, or ncc.html not found, 
						use whatever is in the XHTML, except things that might look like a title or an id, 
						as they are handled above-->
					<xsl:apply-templates select="html:meta[not(matches(@name,$regexpMatchTitleAndID,'i'))]"/>
				</xsl:otherwise>
			</xsl:choose>
		</head>
	</xsl:template>

	<xsl:template match="html:meta[@http-equiv]" priority="10"/>
	<!-- get rid of this one -->

	<xsl:template match="html:meta[@name]">
		<meta>
			<xsl:attribute name="name">
				<xsl:choose>
					<xsl:when test="matches(@name,'^dc:(creator|subject|description|publisher|contributor|date|type|format|source|language|relation|coverage|rights)$','i')">
						<xsl:value-of select="concat('dc:',pfunc:initialCaps(substring-after(@name,'dc:')))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@name"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:copy-of select="@content, @scheme"/>
		</meta>
	</xsl:template>

	<xsl:template match="html:meta[not(@name)]">
		<meta>
			<xsl:copy-of select="@name, @content, @scheme"/>
		</meta>
	</xsl:template>

	<xsl:template match="html:meta" mode="metadata-from-ncc">
		<xsl:if test="matches(@name,'^dc:(creator|subject|description|publisher|contributor|date|type|source|language|relation|coverage|rights)$','i')">
			<meta>
				<xsl:attribute name="name" select="concat('dc:',pfunc:initialCaps(substring-after(@name,'dc:')))"/>
				<xsl:copy-of select="@content, @scheme, @http-equiv"/>
			</meta>
		</xsl:if>
	</xsl:template>

	<xsl:template match="html:link">
		<link rel="stylesheet" type="text/css" href="{@href}"/>
	</xsl:template>

	<!-- In the following template, the various h1 elements are associated with the proper
	frontmatter, bodymatter og rearmatter element -->
	<xsl:template match="html:body">
		<book>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:if test="html:h1[@class eq 'frontmatter'] or //html:head/html:title">
				<frontmatter>
					<!-- Assuming that the title element in head represents the title -->
					<xsl:apply-templates select="//html:head/html:title"/>
					<xsl:apply-templates select=".//html:h1[tokenize(@class,' ')='frontmatter']"/>
				</frontmatter>
			</xsl:if>
			<bodymatter>
				<xsl:apply-templates select=".//html:h1[not(tokenize(@class,' ')=('frontmatter','rearmatter'))]"/>
				<!--<xsl:apply-templates/>-->
			</bodymatter>
			<xsl:if test="html:h1[@class eq 'rearmatter']">
				<rearmatter>
					<xsl:apply-templates select=".//html:h1[tokenize(@class,' ')='rearmatter']"/>
				</rearmatter>
			</xsl:if>
		</book>
	</xsl:template>

	<xsl:template match="html:title">
		<doctitle>
			<xsl:apply-templates/>
		</doctitle>
	</xsl:template>

	<!-- The following template handles everything concerning levels in the DTBook -->
	<xsl:template match="element()[matches(local-name(),'^h[1-6]$')]">
		<!-- The purpose of this template:
			1) Create the proper levelx element
			2) Create the proper hx element
			3) Process all elements up to the next heading (may be none)
			4) Apply templates for the following h[x+1] elements (may be none) up to the next hx element
		-->
		<!-- Name of current element (heading) -->
		<xsl:variable name="h.this.name" as="xs:string" select="local-name()"/>
		<!-- Get the level -->
		<xsl:variable name="h.this.level" as="xs:integer" select="xs:integer(substring-after($h.this.name,'h'))"/>
		<!-- Get the next heading (no matter the level), assuming it is a sibling of the current element. 
			This requirement is stated in the documentation -->
		<xsl:variable name="h.next" select="following-sibling::*[matches(local-name(),'^h[1-6]$')][1]"/>
		<!-- Get the name of the next heading -->
		<xsl:variable name="h.next.name" select="local-name($h.next)"/>
		<!-- NOT USED: Get the level for the next element. 0 if there is no next element 
		<xsl:variable name="h.next.level" as="xs:integer" select="
			if ($h.next)
			then xs:integer(substring-after($h.next.name,'h'))
			else 0" /> -->
		<!-- Get all the following sibling elements (may be none) up to the next heading. 
			If there is no next heading, then get all the following siblings  -->
		<xsl:variable name="e.up-to-next-heading" select="
			if ($h.next)
			then following-sibling::node()[. &lt;&lt; $h.next]
			else following-sibling::node()"/>
		<!-- Get the next heading on the same, or higher, level as the current heading, 
			assuming it is a sibling of the current element. This requirement is stated in the documentation -->
		<xsl:variable name="h.next-on-same-level-or-higher" select="following-sibling::*[matches(local-name(),concat('^h[1-',string($h.this.level),']$'))][1]"/>
		<!-- Get all following headings on the next level, ie if the current is a h3 then look for h4 -->
		<xsl:variable name="h.next-level.all" select="following-sibling::*[matches(local-name(),concat('^h',string($h.this.level + 1),'$'))]"/>
		<!-- Get only following headings (may be none) on the next level, 
			located before the next heading on the same level or higher -->
		<xsl:variable name="h.next-level.relevant" select="
			if ($h.next-on-same-level-or-higher)
			then $h.next-level.all[. &lt;&lt; $h.next-on-same-level-or-higher]
			else $h.next-level.all"/>
		<!-- Are there any elements or text nodes (except hr and br) before the next heading: -->
		<xsl:variable name="NoRelevantElementsUntilNextHeading" as="xs:boolean" select="every $e in $e.up-to-next-heading satisfies (if (self::*) then matches(local-name($e),'^br$|^hr$') else string-length(normalize-space(.)) &gt; 0)"/>
		<!-- Is the next sibling (ignoring hr and br elements when deciding who's next) a heading on a lower level: -->
		<xsl:variable name="NextFollowingSiblingIsHeadingOnLowerLevel" as="xs:boolean" select="matches(local-name(following-sibling::*[not(matches(local-name(.),'^br$|^hr$'))][1]),concat('^h[',string($h.this.level+1),'-6]$'))"/>
		<!-- Create the levelx element -->
		<xsl:element name="{concat('level',string($h.this.level))}">
			<xsl:copy-of select="@class"/>
			<!-- Transfer the class attribute from the hx element to the levelx element -->
			<!-- Create the hx element -->
			<xsl:element name="{local-name()}">
				<!--				<xsl:call-template name="smilref" />-->
				<xsl:call-template name="copy-heading-attributes"/>
				<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
				<xsl:apply-templates>
					<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:element>
			<!-- DEBUG:		<div class="h-info">
				<strong>This heading:</strong><br />
				Name: <xsl:value-of select="$h.this.name" /><br />
				Level: <xsl:value-of select="$h.this.level" /><br />
				Content: <xsl:value-of select="." /><br />
				<strong>Next heading:</strong><br />
				Name: <xsl:value-of select="$h.next.name" /><br />
				Level: <xsl:value-of select="$h.next.level" /><br />
				Content: <xsl:value-of select="$h.next" /><br />
				<strong>Next heading on the same, or higher, level:</strong><br />
				Content: <xsl:value-of select="$h.next-on-same-level-or-higher" /><br />
				<strong>Following headings on level <xsl:value-of select="$h.this.level + 1" />:</strong><br />
				Count: <xsl:value-of select="count($h.next-level.all)" /><br />
				Number of relevant: <xsl:value-of select="count($h.next-level.relevant)" /><br />
				<strong>Relevant children:</strong><br />
				Count: <xsl:value-of select="count($e.up-to-next-heading)" />
			</div>-->

			<!-- Apply templates for all elements up to the next heading -->
			<xsl:for-each-group select="$e.up-to-next-heading"
				group-adjacent="self::text() or self::a or self::abbr or self::acronym or self::annoref or self::bdo or self::br or self::cite or self::code or self::dfn or self::dl or self::em or self::img or self::imggroup or self::kbd or self::list or self::noteref or self::pagenum or self::prodnote or self::q or self::samp or self::sent or self::strong or self::sub or self::sup or self::span or self::w">
				<xsl:choose>
					<xsl:when test="current-grouping-key()">
						<!-- wrap loose nodes in paragraphs -->
						<p>
							<xsl:for-each select="current-group()">
								<xsl:choose>
									<xsl:when test="self::*">
										<xsl:apply-templates select=".">
											<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
										</xsl:apply-templates>
									</xsl:when>
									<xsl:when test="string-length(normalize-space(.)) &gt; 0">
										<xsl:value-of select="normalize-space(.)"/>
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</p>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="current-group()">
							<xsl:choose>
								<xsl:when test="self::*">
									<xsl:apply-templates select=".">
										<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
									</xsl:apply-templates>
								</xsl:when>
								<xsl:when test="string-length(normalize-space(.)) &gt; 0">
									<xsl:value-of select="normalize-space(.)"/>
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each-group>

			<!-- If there are no relevant elements until the next heading, add a dummy element (to satisfy DTBook).
				However if the next relevant sibling is a heading on a lower level, then don't add the dummy element
			  -->
			<xsl:if test="$NoRelevantElementsUntilNextHeading and not($NextFollowingSiblingIsHeadingOnLowerLevel)">
				<p class="dummy"/>
			</xsl:if>
			<!-- Apply templates for all headings on the next level -->
			<xsl:apply-templates select="$h.next-level.relevant">
				<xsl:with-param name="level" select="'block'" tunnel="yes"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>

	<xsl:template match="html:span[starts-with(@class,'page-')]">
		<pagenum page="{substring-after(@class,'page-')}" id="{if (@id) then @id else concat('page-',normalize-space(.))}">
			<xsl:call-template name="copy-page-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:value-of select="normalize-space(.)"/>
		</pagenum>
	</xsl:template>

	<xsl:template match="html:span[@class eq 'sentence']">
		<xsl:variable name="e.next" select="following-sibling::*[1]"/>
		<sent>
			<xsl:call-template name="copy-span-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates>
				<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
			</xsl:apply-templates>
		</sent>
	</xsl:template>

	<xsl:template match="html:span[ends-with(@class,'-prodnote')]">
		<xsl:variable name="e.p.1" select="preceding-sibling::*[1]"/>
		<xsl:variable name="e.p.2" select="preceding-sibling::*[2]"/>
		<xsl:variable name="part-of-imggroup" as="xs:boolean" select="
			local-name($e.p.1) eq 'img'
			or
			(	
				local-name($e.p.1) eq 'span' and $e.p.1/@class eq 'caption' and local-name($e.p.2) eq 'img'
			)"/>
		<xsl:choose>
			<xsl:when test="$part-of-imggroup"/>
			<xsl:otherwise>
				<prodnote render="{substring-before(@class,'-prodnote')}">
					<xsl:call-template name="copy-span-attributes"/>
					<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
					<xsl:apply-templates/>
				</prodnote>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="html:span[ends-with(@class,'-prodnote')]" mode="inside-imgrp">
		<xsl:param name="group-id" as="xs:string"/>
		<prodnote render="{substring-before(@class,'-prodnote')}" imgref="{concat('img-',$group-id)}" id="{concat('pnote-',$group-id)}">
			<xsl:call-template name="copy-std-attr"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<!--			<xsl:comment>TREFF</xsl:comment>-->
			<xsl:apply-templates/>
		</prodnote>
	</xsl:template>

	<xsl:template match="html:span[@class eq 'caption']">
		<xsl:variable name="e.p.1" select="preceding-sibling::*[1]"/>
		<xsl:variable name="e.p.2" select="preceding-sibling::*[2]"/>
		<xsl:variable name="part-of-imggroup" as="xs:boolean" select="
			local-name($e.p.1) eq 'img'
			or
			(	
				local-name($e.p.1) eq 'span' and ends-with($e.p.1/@class,'-prodnote') and local-name($e.p.2) eq 'img'
			)"/>
		<xsl:choose>
			<xsl:when test="$part-of-imggroup"/>
			<!-- The element his handled by the template for img elements -->
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="html:span[@class eq 'caption']" mode="inside-imgrp">
		<xsl:param name="group-id" as="xs:string"/>
		<caption imgref="{concat('img-',$group-id)}" id="{concat('caption-',$group-id)}">
			<xsl:call-template name="copy-std-attr"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</caption>
	</xsl:template>

	<xsl:template match="html:body/html:span[@class eq 'noteref']" priority="5">
		<!-- If the noteref is direct child of html:body, then wrap into a paragraph, and use next match -->
		<p class="noteref html-span">
			<xsl:next-match/>
		</p>
	</xsl:template>

	<xsl:template match="html:span[@class eq 'noteref']" priority="3">
		<noteref>
			<xsl:attribute name="idref" select="@bodyref
(:				if (contains(@bodyref,'#'))
				then substring-after(@bodyref,'#')
				else @bodyref :)"/>
			<xsl:copy-of select="@id"/>
			<xsl:call-template name="copy-std-attr"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</noteref>
	</xsl:template>

	<xsl:template match="html:a[contains(@href,$smil)]">
		<xsl:choose>
			<xsl:when test="$dtbMigration">
				<!-- This is the a element used to represent a reference to a SMIL file in a DAISY 2.02 content doc.
					We don't need it in the DTBook, as the @href will end up as a @smilref in the parent element.
					This is handled by the named template 'copy-std-attr' -->
				<xsl:apply-templates>
					<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<!--  If not doing a DTB migration, then use the next matching template -->
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="html:div[@class eq 'notebody']">
		<note>
			<xsl:copy-of select="@id"/>
			<xsl:call-template name="copy-std-attr"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</note>
	</xsl:template>

	<xsl:template match="html:img">
		<xsl:variable name="e.f.1" select="following-sibling::*[1]"/>
		<xsl:variable name="e.f.2" select="following-sibling::*[2]"/>
		<xsl:variable name="imggrp.associate.first" as="xs:boolean" select="
				local-name($e.f.1) eq 'span' and ends-with($e.f.1/@class,'-prodnote')
				or
				local-name($e.f.1) eq 'span' and $e.f.1/@class eq 'caption'
				"/>
		<xsl:variable name="imggrp.associate.second" as="xs:boolean"
			select="
				(	local-name($e.f.1) eq 'span' and ends-with($e.f.1/@class,'-prodnote')
					and
					local-name($e.f.2) eq 'span' and $e.f.2/@class eq 'caption'
				)
				or
				(	local-name($e.f.2) eq 'span' and ends-with($e.f.2/@class,'-prodnote')
					and
					local-name($e.f.1) eq 'span' and $e.f.1/@class eq 'caption'
				)
				"/>
		<xsl:choose>
			<xsl:when test="$imggrp.associate.first">
				<xsl:variable name="id" as="xs:string" select="generate-id()"/>
				<imggroup id="{concat('imggrp-',$id)}">
					<img>
						<xsl:copy-of select="@src, @alt"/>
						<xsl:call-template name="copy-attributes-not-id"/>
						<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
						<xsl:attribute name="id" select="concat('img-',$id)"/>
					</img>
					<xsl:apply-templates select="$e.f.1" mode="inside-imgrp">
						<xsl:with-param name="group-id" as="xs:string" select="$id"/>
					</xsl:apply-templates>
					<xsl:if test="$imggrp.associate.second">
						<xsl:apply-templates select="$e.f.2" mode="inside-imgrp">
							<xsl:with-param name="group-id" as="xs:string" select="$id"/>
						</xsl:apply-templates>
					</xsl:if>
				</imggroup>
			</xsl:when>
			<xsl:otherwise>
				<img>
					<xsl:copy-of select="@src, @alt"/>
					<xsl:call-template name="copy-attributes"/>
					<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
				</img>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="html:ol | html:ul">
		<list type="{local-name()}">
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</list>
	</xsl:template>

	<xsl:template match="html:p | html:div | html:blockquote">
		<xsl:param name="level" tunnel="yes"/>
		<xsl:element name="{if ($level='inline') then 'span' else local-name()}">
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates>
				<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
			</xsl:apply-templates>
		</xsl:element>
	</xsl:template>

	<xsl:template match="html:a | html:li | html:dl | html:dt | html:dd | html:span | html:strong | html:em | html:sub | html:sup | html:br | html:abbr">
		<!--<xsl:param name="level" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="not($level='inline')">
				<p>
					<xsl:element name="{local-name()}">
						<xsl:call-template name="copy-attributes"/>
						<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
						<xsl:apply-templates/>
					</xsl:element>
				</p>
			</xsl:when>
			<xsl:otherwise>-->
				<xsl:element name="{local-name()}">
					<xsl:call-template name="copy-attributes"/>
					<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
					<xsl:apply-templates>
						<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:element>
			<!--</xsl:otherwise>
		</xsl:choose>-->
	</xsl:template>

	<xsl:template match="html:div[html:span]">
		<div>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<p class="inserted-by-transformer html-p">
				<xsl:apply-templates>
					<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
				</xsl:apply-templates>
			</p>
		</div>
	</xsl:template>

	<!--<xsl:template match="html:body/html:br">
		<!-\- 		<xsl:comment>br elements as direct child of body is removed</xsl:comment> -\->
	</xsl:template>-->

	<xsl:template match="html:table | html:table/html:caption | html:tr | html:td | html:th | html:col">
		<xsl:element name="{local-name()}">
			<xsl:call-template name="copy-table-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>


	<xsl:template name="copy-attributes">
		<xsl:copy-of select="@id, @class, @href"/>
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-attributes-not-id">
		<xsl:copy-of select="@class, @href"/>
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-heading-attributes">
		<xsl:copy-of select="@id"/>
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-table-attributes">
		<xsl:copy-of select="@id, @rowspan, @colspan, @class, @valign"/>
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-span-attributes">
		<xsl:copy-of select="@id"/>
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-page-attributes">
		<xsl:call-template name="copy-std-attr"/>
	</xsl:template>
	<xsl:template name="copy-std-attr">
		<xsl:copy-of select="@title, @dir, @xml:lang"/>
		<xsl:call-template name="smilref"/>
	</xsl:template>

	<xsl:template name="smilref">
		<!-- 	Per Sennels, 20071026: Handle the html:a/@href attribute, if present and if we are doing a DTB mIgration-->
		<xsl:if test="$dtbMigration and contains(html:a[1]/@href,$smil)">
			<xsl:attribute name="smilref" select="html:a[1]/@href"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="html:script"/>

	<xsl:template match="html:b">
		<em>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates>
				<xsl:with-param name="level" select="'inline'" tunnel="yes"/>
			</xsl:apply-templates>
		</em>
	</xsl:template>

	<xsl:template match="html:bdi">
		<div>
			<xsl:copy-of select="@dir"/>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</div>
	</xsl:template>

	<xsl:template match="html:mark | html:meter | html:rt | html:time | html:wbr">
		<!-- HTML5 elements TODO: handle semantic differences between the elements -->
		<span>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</span>
	</xsl:template>

	<xsl:template match="html:nav | html:section | html:article | html:aside | html:hgroup | html:header | html:footer | html:address | html:figure | html:figcaption | html:details | html:summary | html:figure | html:figcaption | html:progress">
		<!-- HTML5 elements TODO: handle semantic differences between the elements. Add CSS attributes like "html-hgroup", "html-aside" etc? -->
		<div>
			<xsl:call-template name="copy-attributes"/>
			<xsl:attribute name="class" select="string-join((@class,concat('html-',local-name())),' ')"/>
			<xsl:apply-templates/>
		</div>
	</xsl:template>

	<xsl:template match="html:hr"/>

	<xsl:template match="*">
		<xsl:if test="local-name() ne 'html'">
			<xsl:text>
</xsl:text>
			<xsl:comment> **** No template for element: <xsl:value-of select="local-name()"/> **** </xsl:comment>
			<!-- 			<xsl:message terminate="no"> **** No template for element: <xsl:value-of select="local-name()" /> **** </xsl:message> -->
		</xsl:if>

		<xsl:apply-templates/>
	</xsl:template>

	<xsl:function name="pfunc:initialCaps" as="xs:string">
		<!-- Return a given string with the first letter capitalized. If input empty, return empty -->
		<xsl:param name="string" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="boolean($string)">
				<!-- Test if it's an empty string -->
				<xsl:value-of select="string-join(
					(
						upper-case(substring($string,1,1)), 
						substring($string,2)
					),
					''
					)
					"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
</xsl:stylesheet>
