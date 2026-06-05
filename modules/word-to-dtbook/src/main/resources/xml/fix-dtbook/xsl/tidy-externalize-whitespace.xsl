<?xml version="1.0" encoding="UTF-8"?>
<!--
	Externalize whitespace
		Version
			2008-04-03

		Description
			Externalizes leading and trailing whitespace from em, strong, sub, sup, pagenum, noteref.
			Handles any level of nesting, e.g.:
				<em> <strong> this </strong> <strong> is <pagenum id="p-1"> 1 </pagenum> </strong> an example </em>

		Nodes
			text()

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:fn="http://www.w3.org/2005/xpath-functions"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

<xsl:include href="output2.xsl"/>
<xsl:include href="recursive-copy2.xsl"/>
<xsl:include href="iterative-processor.xsl"/>

<xsl:template match="dtb:book">
	<xsl:apply-templates select="." mode="startProcessing"/>
</xsl:template>

<xsl:template match="dtb:em[not(@xml:space='preserve')]|dtb:strong[not(@xml:space='preserve')]|dtb:sub[not(@xml:space='preserve')]|dtb:sup[not(@xml:space='preserve')]|dtb:pagenum[not(@xml:space='preserve')]|dtb:noteref[not(@xml:space='preserve')]">
	<xsl:choose>
		<xsl:when test="count(node())=1 and text()">
			<xsl:choose>
				<xsl:when test="normalize-space(text())">
					<xsl:variable name="stringBegin">
						<xsl:call-template name="stringBegin">
							<xsl:with-param name="text" select="text()"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="stringEnd">
						<xsl:call-template name="stringEnd">
							<xsl:with-param name="text" select="text()"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="wsBefore" select="substring(text(), 1, $stringBegin - 1)"/>
					<xsl:variable name="text" select="substring(text(), $stringBegin, $stringEnd - $stringBegin)"/>
					<xsl:variable name="wsAfter" select="substring(text(), $stringEnd)"/>
					<xsl:value-of select="$wsBefore"/>
					<xsl:copy>
						<xsl:copy-of select="@*"/>
						<xsl:value-of select="$text"/>
					</xsl:copy>
					<xsl:value-of select="$wsAfter"/>
				</xsl:when>
				<xsl:otherwise><xsl:call-template name="copy"/></xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="firstNode" select="node()[1][self::text()]"/>
			<xsl:variable name="lastNode" select="node()[last()][self::text()]"/>
			<xsl:variable name="stringBegin">
				<xsl:call-template name="stringBegin">
					<xsl:with-param name="text" select="$firstNode"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="stringEnd">
				<xsl:call-template name="stringEnd">
					<xsl:with-param name="text" select="$lastNode"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="wsBefore" select="substring($firstNode, 1, $stringBegin - 1)"/>
			<xsl:variable name="textFirstNode" select="substring($firstNode, $stringBegin)"/>
			<xsl:variable name="wsAfter" select="substring($lastNode, $stringEnd)"/>
			<xsl:variable name="textLastNode" select="substring($lastNode, 1, $stringEnd - 1)"/>
			<xsl:value-of select="$wsBefore"/>
			<xsl:copy>
				<xsl:copy-of select="@*"/>
				<xsl:value-of select="$textFirstNode"/>
				<xsl:apply-templates select="node()[not(position()=1 and self::text() or position()=last() and self::text())]"/>
				<xsl:value-of select="$textLastNode"/>
			</xsl:copy>
			<xsl:value-of select="$wsAfter"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="stringBegin">
	<xsl:param name="text"/>
	<xsl:param name="index" select="1"/>
	<xsl:choose>
		<xsl:when test="not(matches(substring($text, $index, 1), '\s+')) or $index>string-length($text)"><xsl:value-of select="$index"/></xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="stringBegin">
				<xsl:with-param name="text" select="$text"/>
				<xsl:with-param name="index" select="number($index) + 1"/>
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="stringEnd">
	<xsl:param name="text"/>
	<xsl:param name="index" select="string-length($text) + 1"/>
	<xsl:choose>
		<xsl:when test="not(matches(substring($text, $index - 1, 1), '\s+')) or $index=1"><xsl:value-of select="$index"/></xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="stringEnd">
				<xsl:with-param name="text" select="$text"/>
				<xsl:with-param name="index" select="number($index) - 1"/>
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
