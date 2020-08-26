<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                exclude-inline-prefixes="#all"
                type="odt:get-file" name="main">
	
	<p:input port="fileset.in" primary="true"/>
	<p:input port="in-memory.in" sequence="true"/>
	<p:option name="href"/>
	<p:output port="result" primary="true"/>
	<p:output port="result.fileset">
		<p:pipe step="load" port="result.fileset"/>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
		</p:documentation>
	</p:import>
	
	<p:variable name="base" select="//d:file[starts-with(@media-type,'application/vnd.oasis.opendocument')]/resolve-uri(@href, base-uri(.))"/>
	
	<px:fileset-load name="load">
		<p:with-option name="href" select="resolve-uri($href, $base)"/>
		<p:input port="in-memory">
			<p:pipe step="main" port="in-memory.in"/>
		</p:input>
	</px:fileset-load>
	
</p:declare-step>
