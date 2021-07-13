<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:c="http://daisymfc.sf.net/xslt/config"
                xmlns:pfunc="http://daisymfc.sf.net/xslt/function"
                xmlns="http://www.daisy.org/z3986/2005/ncx/"
                exclude-result-prefixes="html c xs pfunc">

<!--
	TODO: Handle localization for the content of navInfo/text elements
-->

<c:config>
	<c:generator>DMFC Daisy 2.02 to z3986-2005</c:generator>
	<c:name>d202ncc_Z2005ncx</c:name>
	<c:version>0.1</c:version>
	<c:author>Brandon Nelson</c:author>
	<c:description>Creates the Z2005 ncx file.</c:description>
</c:config>

<!-- inparams: -->
<xsl:param name="uid" as="xs:string"/>                                     <!-- uid of publication -->
<xsl:param name="smils" as="document-node(element(smil))*"/>               <!-- DAISY 2.02 SMIL files -->
<xsl:param name="smilCustomTests" as="xs:string*" select="()"/>            <!-- space separated list of .... found in DAISY 3 SMIL files -->
<xsl:param name="defaultStatePagenumbers" as="xs:string" select="'true'"/> <!-- value for head/smilCustomTest/@defaultState -->
<xsl:param name="defaultStateSidebars" as="xs:string" select="'true'"/>    <!-- value for head/smilCustomTest/@defaultState -->
<xsl:param name="defaultStateFootnotes" as="xs:string" select="'true'"/>   <!-- value for head/smilCustomTest/@defaultState -->
<xsl:param name="defaultStateProdnotes" as="xs:string" select="'true'"/>   <!-- value for head/smilCustomTest/@defaultState -->
<xsl:param name="addNavLabelAudio" as="xs:string" select="'false'"/>       <!-- add audio element to NavLabel elements -->
<xsl:param name="minNavLabelAudioLength" as="xs:integer" select="1000"/>   <!-- minimum length for audio clips in NavLabel elements -->

<xsl:template match="/html:html">
	<ncx version="2005-1">
		<xsl:attribute name="xml:lang">
			<xsl:choose>
				<xsl:when test="@xml:lang">
					<xsl:value-of select="@xml:lang"/>
				</xsl:when>
				<xsl:when test="//html:meta[@name eq 'dc:language']">
					<xsl:value-of select="//html:meta[@name eq 'dc:language'][1]/@content"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'en'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
		<xsl:apply-templates select="html:head"/>
		<xsl:apply-templates select="html:body"/>
	</ncx>
</xsl:template>

<xsl:template match="html:head">
	<head>
		<meta name="dtb:uid" content="{$uid}"/>
		<meta name="dtb:depth" content="{html:meta[@name='ncc:depth']/@content}"/>
		<meta name="dtb:generator" content="Pipeline Daisy 2.02 to z39.86-2005"/>
		<meta name="dtb:totalPageCount" content="{html:meta[@name='ncc:pageFront']/@content + html:meta[@name='ncc:pageNormal']/@content + html:meta[@name='ncc:pageSpecial']/@content}"/>
		<xsl:choose>
			<xsl:when test="//html:span[@class='page-normal']">
				<meta name="dtb:maxPageNumber" content="{max(//html:span[@class='page-normal'])}"/>
			</xsl:when>
			<xsl:otherwise>
				<meta name="dtb:maxPageNumber" content="0"/>
			</xsl:otherwise>
		</xsl:choose>
		<!-- Handle smilCustomTest elements (given as parameters) -->
		<xsl:for-each select="$smilCustomTests">
			<smilCustomTest id="{.}" override="visible">
				<xsl:attribute name="defaultState">
					<xsl:choose>
						<xsl:when test=". eq 'pagenumber'"><xsl:value-of select="$defaultStatePagenumbers"/></xsl:when>
						<xsl:when test=". eq 'sidebar'"><xsl:value-of select="$defaultStateSidebars"/></xsl:when>
						<xsl:when test=". eq 'footnote'"><xsl:value-of select="$defaultStateFootnotes"/></xsl:when>
						<xsl:when test=". eq 'prodnote'"><xsl:value-of select="$defaultStateProdnotes"/></xsl:when>
						<!-- There should be no other cases, but just in case: -->
					<xsl:otherwise><xsl:value-of select="'true'"/></xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<xsl:attribute name="bookStruct">
					<xsl:choose>
						<xsl:when test=". eq 'pagenumber'">PAGE_NUMBER</xsl:when>
						<xsl:when test=". eq 'sidebar'">OPTIONAL_SIDEBAR</xsl:when>
						<xsl:when test=". eq 'footnote'">NOTE</xsl:when>
						<xsl:when test=". eq 'prodnote'">OPTIONAL_PRODUCER_NOTE</xsl:when>
						<xsl:otherwise>UNKNOWN_BOOKSTRUCT</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</smilCustomTest>
		</xsl:for-each>
		<!-- Transfer all meta elements from the ncc, as long as they don't start with dtb -->
		<xsl:for-each select="html:meta[not(@http-equiv or starts-with(@name,'dtb:'))]">
			<meta>
				<xsl:copy-of select="@*"/>
			</meta>
		</xsl:for-each>
	</head>
	<docTitle>
		<text><xsl:value-of select="html:title"/></text>
	</docTitle>
	<xsl:apply-templates select="html:meta[@name='dc:creator']"/>
</xsl:template>

<xsl:template match="html:meta[@name='dc:creator']" >
	<docAuthor>
		<text><xsl:value-of select="@content"/></text>
	</docAuthor>
</xsl:template>

<xsl:template match="html:body">
	<xsl:call-template name="navMap"/>
	<xsl:if test="html:span[@class='page-normal' or @class='page-front' or @class='page-special']">
		<xsl:call-template name="pageList"/>
	</xsl:if>
	<xsl:if test="html:div[@class eq 'group']">
		<xsl:call-template name="navListDiv"/>
	</xsl:if>
	<xsl:if test="html:span[matches(@class,'^sidebar$|^optional-prodnote$|^noteref$')]">
		<xsl:call-template name="navListSpan"/> <!-- Sidebars, prodnotes and/or noterefs -->
	</xsl:if>
</xsl:template>

<xsl:template name="navMap">
	<navMap>
		<xsl:apply-templates select="html:h1"/>
	</navMap>
</xsl:template>

<xsl:template match="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
	<navPoint id="{@id}" class="{local-name()}"><xsl:attribute name="playOrder"><xsl:call-template name="positionInNCC"/></xsl:attribute>
		<navLabel>
			<text><xsl:value-of select="normalize-space(.)"/></text>
			<xsl:if test="matches($addNavLabelAudio,'true','i')">
				<xsl:call-template name="navLabelAudio"/>
			</xsl:if>
		</navLabel>
		<content src="{html:a/@href}"/>
		<xsl:apply-templates select="key('nextHeadings', generate-id(.))"/>
	</navPoint>
</xsl:template>

<xsl:template name="pageList">
	<pageList>
		<xsl:apply-templates select="html:span[@class='page-normal' or @class='page-front' or @class='page-special']"/>
	</pageList>
</xsl:template>

<xsl:template match="html:span[@class='page-normal' or @class='page-front' or @class='page-special']">
	<pageTarget class="pagenum" type="{substring-after(@class, '-')}" value="{normalize-space(.)}" id="{@id}">
		<xsl:attribute name="playOrder"><xsl:call-template name="positionInNCC"/></xsl:attribute>
		<navLabel>
			<text><xsl:value-of select="normalize-space(.)"/></text>
			<xsl:if test="matches($addNavLabelAudio,'true','i')">
				<xsl:call-template name="navLabelAudio"/>
			</xsl:if>
		</navLabel>
		<content src="{html:a/@href}"/>
	</pageTarget>
</xsl:template>

<xsl:template name="navListDiv">
	<navList id="navlist-group" class="group">
		<navInfo>
			<text>This list contains the <xsl:value-of select="count(html:div[@class eq 'group'])"/> groups found in this book.</text>
		</navInfo>
		<navLabel>
			<text>Groups</text>
		</navLabel>
		<xsl:apply-templates select="html:div[@class eq 'group']"/>
	</navList>
</xsl:template>

<xsl:template name="navListSpan">
	<xsl:variable name="dtbBody" as="element()" select="//html:body"/>
	<!-- Create a list of all the distinct class attribute values for all the span.??? -->
	<xsl:variable name="spanType" as="xs:string*"
		select="distinct-values(for $e in html:span[matches(@class,'^sidebar$|^optional-prodnote$|^noteref$')] return $e/@class)"/>
	<xsl:for-each select="$spanType">
		<xsl:variable name="type" as="xs:string" select="."/>
		<navList id="navlist-{$type}" class="{$type}">
			<navInfo>
				<text>The list contains the <xsl:value-of select="count($dtbBody/html:span[@class eq $type])"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="$type eq 'sidebar'">sidebars</xsl:when>
						<xsl:when test="$type eq 'optional-prodnote'">optional producer notes</xsl:when>
						<xsl:otherwise>notes</xsl:otherwise>
					</xsl:choose>
					found in this book.</text>
			</navInfo>
			<navLabel>
				<text>
					<xsl:choose>
						<xsl:when test="$type eq 'sidebar'">Sidebars</xsl:when>
						<xsl:when test="$type eq 'optional-prodnote'">Optional producer notes</xsl:when>
						<xsl:otherwise>Notes</xsl:otherwise>
					</xsl:choose>
				</text>
			</navLabel>
			<xsl:apply-templates select="$dtbBody/html:span[@class eq $type]"/>
		</navList>
	</xsl:for-each>
</xsl:template>

<xsl:template match="html:span[matches(@class,'^sidebar$|^optional-prodnote$|^noteref$')] | html:div[@class eq 'group']">
	<navTarget class="{@class}" id="{concat(@class,'-',position())}">
		<xsl:attribute name="playOrder"><xsl:call-template name="positionInNCC"/></xsl:attribute>
		<navLabel>
			<text><xsl:value-of select="normalize-space(.)"/></text>
			<xsl:if test="matches($addNavLabelAudio,'true','i')">
				<xsl:call-template name="navLabelAudio"/>
			</xsl:if>
		</navLabel>
		<content src="{html:a/@href}"/>
	</navTarget>
</xsl:template>

<xsl:template name="navLabelAudio">
	<xsl:variable name="SMIL.filename" as="xs:anyURI" select="html:a/resolve-uri(substring-before(@href,'#'),base-uri(.))"/>
	<xsl:variable name="SMIL.id" as="xs:string" select="substring-after(html:a/@href,'#')"/>
	<xsl:variable name="SMIL" select="$smils[base-uri(/*)=$SMIL.filename]/id($SMIL.id)"/>
	<xsl:variable name="audioElements" as="element()*">
		<xsl:choose>
			<xsl:when test="local-name($SMIL) eq 'text'">
				<xsl:for-each select="$SMIL">
					<xsl:for-each select="following-sibling::seq/audio">
						<xsl:sequence select="."/>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:when>
			<xsl:when test="local-name($SMIL) eq 'par'">
				<xsl:for-each select="$SMIL">
					<xsl:for-each select="seq/audio">
						<xsl:sequence select="."/>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise />
		</xsl:choose>
	</xsl:variable>
	<xsl:if test="$audioElements">
		<xsl:variable name="numberOfAudioElementsToUse" as="xs:integer"
			select="
				if (
				    pfunc:audioEventDuration($audioElements[1]) lt $minNavLabelAudioLength (: if first audioevent is too short :)
				    and
				    count($audioElements) gt 1                                             (: and there is another one :)
				    )
				then 2                                                                     (: use both of them :)
				else 1                                                                     (: if not; use the first one only :)
				"/>
		<xsl:choose>
			<xsl:when test="($numberOfAudioElementsToUse gt 1)
				and (count(distinct-values($audioElements[position() le $numberOfAudioElementsToUse]/@src)) eq 1)">
				<!-- If there are several audio elements and they point to the same audio file,
					concatenate them in one unique audio element -->
				<audio src="{$audioElements[1]/@src}" clipBegin="{pfunc:createZedClipValue($audioElements[1]/@clip-begin)}" clipEnd="{pfunc:createZedClipValue($audioElements[$numberOfAudioElementsToUse]/@clip-end)}"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- Otherwise, just pick the first audio element -->
				<audio src="{$audioElements[1]/@src}" clipBegin="{pfunc:createZedClipValue($audioElements[1]/@clip-begin)}" clipEnd="{pfunc:createZedClipValue($audioElements[1]/@clip-end)}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:if>
</xsl:template>

<xsl:function name="pfunc:audioEventDuration" as="xs:integer">
	<xsl:param name="audioElement" as="element()"/>
	<!--	This function calculates the duration of a DAISY 2.02 audio event
		Input: The relevant <audio ... clip-begin="npt=...." clip-end="npt=...." ... /> element as given in SMIL files
		Output: In integer representing the duration of the audio event i milliseconds.
					If clip-begin evaluates to a larger value then clip-end, the value 0 is returned
		Dependencies:  the function pfunc:d202ClipValue2Ms
	 -->
	<xsl:variable name="clipBegin" as="xs:integer" select="pfunc:d202ClipValue2Ms($audioElement/@clip-begin)"/>
	<xsl:variable name="clipEnd" as="xs:integer" select="pfunc:d202ClipValue2Ms($audioElement/@clip-end)"/>
	<xsl:value-of select="
		if ($clipEnd gt $clipBegin)
		then $clipEnd - $clipBegin
		else 0
	"/>
</xsl:function>

<xsl:function name="pfunc:d202ClipValue2Ms" as="xs:integer">
	<xsl:param name="clipValue" as="xs:string"/>
	<!--	This function converts a SMIL Clock Value to an equivalent number fo millieseconds
		Input: A string on one of the following forms:
				"Xh", "Xmin", "Xs", "Xms" or "X" (the last one treated as "Xs"),
				where X represents a numerical value, with or without a decimal part, and with "." representing the decimal separator
		Output: An integer representing the number of milliseconds.
					If input does not match the expected, the value 0 is returned.
	 -->
	<xsl:analyze-string select="$clipValue" regex="^npt=([0-9]*\.?[0-9]*)((h|min|s|ms)?)$">
		<xsl:matching-substring>
			<xsl:choose>
				<xsl:when test="regex-group(2) eq 'h'">
					<xsl:value-of select="xs:integer(round(number(regex-group(1)) * 3600 * 1000))"/>
				</xsl:when>
				<xsl:when test="regex-group(2) eq 'min'">
					<xsl:value-of select="xs:integer(round(number(regex-group(1)) * 60 * 1000))"/>
				</xsl:when>
				<xsl:when test="regex-group(2) eq 'ms'">
					<xsl:value-of select="xs:integer(round(number(regex-group(1))))"/>
				</xsl:when>
				<xsl:otherwise> <!-- seconds (the default) -->
					<xsl:value-of select="xs:integer(round(number(regex-group(1)) * 1000))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:matching-substring>
		<xsl:non-matching-substring>
			<xsl:value-of select="0"/>
		</xsl:non-matching-substring>
	</xsl:analyze-string>
</xsl:function>

<xsl:function name="pfunc:createZedClipValue" as="xs:string">
	<xsl:param name="daisy202ClipValue" as="xs:string"/>
	<xsl:value-of select="$daisy202ClipValue"/>  <!-- For now, simply return the same value as in the D202 SMIL -->
</xsl:function>

<xsl:template name="positionInNCC">
	<xsl:value-of select="count(preceding::*[
		self::html:span[matches(@class,'^sidebar$|^optional-prodnote$|^noteref$')]
		or self::html:div[@class eq 'group']
		or self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6
		or self::html:span[substring-before(@class, '-')='page']]) + 1"/>
</xsl:template>

<xsl:key name="nextHeadings"
			match="html:h6"
			use="generate-id(preceding-sibling::*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5][1])"/>

<xsl:key name="nextHeadings"
			match="html:h5"
			use="generate-id(preceding-sibling::*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4][1])"/>

<xsl:key name="nextHeadings"
			match="html:h4"
			use="generate-id(preceding-sibling::*[self::html:h1 or self::html:h2 or self::html:h3][1])"/>

<xsl:key name="nextHeadings"
			match="html:h3"
			use="generate-id(preceding-sibling::*[self::html:h1 or self::html:h2][1])"/>

<xsl:key name="nextHeadings"
			match="html:h2"
			use="generate-id(preceding-sibling::html:h1[1])"/>

</xsl:stylesheet>
