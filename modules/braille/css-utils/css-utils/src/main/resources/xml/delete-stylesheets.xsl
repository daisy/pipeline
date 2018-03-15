<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">
	
	<!--
	    see also parse-xml-stylesheet-instructions.xsl in common-utils
	-->
	
	<xsl:variable name="XML_STYLESHEET_PSEUDO_ATTR_RE">(href|type|title|media|charset|alternate)=("([^"]+)"|'([^']+)')</xsl:variable>
	<xsl:variable name="XML_STYLESHEET_RE"
	              select="concat('^\s*',$XML_STYLESHEET_PSEUDO_ATTR_RE,'(\s+',$XML_STYLESHEET_PSEUDO_ATTR_RE,')*\s*$')"/>
	
	<xsl:template match="/processing-instruction('xml-stylesheet')">
		<xsl:choose>
			<xsl:when test="matches(., $XML_STYLESHEET_RE)">
				<xsl:variable name="parsed" as="element()">
					<_>
						<xsl:analyze-string select="." regex="{$XML_STYLESHEET_PSEUDO_ATTR_RE}">
							<xsl:matching-substring>
								<xsl:attribute name="{regex-group(1)}" select="concat(regex-group(3),regex-group(4))"/>
							</xsl:matching-substring>
						</xsl:analyze-string>
					</_>
				</xsl:variable>
				<xsl:if test="not($parsed[@type='text/css'
				              or (not($parsed/@type[not(.='text/css')]) and matches($parsed/@href,'\.s?css$'))])">
					<xsl:sequence select="."/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*[local-name()='style'][not(@type[not(.='text/css')])]"/>
	
	<xsl:template match="*[local-name()='link'][@rel='stylesheet' and not(@type[not(.='text/css')]) and matches(@href,'\.s?css$')]"/>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
