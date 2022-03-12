<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java"
                exclude-result-prefixes="#all">

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Transcode an audio file.</p>
			<p>Returns the file path of the new file. The new file will be stored in the
			"output-dir" directory and it's name will based on the name of the input file.</p>
		</desc>
	</doc>
	<java:function name="pf:transcode-audio-file" as="xs:anyURI">
		<xsl:param name="href" as="xs:anyURI"/>
		<xsl:param name="old-file-type" as="xs:string?"/>
		<xsl:param name="new-file-type" as="xs:string"/>
		<xsl:param name="output-dir" as="xs:anyURI"/>
		<!--
		    Implemented in ../../java/org/daisy/pipeline/audio/saxon/impl/TranscodeAudioFileDefinition.java
		-->
	</java:function>

</xsl:stylesheet>
