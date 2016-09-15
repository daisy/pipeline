<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step
	xmlns:p="http://www.w3.org/ns/xproc"
	xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
	xmlns:c="http://www.w3.org/ns/xproc-step"
	xmlns:d="http://www.daisy.org/ns/pipeline/data"
	xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	exclude-inline-prefixes="#all"
	type="odt:get-file"
	name="get-file"
	version="1.0">
	
	<p:input port="fileset.in"/>
	<p:input port="in-memory.in" sequence="true"/>
	<p:option name="href"/>
	<p:output port="result" primary="true">
		<p:pipe step="maybe-load-file" port="result"/>
	</p:output>
	<p:output port="fileset.out">
		<p:pipe step="get-file" port="fileset.in"/>
	</p:output>
	<p:output port="in-memory.out" sequence="true">
		<p:pipe step="maybe-load-file" port="in-memory.out"/>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
	
	<p:variable name="base" select="//d:file[starts-with(@media-type,'application/vnd.oasis.opendocument')]/resolve-uri(@href, base-uri(.))">
		<p:pipe step="get-file" port="fileset.in"/>
	</p:variable>
	
	<p:split-sequence name="file-already-in-memory">
		<p:input port="source">
			<p:pipe step="get-file" port="in-memory.in"/>
		</p:input>
		<p:with-option name="test" select="concat('base-uri(/*)=&quot;', resolve-uri($href, $base), '&quot;')">
			<p:empty/>
		</p:with-option>
	</p:split-sequence>
	<p:count/>
	
	<p:choose name="maybe-load-file">
		<p:when test="number(/c:result) > 0">
			<p:output port="result" primary="true">
				<p:pipe step="file-already-in-memory" port="matched"/>
			</p:output>
			<p:output port="in-memory.out" sequence="true">
				<p:pipe step="get-file" port="in-memory.in"/>
			</p:output>
			<p:sink/>
		</p:when>
		<p:otherwise>
			<p:output port="result" primary="true">
				<p:pipe step="load-file" port="result"/>
			</p:output>
			<p:output port="in-memory.out" sequence="true">
				<p:pipe step="get-file" port="in-memory.in"/>
				<p:pipe step="load-file" port="result"/>
			</p:output>
			<px:fileset-filter>
				<p:input port="source">
					<p:pipe step="get-file" port="fileset.in"/>
				</p:input>
				<p:with-option name="href" select="resolve-uri($href, $base)"/>
			</px:fileset-filter>
			<px:fileset-load name="load-file">
				<p:input port="in-memory">
					<p:empty/>
				</p:input>
			</px:fileset-load>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
