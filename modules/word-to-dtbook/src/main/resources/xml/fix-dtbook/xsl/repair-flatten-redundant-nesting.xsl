<?xml version="1.0" encoding="UTF-8"?>
<!--
	Flatten redundant nesting
		Version
			2007-12-03

		Description
			Removes nested p

		Nodes
			p

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

	<xsl:template match="dtb:p[dtb:p and count(node()[
		(self::text() and normalize-space()='') or
			self::dtb:p or self::comment() or
				self::processing-instruction()
		])=count(node())]">
		<xsl:message terminate="no">Removed a nested paragraph</xsl:message>
		<xsl:apply-templates/>
	</xsl:template>
  
</xsl:stylesheet>
