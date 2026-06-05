<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<!--
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>-->
	<xsl:param name="stampValue"></xsl:param>
	<xsl:output method="text" indent="yes" encoding="Windows-1252"/>
	<xsl:strip-space elements="*"/>

	<xsl:template name="META_INFO">
		<xsl:text>{\info</xsl:text>
		<xsl:text>{\title </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:title</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{\subject </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:subject</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{\author </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:creator</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{\company </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:publisher</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{\doccomm </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:description</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{\*\userprops </xsl:text>
		<xsl:text>{{\propname Identifier}\proptype30\staticval </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:identifier</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:text>{{\propname Copyright}\proptype30\staticval </xsl:text>
		<xsl:call-template name="META_CONTENT">
			<xsl:with-param name="metaname">dc:rights</xsl:with-param>
		</xsl:call-template>
		<xsl:text>}
</xsl:text>
		<xsl:if test="string-length($stampValue)&gt;0">
			<xsl:text>{{\propname Stamp}\proptype30\staticval </xsl:text>
			<xsl:value-of select="$stampValue"/>
			<xsl:text>}
</xsl:text>
		</xsl:if>
		<xsl:text>}}
</xsl:text>
	</xsl:template>

	<xsl:template name="META_CONTENT">
		<xsl:param name="metaname"/>
		<xsl:for-each select="//*[self::meta|self::dtb:meta][@name=$metaname]">
			<xsl:call-template name="rtf-encode">
				<xsl:with-param name="str" select="@content"/>
				<xsl:with-param name="escMethod">2</xsl:with-param>
			</xsl:call-template>
			<xsl:text> </xsl:text>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
