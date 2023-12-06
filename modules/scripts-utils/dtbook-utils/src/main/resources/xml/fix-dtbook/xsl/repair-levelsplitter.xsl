<?xml version="1.0" encoding="UTF-8"?>
<!--
	Level splitter
		Version
			2007-09-26

		Description
			Splits a level into several levels on every additional heading on the same level

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

	<xsl:template match="dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6">
		<xsl:variable name="level" select="substring-after(name(), 'level')"/>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:for-each select="node()">
				<xsl:if test="self::dtb:*[name()=concat('h', $level)] and preceding-sibling::dtb:*[name()=concat('h', $level)]">
					<xsl:message terminate="no"><xsl:value-of select="concat('Splitting level ', $level)"/></xsl:message>
					<xsl:value-of select="concat('&lt;/level', $level, '&gt;&lt;level', $level, '&gt;')" disable-output-escaping="yes"/>
				</xsl:if>
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
