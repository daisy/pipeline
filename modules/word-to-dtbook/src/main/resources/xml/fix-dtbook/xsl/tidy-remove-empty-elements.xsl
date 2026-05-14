<?xml version="1.0" encoding="UTF-8"?>
<!--
	Remove empty elements
		Version
			2008-11-05

		Description
			Removes
				* empty/whitespace p except when 
						1. preceded by hx or no preceding element and parent is a level
						and 
						2. followed only by other empty p 
				* empty/whitespace em, strong, sub, sup

		Nodes
			p, em, strong, sub, sup, hx, math

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"
			(x) "http://www.w3.org/1998/Math/MathML"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:m="http://www.w3.org/1998/Math/MathML" exclude-result-prefixes="dtb">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
 
 	<xsl:template match="dtb:p[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:choose>
			<xsl:when test="(preceding-sibling::*[1][starts-with(name(), 'h')] or (not(preceding-sibling::*) and (parent::dtb:level1 or parent::dtb:level2 or parent::dtb:level3 or parent::dtb:level4 or parent::dtb:level5 or parent::dtb:level6 or parent::dtb:level))) and count(following-sibling::*)=count(following-sibling::dtb:p[(text() and count(node())=1 and normalize-space()='') or not(node())])"><xsl:call-template name="copy"/></xsl:when>
			<xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:strong[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:apply-templates/>
	</xsl:template>
 
	<xsl:template match="dtb:em[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="dtb:sub[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="dtb:sup[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="dtb:bdo[(text() and count(node())=1 and normalize-space()='') or not(node())]">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:hd">
		<xsl:choose>
			<xsl:when test="normalize-space()!=''"><xsl:call-template name="copy"/></xsl:when>
			<xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:math[not(m:*)]">
		<xsl:apply-templates/>
	</xsl:template>
	
</xsl:stylesheet>
