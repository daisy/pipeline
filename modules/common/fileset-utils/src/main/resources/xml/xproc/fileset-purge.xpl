<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:fileset-purge" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Remove files that don't exist in memory or on disk.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	<p:output port="result.fileset"/>

	<p:import href="fileset-fix-original-hrefs.xpl"/>

	<pxi:fileset-fix-original-hrefs purge="true" detect-existing="true">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</pxi:fileset-fix-original-hrefs>

</p:declare-step>
