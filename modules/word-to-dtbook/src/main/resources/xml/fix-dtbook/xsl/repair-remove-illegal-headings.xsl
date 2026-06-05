<?xml version="1.0" encoding="utf-8"?>
<!--
	Remove illegal headings
		Version
			2007-09-27

		Description
			Changes a hx into a p with @class="hx" if parent isn't levelx

			Note:
				"Remove illegal headings" cannot handle hx in inline context.
				Support for this could be added.

		Nodes
			hx

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
	
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6">
		<xsl:choose>
			<xsl:when test="parent::*[not(self::dtb:level1 or self::dtb:level2 or self::dtb:level3 or self::dtb:level4 or self::dtb:level5 or self::dtb:level6)]">
				<xsl:message terminate="no">Changed a heading into a paragraph</xsl:message>
				<xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:copy-of select="@*"/>
					<xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="copy"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
