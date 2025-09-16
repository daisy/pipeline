<?xml version="1.0" encoding="utf-8"?>
<!--
	Fix idref attribute on noteref and annoref elements
		Version
			2007-12-03

		Description
			idref must be present on noteref and annoref. Add idref if missing or 
			change if empty.

			The value of the idref must include a fragment identifier.
			Add a hash mark in the beginning of all idref attributes that don't
			contain a hash mark.

		Input parameters
			assignEmpty - 	'true'/'false'
							'true' assigns an idref value to noteref/annoref based on its
							relative placement among notes/annotations
			addHashMark -	'true'/'false'
							'true' adds a hash mark in the beginning of all idref attributes 
							that don't contain a hash mark.

		Nodes
			noteref, annoref

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Linus Ericson, TPB
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	
	<xsl:param name="assignEmpty" select="'true'"/>
	<xsl:param name="addHashMark" select="'true'"/>
	
	<xsl:template match="dtb:dtbook">
		<xsl:choose>
			<xsl:when test="$assignEmpty='false' and $addHashMark='false'"><xsl:copy-of select="."/></xsl:when>
			<xsl:otherwise><xsl:call-template name="copy"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="dtb:noteref|dtb:annoref">
		<xsl:copy>
			<xsl:copy-of select="@*[not(local-name()='idref')]"/>
			<xsl:choose>
				<xsl:when test="$assignEmpty='true' and (not(@idref) or @idref='' or @idref='#')">
					<xsl:choose>
						<xsl:when test="self::dtb:noteref">
							<xsl:attribute name="idref">#<xsl:value-of select="(//dtb:note)[
							round( ( ( count(current()/preceding::dtb:noteref) ) 
								* 
							( count(//dtb:note) div count(//dtb:noteref) ) ) + 0.5)]/@id"/></xsl:attribute>
							<xsl:message terminate="no">Added idref to noteref</xsl:message>
						</xsl:when>
						<xsl:when test="self::dtb:annoref">
							<xsl:attribute name="idref">#<xsl:value-of select="(//dtb:annotation)[
							round( ( ( count(current()/preceding::dtb:annoref) )
								*
							( count(//dtb:annotation) div count(//dtb:annoref) ) ) + 0.5)]/@id"/></xsl:attribute>
							<xsl:message terminate="no">Added idref to annoref</xsl:message>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$addHashMark='true' and (not(contains(@idref,'#')))">
					<xsl:attribute name="idref">
						<xsl:text>#</xsl:text>
						<xsl:value-of select="@idref"/>
					</xsl:attribute>
					<xsl:message terminate="no">Added hash mark to idref</xsl:message>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="@idref"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
