<?xml version="1.0" encoding="UTF-8"?>
<!--
	Change inline pagenum to block
		Version
			2007-12-03

		Description
			Removes otherwise empty p or li around pagenum (except p in td)

		Nodes
			p, li

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

	<xsl:template match="*[(self::dtb:li or self::dtb:p) and dtb:pagenum and 
		not(parent::dtb:td) and 
			count(
				node()[
					(self::text() and normalize-space()='') or
						self::dtb:pagenum or self::comment() or
							self::processing-instruction()
				]
			)=count(node())
	]">
		<xsl:apply-templates/>
	</xsl:template>

</xsl:stylesheet>
