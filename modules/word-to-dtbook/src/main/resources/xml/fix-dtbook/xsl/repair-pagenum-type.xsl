<?xml version="1.0" encoding="utf-8"?>
<!--
	fix pagenum elements where the page attribute and the content don't match
	
		Version
			2007-11-29
			rev 2008-05-30

		Description
			Update the @page attribute to make it match the contents of the pagenum
			element.
			
			If @page="normal" but the contents of the element doesn't match "normal"
			content, the @page attribute is changed to:
			  - @page="front" if the contents is roman numerals and the pagenum element
			    is located in the frontmatter of the book
			  - @page="special" otherwise	
	
			If @page="front" but the contents of the element doesn't match "front"
			content (neither roman nor arabic numerals), the @page attribute is changed to "special"		

		Nodes
			pagenum

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Linus Ericson, TPB
			James Pritchett, RFBD
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy2.xsl"/>
	<xsl:include href="output2.xsl"/>
	
	<!-- 
		Match:  This is a @page="normal", but the content doesn't look like that
		Action: If in frontmatter and the contents are roman numerals, change to @page="front",
		        otherwise change to @page="special"
	 -->
	<xsl:template match="dtb:pagenum[@page='normal' and not(matches(.,'^\s*\d+\s*$','s'))]">
		<xsl:choose>			
			<xsl:when test="ancestor::dtb:frontmatter and matches(.,'^\s*[Mm]*([Dd]?[Cc]{0,3}|[Cc][DdMm])([Ll]?[Xx]{0,3}|[Xx][LlCc])([Vv]?[Ii]{0,3}|[Ii][VvXx])\s*$','s')">
				<xsl:message>changing page="normal" to page="front"</xsl:message>
				<xsl:copy>
					<xsl:attribute name="page">front</xsl:attribute>				
					<xsl:copy-of select="@*[local-name()!='page']"/>
					<xsl:apply-templates/>			
				</xsl:copy>
			</xsl:when>			
			<xsl:otherwise>
				<xsl:message>changing page="normal" to page="special"</xsl:message>				
				<xsl:copy>
					<xsl:attribute name="page">special</xsl:attribute>				
					<xsl:copy-of select="@*[local-name()!='page']"/>
					<xsl:apply-templates/>			
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- jpritchett@rfbd.org, 2008-05-30:  Added correction of fronts-that-should-be-specials from the "tidy" sheet -->
	<!-- 
		Match:  This is a @page="front", but the content doesn't look like that (neither roman nor arabic numerals)
		Action: If the contents are numbers only, change to @page="normal",
		otherwise change to @page="special"
	-->
	<xsl:template match="dtb:pagenum[@page='front' and not(matches(.,'(^\s*[Mm]*([Dd]?[Cc]{0,3}|[Cc][DdMm])([Ll]?[Xx]{0,3}|[Xx][LlCc])([Vv]?[Ii]{0,3}|[Ii][VvXx])\s*$)|(^\s*\d+\s*$)','s'))]">
		<xsl:message>changing page="front" to page="special"</xsl:message>
		<xsl:copy>
			<xsl:attribute name="page">special</xsl:attribute>				
			<xsl:copy-of select="@*[local-name()!='page']"/>
			<xsl:apply-templates/>			
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
