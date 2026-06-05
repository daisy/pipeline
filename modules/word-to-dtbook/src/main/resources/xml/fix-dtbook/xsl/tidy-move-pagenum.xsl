<?xml version="1.0" encoding="UTF-8"?>
<!--
	Move pagenum
		Version
			2007-11-29

		Description
			Moves
				* pagenum inside h[x] before h[x]
				* pagenum inside a word after the word

		Nodes
			pagenum

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
		<!-- Move all pagenums inside hx before hx -->
		<xsl:for-each select="descendant::dtb:pagenum">
			<xsl:copy-of select="."/>
		</xsl:for-each>
		<xsl:call-template name="copy"/>
	</xsl:template>
	
	<!-- Ignore pagenums inside hx, they are processed above -->
	<xsl:template match="dtb:pagenum[ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|
	ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd]"/>

	<!-- Ignore these text nodes, they are processed below -->
	<xsl:template match="text()[preceding-sibling::node()[1][self::dtb:pagenum] and
							preceding-sibling::node()[2][self::text()] and
not(ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd)]"/>
							
	<!-- Process pagenum with text nodes on both sides -->
	<xsl:template match="dtb:pagenum[preceding-sibling::node()[1][self::text()] and 
							 following-sibling::node()[1][self::text()] and
not(ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd)]">
		<xsl:variable name="A1" select="following-sibling::node()[1]"/>
		<xsl:variable name="A2" select="preceding-sibling::node()[1]"/>
		<xsl:choose>
			<!-- 
          ends-with: substring($A, string-length($A) - string-length($B) + 1) = $B
                     Se XSLT programmers reference, second edition, Michael Kay, sidan 541
      -->
			<!-- 
				If the previous text node ends with a space or the next text node starts with a space, the pagenum is not moved.
      -->
			<xsl:when test="matches($A1, '^\s') or matches($A2,'\s$')">
				<xsl:call-template name="copy"/>
				<xsl:value-of select="$A1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="contains($A1,' ')">
						<xsl:value-of select="substring-before($A1,' ')"/>
						<xsl:call-template name="copy"/>
						<xsl:value-of select="concat(' ',substring-after($A1,' '))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$A1"/>
						<xsl:call-template name="copy"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
 
</xsl:stylesheet>
