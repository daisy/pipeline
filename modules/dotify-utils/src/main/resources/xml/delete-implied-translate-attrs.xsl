<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.daisy.org/ns/2011/obfl"
                xpath-default-namespace="http://www.daisy.org/ns/2011/obfl">

	<xsl:template match="*[@xml:lang|@translate]">
		<xsl:param name="lang" tunnel="yes" as="xs:string?" select="()"/>
		<xsl:param name="source-translate" tunnel="yes" as="xs:string?" select="()"/>
		<xsl:param name="result-translate" tunnel="yes" as="xs:string?" select="()"/>
		<xsl:variable name="lang" as="xs:string?" select="(@xml:lang,$lang)[1]"/>
		<xsl:variable name="source-translate" as="xs:string?" select="(@translate,$source-translate)[1]"/>
		<xsl:variable name="mode" as="xs:string" select="($source-translate,'')[1]"/>
		<xsl:variable name="implied-mode-without-translate" as="xs:string"
		              select="if ($lang[tokenize(.,'-')='Brai'])
		                      then 'pre-translated-text-css'
		                      else ($result-translate,'')[1]"/>
		<xsl:copy>
			<xsl:apply-templates select="@* except @translate"/>
			<xsl:choose>
				<xsl:when test="$mode=$implied-mode-without-translate">
					<xsl:apply-templates>
						<xsl:with-param name="lang" tunnel="yes" select="$lang"/>
						<xsl:with-param name="source-translate" tunnel="yes" select="$source-translate"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="translate" select="$mode"/>
					<xsl:apply-templates>
						<xsl:with-param name="lang" tunnel="yes" select="$lang"/>
						<xsl:with-param name="source-translate" tunnel="yes" select="$source-translate"/>
						<xsl:with-param name="result-translate" tunnel="yes" select="$mode"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
