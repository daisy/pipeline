<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
	xmlns:d="http://www.daisy.org/ns/z3998/authoring/features/description/"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:z="http://www.daisy.org/ns/z3998/authoring/" exclude-result-prefixes="#all" version="2.0">


	<xsl:template match="d:description">
		<aside>
			<xsl:apply-templates select="@*|node()"/>
		</aside>
	</xsl:template>

	<xsl:template match="d:body">
		<details>
			<xsl:apply-templates select="@*|node()"/>
		</details>
	</xsl:template>
	<xsl:template match="d:summary">
		<summary>
			<xsl:apply-templates select="@*|node()"/>
		</summary>
	</xsl:template>
	<xsl:template match="d:longdesc[not(*)]">
		<p>
			<xsl:value-of select="."/>
		</p>
	</xsl:template>

</xsl:stylesheet>
