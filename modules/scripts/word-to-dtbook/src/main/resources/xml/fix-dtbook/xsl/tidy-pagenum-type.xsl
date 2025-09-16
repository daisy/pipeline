<?xml version="1.0" encoding="utf-8"?>
<!--
	fix pagenum elements where the page attribute and the content don't match
	
		Version
			2007-11-29
			Rev. 2008-05-30

		Description
			Update the @page attribute to make it match the contents of the pagenum
			element.
			
			If @page="front" but the contents of the element is an arabic number,
			the @page attribute is changed to "normal"
			(note:  arabic numbers are theoretically allowed from @page="front", but
			are not considered standard practice by many)
			
			If @page="special" but the element has no content, adds a dummy content
			("page break").

		Nodes
			pagenum

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Linus Ericson, TPB
			James  Pritchett, RFBD
			James Norrish, VUW
			Romain Deltour, DAISY
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy2.xsl"/>
	<xsl:include href="output2.xsl"/>
	<xsl:include href="localization.xsl"/>
	
	<!-- jpritchett@rfbd.org, 2008-05-30:  Removed front-to-special conversion, as this is now in the "repair" stylesheet -->
	<!-- 
		Match:  This is a @page="front", but the content doesn't look like that
		Action: If the contents are numbers only, change to @page="normal",
		        otherwise change to @page="special"
	 -->
	<xsl:template match="dtb:pagenum[@page='front' and matches(.,'^\s*\d+\s*$','s')]">
		<xsl:message>changing page="front" to page="normal"</xsl:message>
		<xsl:copy>
			<xsl:attribute name="page">normal</xsl:attribute>				
			<xsl:copy-of select="@*[local-name()!='page']"/>
			<xsl:apply-templates/>			
		</xsl:copy>				
	</xsl:template>
	
	<!--
		Match: page with @type=special with no content.
		Action: add some dummy content.
	-->
	<xsl:template match="dtb:pagenum[@page='special'][not(normalize-space())]">
		<xsl:message>adding dummy content to empty "special" pagenum</xsl:message>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="." mode="localizedPageBreak"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
