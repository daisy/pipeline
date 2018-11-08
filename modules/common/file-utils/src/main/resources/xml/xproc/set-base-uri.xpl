<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:set-base-uri"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="px">
	
	<p:input port="source"/>
	<p:output port="result"/>
	<p:option name="base-uri"/>
	
	<p:xslt>
		<p:with-option name="output-base-uri" select="$base-uri"/>
		<p:with-param name="output-base-uri" select="$base-uri"/>
		<p:input port="stylesheet">
			<p:inline>
				<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
					<xsl:param name="output-base-uri" required="yes"/>
					<xsl:template match="/*">
						<xsl:sequence select="."/>
					</xsl:template>
					<!-- if xml:base attribute is defined on document element, also adapt it -->
					<xsl:template match="/*[@xml:base]" priority="1">
						<xsl:copy>
							<xsl:sequence select="@* except @xml:base"/>
							<xsl:attribute name="xml:base" select="$output-base-uri"/>
							<xsl:sequence select="node()"/>
						</xsl:copy>
					</xsl:template>
				</xsl:stylesheet>
			</p:inline>
		</p:input>
	</p:xslt>
	
</p:declare-step>
