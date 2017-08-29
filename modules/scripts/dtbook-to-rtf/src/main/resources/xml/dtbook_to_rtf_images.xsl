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

	<!-- ##### IMAGE STRUCTURES ##### -->
	
	<!-- #### img ELEMENT #### (when outside imggroup)-->
	<xsl:template match="img|dtb:img">
		<xsl:call-template name="IMG"/>
		<xsl:text>{\fs16 </xsl:text>
		<xsl:call-template name="IMG_ALT_AS_FOOTNOTE"/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template name="IMG">
		<xsl:text>{\field{\*\fldinst INCLUDEPICTURE "</xsl:text>
		<xsl:value-of select="@src"/>
		<xsl:text>" \\d \\x \\y}}</xsl:text>
	</xsl:template>

	<xsl:template name="IMG_ALT_AS_FOOTNOTE">
		<xsl:if test="@alt">
			<xsl:choose>
				<xsl:when test="ancestor::note|ancestor::dtb:note|ancestor::annotation|ancestor::dtb:annotation">
					<!-- Nested notes are displayed in their full in superscript -->
					<xsl:text>{\super </xsl:text>
					<xsl:call-template name="rtf-encode">
						<xsl:with-param name="str" select="@alt"/>
					</xsl:call-template>
					<xsl:text>}</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>[\chftn{\footnote</xsl:text>
					<xsl:call-template name="NORMAL_STYLE_FONT_ONLY"/>
					<xsl:text>[\chftn]\tab </xsl:text>
					<xsl:call-template name="rtf-encode">
						<xsl:with-param name="str" select="@alt"/>
					</xsl:call-template>
					<xsl:text>}]</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<!-- #### imggroup ELEMENT #### -->
	<xsl:template match="imggroup|dtb:imggroup">
		<xsl:text>{\par\pard\li283\ri283\sb50\sa50 </xsl:text>
		<xsl:apply-templates mode="IMGGROUP"/>
		<xsl:text>}\pard </xsl:text>
	</xsl:template>

	<xsl:template match="img|dtb:img" mode="IMGGROUP">
		<xsl:call-template name="IMG"/>
		<xsl:text>\line </xsl:text>
		<xsl:call-template name="IMG_ALT_AS_FOOTNOTE"/>
		<xsl:text>\par&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="caption|dtb:caption" mode="IMGGROUP">
		<xsl:text>{\b </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}\par
</xsl:text>
	</xsl:template>

	<xsl:template match="prodnote|dtb:prodnote" mode="IMGGROUP">
		<xsl:apply-templates select="."/>
	</xsl:template>
	
</xsl:stylesheet>
