<?xml version="1.0" encoding="UTF-8"?>
<!--
    
    Version
    2008-05-07
    
	Description
	Tools used to insert dummy headings in levels.
		* "addHeading" is used to add a hx to the context levelx
			- can be applied to level1 to level6 and level
			- the heading is localized
			- leading pagenums and comments are copied first
			- an empty p is added after the heading if required 
		*"addLevelWithHeading" is used to a levelx with a  hx.
			- can be applied for level1 to level6 but not level
			- the heading is localized
			- An empty p is added after the heading
    
    Nodes
    dtbook/level1|level2|level3|level4|level5|level6|level
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Romain Deltour, DAISY
	
-->
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2.0" exclude-result-prefixes="dtb">
	
	<xsl:include href="localization.xsl"/>
	
	<xsl:template match="*" mode="copyLeadingPagenums">
		<xsl:apply-templates select="(dtb:pagenum|text()|processing-instruction()|comment())[not(preceding-sibling::dtb:*[not(self::dtb:pagenum)])]" />
	</xsl:template>

	<xsl:template match="*" mode="copyAllButLeadingPagenums">
		<xsl:variable name="nodes" select="*[not(self::dtb:pagenum)]|(dtb:pagenum|text()|processing-instruction()|comment())[preceding-sibling::dtb:*[not(self::dtb:pagenum)]]"/>
		<xsl:choose>
			<xsl:when test="$nodes">
				<xsl:apply-templates select="$nodes" />
			</xsl:when>
			<xsl:otherwise>
				<p/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*" mode="addHeading">
		<xsl:param name="level"/>
		<xsl:message><xsl:value-of select="concat('Added a dummy h', $level)"/></xsl:message>
		<xsl:apply-templates select="." mode="copyLeadingPagenums"/>
		<xsl:element name="h{$level}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:apply-templates select="." mode="localizedHeading"/>
		</xsl:element>
		<xsl:apply-templates select="." mode="copyAllButLeadingPagenums"/>
	</xsl:template>
	
	<xsl:template match="*" mode="addLevelWithHeading">
		<xsl:param name="level"/>
		<xsl:element name="level{$level}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:element name="h{$level}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
				<xsl:apply-templates select="." mode="localizedHeading"/>
			</xsl:element>
			<p/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
