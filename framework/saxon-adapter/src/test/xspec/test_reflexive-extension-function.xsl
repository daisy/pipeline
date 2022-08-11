<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:MyClass="MyClass">

	<xsl:function name="x:test" as="xs:string">
		<xsl:variable name="x" select="MyClass:new('foobar')"/>
		<xsl:variable name="x" select="MyClass:uppercase($x)"/>
		<xsl:variable name="x" select="MyClass:wrapInMap($x)"/>
		<xsl:sequence select="string($x('result'))"/>
	</xsl:function>

</xsl:stylesheet>
