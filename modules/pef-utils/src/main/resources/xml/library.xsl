<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Encode a (Unicode) braille string using the specified character set.</p>
		</desc>
	</doc>
	<java:function name="pf:pef-encode" as="xs:string">
		<xsl:param name="table" as="xs:string"/>
		<xsl:param name="braille" as="xs:string"/>
		<!--
			Implemented in ../../java/org/daisy/pipeline/braille/pef/saxon/impl/EncodeDefinition.java
		-->
	</java:function>

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Decode a braille string in the specified character set (to Unicode braille).</p>
		</desc>
	</doc>
	<java:function name="pf:pef-decode" as="xs:string">
		<xsl:param name="table" as="xs:string"/>
		<xsl:param name="text" as="xs:string"/>
		<!--
			Implemented in ../../java/org/daisy/pipeline/braille/pef/saxon/impl/DecodeDefinition.java
		-->
	</java:function>

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Assert that the given medium is a braille file format.</p>
			<p>Raises an error if the condition is not met.</p>
			<p>Returns the gived medium if the condition is met.</p>
		</desc>
	</doc>
	<xsl:function name="pf:pef-assert-embossable" as="item()"
	              xmlns:PefMediaFunctions="org.daisy.pipeline.braille.pef.saxon.impl.PefMediaFunctionProvider$PefMediaFunctions">
		<xsl:param name="medium" as="item()"/>
		<xsl:sequence select="PefMediaFunctions:assertEmbossable($medium)">
			<!--
			    Implemented in ../../java/org/daisy/pipeline/braille/pef/saxon/impl/PefMediaFunctionProvider.java
			-->
		</xsl:sequence>
	</xsl:function>

</xsl:stylesheet>
