<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Test whether a media query matches a medium..</p>
		</desc>
	</doc>
	<java:function name="pf:media-query-matches" as="xs:boolean">
		<xsl:param name="media-query" as="xs:string"/>
		<xsl:param name="medium" as="xs:string"/>
		<!--
		    Implemented in ../../java/org.daisy/pipeline/css/saxon/impl/MediaQueryMatchesDefinition.java
		-->
	</java:function>

</xsl:stylesheet>
