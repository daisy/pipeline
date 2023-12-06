<?xml version="1.0" encoding="UTF-8"?>
<!--
	Remove empty elements
		Version
			2007-12-03

		Description
			Similar to tidy-remove-empty-elements, but removes empty/whitespace elements 
			that must have children.

		Nodes
			*annotation
			*div
			*dl
			*imggroup
			*level
			*level1-6
			*list
			*note
			*table

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	
	<xsl:template match="*[
		(	self::dtb:annotation or self::dtb:div or self::dtb:dl or self::dtb:imggroup or
				self::dtb:level or self::dtb:level1 or self::dtb:level2 or self::dtb:level3 or
					self::dtb:level4 or self::dtb:level5 or self::dtb:level6 or self::dtb:list or
						self::dtb:note or self::dtb:table
		) and (
			count(
				node()[
					(self::text() and normalize-space()='') or self::comment() or
						self::processing-instruction()
				]
			)=count(node())
		)
	]">
		<xsl:message terminate="no">Removed an empty element</xsl:message>
		<xsl:apply-templates/>
	</xsl:template>
  
</xsl:stylesheet>
