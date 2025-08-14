<?xml version="1.0" encoding="UTF-8"?>
<!--
    Add xml:lang to dtbook
    Version
    2008-03-07
    
    Description
    Adds @xml:lang to dtbook, if dc:Language metadata is present
    
    Nodes
    dtbook
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    James Pritchett, RFB&D
	Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
				exclude-result-prefixes="dtb">
    
    <xsl:include href="recursive-copy.xsl"/>
    <xsl:include href="output.xsl"/>
	<xsl:include href="library.xsl"/>
    
    <xsl:param name="documentLanguage" select="''"/>


    <xsl:template match="dtb:dtbook">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="count(@xml:lang)=0">
				<xsl:choose>
					<xsl:when test="$documentLanguage!=''">
						<xsl:message terminate="no">Adding @xml:lang to dtbook element</xsl:message>
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="$documentLanguage"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test="count(dtb:head/dtb:meta[@name='dc:Language'])&gt;0">
						<xsl:message terminate="no">Adding @xml:lang to dtbook element</xsl:message>
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="dtb:head/dtb:meta[@name='dc:Language']/@content"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message terminate="no">Adding @xml:lang to dtbook element</xsl:message>
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="pf:default-locale()"/>
						</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
            </xsl:if>
            <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
