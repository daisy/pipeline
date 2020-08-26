<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:smil-to-audio-fileset"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>List the distinct files referenced from audio elements in a SMIL.</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="smil-to-audio-fileset.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
</p:declare-step>
