<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:MyClass="MyClassProvider$MyClass">

	<xsl:function name="x:test" as="xs:string">
		<xsl:variable name="x" select="MyClass:new('foobar')"/>
		<xsl:variable name="x" select="MyClass:uppercase($x)"/>
		<xsl:variable name="x" select="MyClass:innerClass($x)"/>
		<xsl:variable name="x" select="MyClass:outerClass($x)"/>
		<xsl:variable name="x" select="MyClass:wrapInArray($x)"/>
		<xsl:variable name="x" select="$x(1)"/>
		<xsl:variable name="x" select="MyClass:wrapInMap($x)"/>
		<xsl:variable name="x" select="$x('result')"/>
		<xsl:sequence select="string($x)"/>
	</xsl:function>

</xsl:stylesheet>
