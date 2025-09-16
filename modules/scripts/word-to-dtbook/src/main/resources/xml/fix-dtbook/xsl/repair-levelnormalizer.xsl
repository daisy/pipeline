<?xml version="1.0" encoding="UTF-8"?>
<!--
	Level normalizer
		Version
			2007-09-26

		Description
			Removes levelx if it has descendant headings of x-1 (this simplifies later steps).

			Note: Level normalizer cannot fix level1/level2/level1

		Nodes
			levelx

		Namespaces
			(x) ""
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	

	<xsl:template match="dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6">
		<xsl:variable name="level" select="substring-after(name(), 'level')"/>
		<xsl:choose>
			<xsl:when test="descendant::dtb:*[(self::dtb:h1 or self::dtb:h2 or self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6) and substring-after(name(), 'h')&lt;$level]">
				<xsl:message terminate="no">Removed problematic level</xsl:message>
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="copy"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
