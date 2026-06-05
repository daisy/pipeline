<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	>
	<!--
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>-->
	<xsl:output method="text" indent="yes" encoding="Windows-1252"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="table|dtb:table">
		<xsl:variable name="thead" select="thead|dtb:thead"/>
		<xsl:variable name="tbody" select="tbody|dtb:tbody"/>
		<xsl:apply-templates select="caption|dtb:caption"/>
		<xsl:choose>
			<xsl:when test="tbody|dtb:tbody">
				<xsl:for-each select="tbody">
					<xsl:text>\sb100\sa100 {</xsl:text>
					<xsl:apply-templates select="$thead/tr|$thead/dtb:tr"/>
					<xsl:apply-templates/>
					<xsl:apply-templates select="$tbody/tr|$tbody/dtb:tr"/>
					<xsl:text>}\pard </xsl:text>
					<xsl:if test="following-sibling::tbody|following-sibling::tbody">\par</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>\sb100\sa100 {</xsl:text>
				<xsl:apply-templates select="$thead/tr|$thead/dtb:tr"/>
				<xsl:apply-templates select="tr|dtb:tr"/>
				<xsl:apply-templates select="$tbody/tr|$tbody/dtb:tr"/>
				<xsl:text>}\pard </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="tr|dtb:tr">
		<xsl:text>\trowd\keep\keepn\sb0\sa0 </xsl:text>
		<xsl:for-each select="th|td|dtb:th|dtb:td">
			<xsl:call-template name="CELLX"/>
		</xsl:for-each>
		<xsl:apply-templates/>
		<xsl:text>\row
</xsl:text>
	</xsl:template>

	<xsl:template match="th|dtb:th">
		<xsl:call-template name="STRONG_STYLE"/>
		<xsl:text>\intbl </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\cell </xsl:text>
	</xsl:template>
	
	<xsl:template match="td|dtb:td">
		<xsl:call-template name="NORMAL_STYLE_FONT_ONLY"/>
		<xsl:text>\intbl </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>\cell </xsl:text>
	</xsl:template>

	<xsl:template name="CELLX">
		<xsl:variable name="cellwidth" select="format-number(9637 div count(../*),'0')"/>
		<xsl:variable name="colindex" select="count(preceding-sibling::*)"/>
		<xsl:text>\clbrdrt\brdrs\clbrdrb\brdrs\clbrdrl\brdrs\clbrdrr\brdrs</xsl:text>
		<xsl:value-of select="concat('\cellx', (xs:integer($colindex)+1)*xs:integer($cellwidth))"/>
	</xsl:template>
</xsl:stylesheet>
