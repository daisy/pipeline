<?xml version="1.0" encoding="UTF-8"?>
<!--
	Complete structure
		Version
			2007-12-03

		Description
			Adds an empty p-tag if hx is the last element

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

	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:hd">
		<xsl:call-template name="copy"/>
		<xsl:if test="not(following-sibling::*)">
			<xsl:message terminate="no">Added an empty paragraph to complete level</xsl:message>
			<xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/"/>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
