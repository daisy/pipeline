<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:obfl="http://www.daisy.org/ns/2011/obfl" xmlns:pef="http://www.daisy.org/ns/2008/pef" xmlns:dc="http://purl.org/dc/elements/1.1/">
	<xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="timestamp"/>

	<xsl:template match="/">
		<html>
			<head>
				<title>Dotify Formatter Tests</title>
				<style type="text/css">
					div.volume, div.section {
						border-left-style: solid;
						border-color: #ddd;
						border-width: 1px;
						margin: 1em 1em;
					}
					div.page {
						margin: 1em 1em;
						border-style: solid;
						border-width: 1px;
						background-color: #fefefe;
					}
					div.label {
						font-family: sans-serif;
						text-color: green;
					}
					span.element {
						padding-right: 1em;
					}
					body {
						font-family: sans-serif;
						margin: 2em 2em;
					}
					h1 {
						margin-top: 2em;
					}
					a.internal {
						color: white;
						text-decoration: none;
					}
					a.internal:hover {
						color: grey;
					}
					.example {
					    padding: .5em;
    					border-left-width: .5em;
    					border-left-style: solid;
 						border-color: #e0cb52;
  						background-color: #fcfaee;
					}
				</style>
			</head>
			<body>
				<p>Updated: <xsl:value-of select="$timestamp"/></p>
				<xsl:apply-templates/>
			</body>		
		</html>
	</xsl:template>
	<xsl:template match="tests">
		<xsl:for-each select="test">
			<xsl:variable name="input" select="document(@input)"/>
			<h1 id="{@input}"><xsl:value-of select="$input/obfl:obfl/obfl:meta/dc:title"/><a href="#{@input}" class="internal"> &#x00B6;</a></h1>
			<p><xsl:value-of select="$input/obfl:obfl/obfl:meta/dc:description"/></p>
			<h2><xsl:value-of select="@input"/></h2>
			<pre class="example">
				<xsl:apply-templates select="$input" mode="display-element"/>
			</pre>
			<h2><xsl:value-of select="@expected"/></h2>
			<xsl:variable name="expected" select="document(@expected)"/>
			<pre class="example">
				<xsl:apply-templates select="$expected/pef:pef" mode="display-element"/>
			</pre>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="*" mode="display-element">
		<xsl:text>&#x000A;</xsl:text>
		<xsl:for-each select="ancestor::*">
			<xsl:text>  </xsl:text>
		</xsl:for-each>
		<xsl:choose>
			<xsl:when test="count(node())>0">
				<xsl:text>&lt;</xsl:text><xsl:value-of select="name()"/>
				<xsl:apply-templates select="." mode="display-attributes"/>
				<xsl:text>&gt;</xsl:text>
				<xsl:apply-templates mode="display-element"/>
				<xsl:if test="*">
					<xsl:text>&#x000A;</xsl:text>
					<xsl:for-each select="ancestor::*">
						<xsl:text>  </xsl:text>
					</xsl:for-each>
				</xsl:if>
				<xsl:text>&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text>&gt;</xsl:text>
			</xsl:when>
			<xsl:otherwise><xsl:text>&lt;</xsl:text><xsl:value-of select="name()"/><xsl:apply-templates select="." mode="display-attributes"/><xsl:text>/&gt;</xsl:text></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="*" mode="display-attributes">
		<xsl:for-each select="@*">
			<xsl:text> </xsl:text><xsl:value-of select="name()"/><xsl:text>="</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="text()[normalize-space()='']" mode="display-element"></xsl:template>
	
</xsl:stylesheet>
