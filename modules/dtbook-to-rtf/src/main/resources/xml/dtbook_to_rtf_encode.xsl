<?xml version="1.0" encoding="utf-8"?>	
<!-- Template rtf-encode documentation
	Encodes a string for insertion in an RTF file and writes it to the resulting RTF file
	Any character below unicode codepoint 160 is written as is, 
	characters above unicode codepoint 160 is encoded using \uN? or \'X, 
	where N is the decimal value of the codepoint, X is the hex value of the codepoint
	and ? is the character used by apps not being able to display the unicide character
	Params:
	  str:        The string to encode
	  encMethod:  When 1 use \uN? style encoding
	              When 2 use \'X style encoding
	              Else replace characters above 160 with ?
-->
<!-- XSLT 2.0 version - makes use of built in xslt 2.0 functions to do rtf encoding -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
	<xsl:template name="rtf-encode">
		<xsl:param name="str"/>
		<xsl:param name="escMethod">1</xsl:param>
		<xsl:if test="$str">
			<xsl:for-each select="string-to-codepoints($str)">
				<xsl:choose>
					<xsl:when test=". = 92">\\</xsl:when>
					<xsl:when test=". = 123">\{</xsl:when>
					<xsl:when test=". = 125">\}</xsl:when>
					<xsl:when test=". &lt; 160">
						<xsl:value-of select="codepoints-to-string(.)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="$escMethod=1">
								<xsl:value-of select="concat('\u',.,'?')"/>
							</xsl:when>
							<xsl:when test="$escMethod=2">
								<xsl:value-of select="concat('\''',.)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>?</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>	
</xsl:stylesheet>
<!-- DBB version - makes use of a user defined function to do rtf encoding -->
<!--
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 	
	xmlns:dbb="http://www.dbb.dk/functions">	
	<xsl:template name="rtf-encode">
		<xsl:param name="str"/>
		<xsl:param name="escMethod">1</xsl:param>
		<xsl:if test="$str">
			<xsl:value-of select="dbb:RtfEncode($str, $escMethod)"/>
		</xsl:if>
	</xsl:template>	
</xsl:stylesheet>
-->
