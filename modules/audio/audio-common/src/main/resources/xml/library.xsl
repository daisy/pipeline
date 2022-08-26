<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:TranscodeAudioFile="org.daisy.pipeline.audio.saxon.impl.TranscodeAudioFileDefinition$TranscodeAudioFile"
                exclude-result-prefixes="#all">

	<doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
		<desc>
			<p>Transcode an audio file.</p>
			<p>Returns a map with the file path ("href", a xs:anyURI) of the new file, and an
			optional "clipBegin" in seconds (a xs:decimal, with millisecond precision) to indicate
			if the audio contained in the original audio file starts at an offset within the new
			file. If "clipBegin" is present, there will also be "clipEnd", "original-clipBegin" and
			"original-clipEnd" attributes. The new file will be stored in the "output-dir" directory
			and it's name will based on the name of the input file.</p>
		</desc>
	</doc>
	<xsl:function name="pf:transcode-audio-file" as="map(*)">
		<xsl:param name="href" as="xs:anyURI"/>
		<xsl:param name="old-file-type" as="xs:string?"/>
		<xsl:param name="new-file-type" as="xs:string"/>
		<xsl:param name="output-dir" as="xs:anyURI"/>
		<xsl:sequence select="TranscodeAudioFile:run(
		                        TranscodeAudioFile:new(),
		                        $href, $old-file-type, $new-file-type, $output-dir)">
			<!--
				Implemented in ../../java/org/daisy/pipeline/audio/saxon/impl/TranscodeAudioFileDefinition.java
			-->
		</xsl:sequence>
	</xsl:function>

</xsl:stylesheet>
