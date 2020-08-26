<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-epub3.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
	
	<p:output port="fileset" primary="true"/>
	<p:output port="in-memory" sequence="true">
		<p:pipe step="load" port="result.in-memory"/>
	</p:output>
	
	<p:option name="epub" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub-load
		</p:documentation>
	</p:import>
	
	<px:epub-load version="3" name="load">
		<p:with-option name="href" select="$epub"/>
	</px:epub-load>
	
</p:declare-step>
