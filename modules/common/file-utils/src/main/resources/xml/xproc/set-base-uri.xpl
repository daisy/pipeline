<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:set-base-uri"
                exclude-inline-prefixes="px">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Changes the base URI of the document.</p>
		<p>px:set-base-uri has an effect on all elements that do not have an ancestor with an
		absolute xml:base, unless that element is the root element. Whether an element has a
		different base URI than its parent does not matter as long as this is not made explicit with
		an xml:base attribute. Elements with a relative xml:base are also affected by the
		px:set-base-uri in that their absolute base URI changes accordingly, but the xml:base
		attribute is not changed. Elements with an absolute xml:base, and their descendants, are not
		affected by the px:set-base-uri.</p>
	</p:documentation>
	
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
					<xsl:template match="/">
						<xsl:document>
							<xsl:sequence select="/processing-instruction()"/>
							<xsl:apply-templates select="/*"/>
						</xsl:document>
					</xsl:template>
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
