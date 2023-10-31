<?xml version="1.0" encoding="UTF-8"?>
<!-- ========================================================================= -->
<!-- There are 2 copies of this file:                                          -->
<!-- * scripts/dtbook-to-pef/src/main/resources/css/lists.xsl                  -->
<!-- * scripts/dtbook-to-rtf/src/main/resources/xml/number-lists.xsl           -->
<!-- Whenever you update this file, also update the other copies.              -->
<!-- ========================================================================= -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:css="org.daisy.pipeline.css.StyleAccessor"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/numeral-conversion.xsl"/>

	<xsl:param name="style"/>

	<!-- Handle "start" attribute -->

	<xsl:template match="list[@start]">
		<xsl:variable name="start" as="xs:integer?">
			<xsl:choose>
				<xsl:when test="matches(@start,'^\s*[+-]?[0-9]+\s*$')">
					<xsl:sequence select="xs:integer(number(@start))"/>
				</xsl:when>
				<!-- convert @start to a numeric value when needed -->
				<xsl:when test="@enum=('i','I') and pf:numeric-is-roman(@start)">
					<xsl:sequence select="pf:numeric-roman-to-decimal(@start)"/>
				</xsl:when>
				<xsl:when test="@enum=('a','A')">
					<xsl:sequence select="pf:numeric-alpha-to-decimal(lower-case(@start))"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="exists($start)">
				<xsl:copy>
					<xsl:apply-templates select="@* except @style"/>
					<!--
					    Note that counter properties follow the cascading rules as normal, so if a
					    counter-reset declaration is already present, it needs to be modified instead of
					    simply adding another counter-reset declaration.
					-->
					<xsl:variable name="counter-reset" as="xs:string?" select="css:get($style,.,'counter-reset')"/>
					<!-- prepend to existing counter-reset declaration so that we do not override anything -->
					<xsl:variable name="counter-reset" as="xs:string"
					              select="string-join((
					                        concat('list-item ',$start - 1),
					                        $counter-reset
					                      ),' ')"/>
					<!-- append to existing style attribute -->
					<xsl:attribute name="style" select="string-join((
					                                      @style,
					                                      concat('counter-reset: ',$counter-reset)
					                                    ),'; ')"/>
					<xsl:apply-templates select="node()"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
