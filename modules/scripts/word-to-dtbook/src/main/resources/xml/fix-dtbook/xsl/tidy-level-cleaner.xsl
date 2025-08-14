<?xml version="1.0" encoding="UTF-8"?>
<!--
	Level cleaner
		Version
			2008-02-11

		Description
			Redundant level structure is sometimes used to mimic the original layout, 
			but can pose a problem in some circumstances. "Level cleaner" simplifies 
			the level structure by removing redundant levels (subordinate levels will 
			be moved upwards). Note that the headings of the affected levels will 
			also change, which will alter the appearance of the layout.

		Nodes
			levelx
			hx

		Namespaces
			(x) ""
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Authors
			Joel Håkansson, TPB
			James Pritchett, RFB&D
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>

	<!-- Template for all levels (1-6) -->
	<xsl:template match="dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6">
		<xsl:param name="currentLevel" select="0"/>		<!-- $currentLevel defaults to 1 -->
		
		<xsl:choose>
		<!-- If all children are level nodes, then this is a redundant level; don't output anything -->
			<xsl:when test="count(./dtb:level2|./dtb:level3|./dtb:level4|./dtb:level5|./dtb:level6)=count(child::*)">
				<xsl:apply-templates>
					<xsl:with-param name="currentLevel" select="$currentLevel"/>
				</xsl:apply-templates>
			</xsl:when>
			
		<!-- Otherwise, output the next level element and recurse with $currentLevel bumped up one -->
			<xsl:otherwise>
				<xsl:element name="level{$currentLevel + 1}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:copy-of select="@*"/>
					<xsl:apply-templates>
						<xsl:with-param name="currentLevel" select="$currentLevel+1"/>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
			</xsl:choose>
	</xsl:template>
		
	<!-- Template for all headings (h1-h6) -->
	<!-- This just rewrites the heading at the current level (whatever that might be) -->
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6">
		<xsl:param name="currentLevel" select="0"/>
		<xsl:element name="h{$currentLevel}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
