<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                exclude-result-prefixes="#all">

	<xsl:param name="hidden" required="yes" as="xs:boolean"/>

	<xsl:template match="*[@epub:type/tokenize(.,'\s+')[not(.='')]='pagebreak']
	                      [not(*) and normalize-space(string(.))='']
	                      [@title|@aria-label]">
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:choose>
				<xsl:when test="$hidden">
					<xsl:attribute name="style" select="string-join(('visibility: hidden',@style/string(.)),'; ')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:sequence select="@style"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="(@aria-label,@title)[1]"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
