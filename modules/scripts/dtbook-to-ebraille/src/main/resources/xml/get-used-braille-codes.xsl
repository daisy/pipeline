<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl">
		<!-- pf:braille-code-from-language-tag -->
	</xsl:import>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/*" priority="1">
		<xsl:next-match/>
		<xsl:variable name="braille-codes" as="map(xs:string,xs:integer)">
			<xsl:apply-templates mode="braille-codes" select=".">
				<xsl:with-param name="collect" select="map{}"/>
				<xsl:with-param name="braille-code" select="()"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:result-document href="braille-codes">
			<d:codes>
				<xsl:for-each select="map:keys($braille-codes)">
					<d:code weight="{$braille-codes(.)}"><xsl:value-of select="."/></d:code>
				</xsl:for-each>
			</d:codes>
		</xsl:result-document>
	</xsl:template>

	<xsl:template match="*[@xml:lang]">
		<xsl:param name="result-lang" tunnel="yes" as="xs:string?" select="()"/>
		<xsl:variable name="lang" select="string(@xml:lang)"/>
		<xsl:variable name="lang" select="(substring-before($lang,'-t-'),$lang)[not(.='')][1]"/>
		<xsl:choose>
			<!-- unwrap the temporary _ elements inserted by abstract-block-translator.xsl -->
			<xsl:when test="$lang=$result-lang and self::_[not(@* except @xml:lang)]">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<!-- drop redundant xml:lang attributes -->
					<xsl:if test="not($lang=$result-lang)">
						<xsl:attribute name="xml:lang" select="$lang"/>
					</xsl:if>
					<xsl:apply-templates select="@* except @xml:lang"/>
					<xsl:apply-templates>
						<xsl:with-param name="result-lang" tunnel="yes" select="$lang"/>
					</xsl:apply-templates>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="braille-codes" match="*|text()" as="map(xs:string,xs:integer)">
		<xsl:param name="collect" as="map(xs:string,xs:integer)" required="yes"/>
		<xsl:param name="braille-code" as="xs:string?" required="yes"/>
		<xsl:variable name="braille-code" as="xs:string?"
		              select="if (@xml:lang)
		                      then pf:braille-code-from-language-tag(@xml:lang)
		                      else $braille-code"/>
		<xsl:choose>
			<xsl:when test="descendant::*/@xml:lang">
				<xsl:iterate select="*|text()">
					<xsl:param name="collect" as="map(xs:string,xs:integer)" select="$collect"/>
					<xsl:on-completion>
						<xsl:sequence select="$collect"/>
					</xsl:on-completion>
					<xsl:next-iteration>
						<xsl:with-param name="collect" as="map(xs:string,xs:integer)">
							<xsl:apply-templates mode="braille-codes" select=".">
								<xsl:with-param name="collect" select="$collect"/>
								<xsl:with-param name="braille-code" select="$braille-code"/>
							</xsl:apply-templates>
						</xsl:with-param>
					</xsl:next-iteration>
				</xsl:iterate>
			</xsl:when>
			<xsl:when test="$braille-code">
				<xsl:sequence select="map:put(
				                        $collect,
				                        $braille-code,
				                        sum(($collect($braille-code),string-length(normalize-space(.)))))"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
