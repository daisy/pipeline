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

	<!-- ##### LIST STRUCTURES ##### -->
	
	<!-- #### list ELEMENT #### -->
	<xsl:template match="list|dtb:list">
		<xsl:if test="preceding-sibling::node()[self::text()]">
			<xsl:text>\par&#xa;</xsl:text>
		</xsl:if>
		<xsl:apply-templates/>
		<xsl:text>\pard </xsl:text>
	</xsl:template>

	<!-- #### li ELEMENT ####-->
	<xsl:template match="li|dtb:li">
		<xsl:variable name="curnum" select="count(preceding-sibling::li|preceding-sibling::dtb:li)+1"/>
		<xsl:variable name="indentcount" select ="count(ancestor::li)"/>
		<xsl:choose>
			<xsl:when test="text()|*[not(self::list|self::dtb:list)]">
				<xsl:choose>
					<xsl:when test="parent::dtb:list[@type='pl']">
						<xsl:call-template name="LI_PL"/>
					</xsl:when>
					<xsl:when test="parent::list[@type='ol']|parent::dtb:list[@type='ol']">
						<xsl:call-template name="LI_OL"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="LI_UL"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- #### lic ELEMENT #### -->
	<xsl:template match="lic|dtb:lic">
		<xsl:text>\i </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\i0 </xsl:text>
	</xsl:template>

	<!-- #### UNORDERED LIST ITEMS #### -->
	<xsl:template name="LI_UL">
		<xsl:variable name="indentcount" select ="count(ancestor::li)"/>
		<xsl:text>\pard{\pntext\bullet\tab}{\*\pn\pnlvlblt\pnf2\pnindent283{\pntxtb\bullet}}</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"/>
		<xsl:value-of select="concat('\li',283*($indentcount+1))"/>
		<xsl:text>\fi-283\sb50\sa50 </xsl:text>
		<xsl:apply-templates/>
		<xsl:call-template name="LI_END"/>
	</xsl:template>

	<!-- #### ORDERED LIST ITEMS #### -->
	<xsl:template name="LI_OL">
		<xsl:variable name="curnum" select="count(preceding-sibling::li|preceding-sibling::dtb:li)+1"/>
		<xsl:variable name="indentcount" select ="count(ancestor::li)"/>
		<xsl:text>\pard{\pntext </xsl:text>
		<xsl:value-of select="$curnum"/>
		<xsl:text>.\tab}{\*\pn\pndec\pnf2\pnlvl</xsl:text>
		<xsl:value-of select="(($indentcount+2) mod 9)+1"/>
		<xsl:text>}</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"/>
		<xsl:value-of select="concat('\li',283*($indentcount+1))"/>
		<xsl:text>\fi-283\sb50\sa50 </xsl:text>
		<xsl:apply-templates/>
		<xsl:call-template name="LI_END"/>
	</xsl:template>

	<!-- #### PREFORMATTED LIST ITEMS #### -->
	<xsl:template name="LI_PL">
		<xsl:variable name="indentcount" select ="count(ancestor::li)"/>
		<xsl:text>\pard</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"/>
		<xsl:value-of select="concat('\li',283*($indentcount+1))"/>
		<xsl:text>\sb50\sa50 </xsl:text>
		<xsl:apply-templates/>
		<xsl:call-template name="LI_END"/>
	</xsl:template>

	<!-- #### END OF LIST ITEM #### -->
	<xsl:template name="LI_END">
		<xsl:choose>
			<xsl:when test="node()[last()][self::list|self::dtb:list]"/>
			<xsl:otherwise>
				<xsl:text>\par&#xa;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- #### dl ELEMENT #### -->
	<xsl:template match="dl|dtb:dl">
		<xsl:apply-templates/>
	</xsl:template>
	
	<!-- #### dt ELEMENT #### -->
	<xsl:template match="dt|dtb:dt">
		<xsl:text>\pard</xsl:text>
		<xsl:call-template name="STRONG_STYLE"/>
		<xsl:text>\li0\sb50\sa50 </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\par&#xa;</xsl:text>
	</xsl:template>


	<!-- #### dd ELEMENT #### -->
	<xsl:template match="dd|dtb:dd">
		<xsl:text>\pard</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"/>
		<xsl:text>\li566\sb50\sa50 </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\par&#xa;</xsl:text>
	</xsl:template>

</xsl:stylesheet>
