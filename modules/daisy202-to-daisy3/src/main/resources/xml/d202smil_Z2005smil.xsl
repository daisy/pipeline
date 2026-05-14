<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="http://www.w3.org/2001/SMIL20/"
                exclude-result-prefixes="xs pf">

<!--
	To do:
	- 2.02 ncc normally points to smil <text/>, in zed this is
			allowed but not recommended, should be parent timecontainer (psps: fixed, but input param needed)
	- layout/region?
	- skippability (psps: fixed for pages, sidebars, and prodnotes. footnotes must be handled)
-->

<!-- psps20080312: The @id problem
	Case 1: @id on par, not on par/text
		ncc points to par
		Perfect, just let the @id stay where it is, and perhaps create a suitable @id on the par/text
	case 2: @id on par/text, not on par itself
		ncc points to text
		Zed prefers the other way round, so we'll move the par/text/@id to par/@id, and perhaps create a suitable @id on the par/text
	case 3: @id on both
		This is the tricky one, as we don't know if ncc points to the par or the par/text.
		Proposed solution: Assume that Pipeline can figure out the ncc/SMIL-relation,
		and hands it over to the xsl as a boolean parameter; NCCPointsToPar
		If NCCPointsToPar then
			let the @id's stay where they are
		else
			swap the par/@id and the par/text/@id
		end if
	TODO: Must check out how this works for D202 footnotes with nested par's
-->

<xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
<xsl:import href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

<!-- inparams: -->
	<xsl:param name="uid" as="xs:string"/>                                      <!-- uid of publication -->
	<xsl:param name="title" as="xs:string"/>                                    <!-- title of publication -->
	<xsl:param name="isNcxOnly" as="xs:boolean"/>                               <!-- whether to drop text elements -->
	<xsl:param name="defaultStatePagenumbers" as="xs:string" select="'true'"/>  <!-- value for head/customAttributes/customTest/@defaultState -->
	<xsl:param name="defaultStateSidebars" as="xs:string" select="'true'"/>     <!-- value for head/customAttributes/customTest/@defaultState -->
	<xsl:param name="defaultStateFootnotes" as="xs:string" select="'true'"/>    <!-- value for head/customAttributes/customTest/@defaultState -->
	<xsl:param name="defaultStateProdnotes" as="xs:string" select="'true'"/>    <!-- value for head/customAttributes/customTest/@defaultState -->
	<xsl:param name="NCCPointsToPars" as="xs:boolean" select="false()"/>        <!-- used for proper @id handling for par's -->

<xsl:template match="smil" priority="1">
	<xsl:call-template name="pf:next-match-with-generated-ids">
		<xsl:with-param name="prefix" select="'seq_'"/>
		<xsl:with-param name="for-elements" select=".//seq[not(@id)]"/>
	</xsl:call-template>
</xsl:template>

<xsl:template match="smil">
	<smil>
		<xsl:apply-templates select="head"/>
		<xsl:apply-templates select="body"/>
	</smil>
</xsl:template>

<xsl:template match="head">
	<head>
		<meta name="dtb:uid" content="{$uid}"/>
		<!-- this value will be corrected in px:daisy3-smil-add-elapsed-time -->
		<meta name="dtb:totalElapsedTime" content="00:00:00"/>
		<meta name="dtb:generator" content="DAISY Pipeline 2"/>
		<!-- <meta name="NCCPointsToPar" content="{$NCCPointsToPars}"/> -->
		<!-- <meta name="NCCPtoP" content="{$NCCPointsToPars}"/> -->
		<!-- psps: Added customAttributes -->
		<xsl:if test="//par/@system-required">
			<customAttributes>
				<xsl:for-each select="distinct-values(//par/@system-required)">
					<customTest id="{substring-before(.,'-on')}" defaultState="false" override="visible">
						<xsl:attribute name="defaultState">
							<xsl:choose>
								<xsl:when test=". eq 'pagenumber-on'"><xsl:value-of select="$defaultStatePagenumbers"/></xsl:when>
								<xsl:when test=". eq 'sidebar-on'"><xsl:value-of select="$defaultStateSidebars"/></xsl:when>
								<xsl:when test=". eq 'footnote-on'"><xsl:value-of select="$defaultStateFootnotes"/></xsl:when>
								<xsl:when test=". eq 'prodnote-on'"><xsl:value-of select="$defaultStateProdnotes"/></xsl:when>
								<!-- There should be no other cases, but just in case: -->
								<xsl:otherwise><xsl:value-of select="'true'"/></xsl:otherwise>
							</xsl:choose>
						</xsl:attribute>
					</customTest>
				</xsl:for-each>
			</customAttributes>
		</xsl:if>
	</head>
</xsl:template>

<xsl:template match="body">
	<body>
		<seq dur="{pf:smil-seconds-to-full-clock-value(pf:smil-total-seconds(.))}">
			<xsl:for-each select="(seq)[1]">
				<xsl:choose>
					<xsl:when test="@id">
						<xsl:sequence select="@id"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="pf:generate-id"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:apply-templates select="seq/*"/>
		</seq>
	</body>
</xsl:template>

<xsl:template match="par">
	<xsl:if test="not($isNcxOnly) or descendant-or-self::audio">
			<!--
			ID value:
			- Case 1: keep @id as it is
			- Case 2: use text/@id on the par
			- Case 3: @id on both elements, value of $NCCPointsToPars decides what to do
			-->
			<par
				id="{if (@id and not(text/@id)) then @id
				else if (not(@id) and text/@id) then text/@id
				else if ($NCCPointsToPars) then @id
				else text/@id}">
				<xsl:if test="@system-required">
					<xsl:attribute name="customTest">
						<xsl:value-of select="substring-before(@system-required,'-on')"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:apply-templates select="node()"/>
			</par>
	</xsl:if>
</xsl:template>

<xsl:template match="text">
	<xsl:choose>
		<xsl:when test="$isNcxOnly">
			<!-- no text element rendered -->
		</xsl:when>
		<xsl:otherwise>
			<text src="{@src}">
				<xsl:attribute name="id">
					<xsl:choose>
						<!-- Case 1 -->
						<xsl:when test="../@id and not(@id)">
							<xsl:value-of select="concat('t1-',../@id)"/>
						</xsl:when>
						<!-- Case 2 -->
						<xsl:when test="not(../@id) and @id">
							<xsl:value-of select="concat('t2-',@id)"/>
						</xsl:when>
						<!-- Case 3: @id on both elements, value of $NCCPointsToPars decides what to do -->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$NCCPointsToPars">
									<xsl:value-of select="@id"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="../@id"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</text>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="seq">
	<seq>
		<xsl:choose>
			<xsl:when test="@id"><xsl:attribute name="id" select="@id"/></xsl:when>
			<xsl:otherwise><xsl:call-template name="pf:generate-id"/></xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates />
	</seq>
</xsl:template>

<xsl:template match="audio">
	<audio id="{@id}" src="{@src}" clipBegin="{@clip-begin}" clipEnd="{@clip-end}"/>
</xsl:template>

<xsl:template match="image">
	<image id="{@id}" src="{@src}"/>
</xsl:template>

</xsl:stylesheet>

