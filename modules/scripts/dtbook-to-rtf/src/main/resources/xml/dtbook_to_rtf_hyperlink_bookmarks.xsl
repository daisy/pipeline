<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<!--
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>-->

	<xsl:template name="BOOKMARK_START">
		<xsl:variable name="id" select="@id"/>
		<xsl:choose>
			<xsl:when test="self::levelhd|self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::note|self::noteref|self::annotation|self::dtb:hd|self::dtb:h1|self::dtb:h2|self::dtb:h3|self::dtb:h4|self::dtb:h5|self::dtb:h6|self::dtb:note|self::dtb:noteref|self::dtb:annotation">
				<xsl:text>{\*\bkmkstart </xsl:text>
				<xsl:value-of select="$id"/>
				<xsl:text>}</xsl:text>
			</xsl:when>
			<xsl:when test="(//a[@href=concat('#', $id)]) or (//dtb:a[@href=concat('#', $id)])">
				<xsl:text>{\*\bkmkstart </xsl:text>
				<xsl:value-of select="$id"/>
				<xsl:text>}</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="BOOKMARK_END">
		<xsl:variable name="id" select="@id"/>
		<xsl:choose>
			<xsl:when test="self::levelhd|self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::note|self::noteref|self::annotation|self::dtb:hd|self::dtb:h1|self::dtb:h2|self::dtb:h3|self::dtb:h4|self::dtb:h5|self::dtb:h6|self::dtb:note|self::dtb:noteref|self::dtb:annotation">
				<xsl:text>{\*\bkmkend </xsl:text>
				<xsl:value-of select="$id"/>
				<xsl:text>}</xsl:text>
			</xsl:when>
			<xsl:when test="(//a[@href=concat('#', $id)]) or (//dtb:a[@href=concat('#', $id)])">
				<xsl:text>{\*\bkmkned </xsl:text>
				<xsl:value-of select="$id"/>
				<xsl:text>}</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- ##### HYPERLINK STRUCTURE ##### -->

	<!-- #### a ELEMENT #### -->
	<xsl:template match ="a|dtb:a">
		<xsl:choose>
			<xsl:when test="starts-with(@href, '#') or @external='false'">
				<xsl:text>{\field</xsl:text>
				<xsl:text>{\*\fldinst HYPERLINK \\l </xsl:text>
				<xsl:value-of select="substring-after(@href, '#')"/>
				<xsl:text>}</xsl:text>
				<xsl:text>{\fldrslt </xsl:text>
				<xsl:text>\cf3\ul </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
				<xsl:text>}</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>{\field</xsl:text>
				<xsl:text>{\*\fldinst HYPERLINK </xsl:text>
				<xsl:value-of select="@href"/>
				<xsl:text>}</xsl:text>
				<xsl:text>{\fldrslt </xsl:text>
				<xsl:text>\cf3\ul </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
				<xsl:text>}</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	

</xsl:stylesheet>