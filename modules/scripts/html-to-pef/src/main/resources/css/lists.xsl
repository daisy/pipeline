<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="org.daisy.pipeline.css.StyleAccessor"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:param name="style"/>

	<!-- Handle "start" attribute (https://html.spec.whatwg.org/multipage/grouping-content.html#attr-ol-start) -->

	<xsl:template match="ol[@start[matches(.,'^\s*[+-]?[0-9]+\s*$')]]">
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
			                        concat('list-item ',xs:integer(number(@start)) - 1),
			                        $counter-reset
			                      ),' ')"/>
			<!-- append to existing style attribute -->
			<xsl:attribute name="style" select="string-join((
			                                      @style,
			                                      concat('counter-reset: ',$counter-reset)
			                                    ),'; ')"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Handle "value" attribute (https://html.spec.whatwg.org/multipage/grouping-content.html#attr-li-value) -->

	<xsl:template match="li[@value[matches(.,'^\s*[+-]?[0-9]+\s*$')]]">
		<xsl:copy>
			<xsl:apply-templates select="@* except @style"/>
            <!--
			    Note that counter properties follow the cascading rules as normal, so if a
			    counter-set declaration is already present, it needs to be modified instead of
			    simply adding another counter-set declaration.
			-->
			<xsl:variable name="counter-set" as="xs:string?" select="css:get($style,.,'counter-set')"/>
			<!-- prepend to existing counter-set declaration so that we do not override anything -->
			<xsl:variable name="counter-set" as="xs:string"
			              select="string-join((
			                        concat('list-item ',xs:integer(number(@value))),
			                        $counter-set
			                      ),' ')"/>
			<!-- append to existing style attribute -->
			<xsl:attribute name="style" select="string-join((
			                                      @style,
                                                  concat('counter-set: ',$counter-set)
			                                    ),'; ')"/>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
