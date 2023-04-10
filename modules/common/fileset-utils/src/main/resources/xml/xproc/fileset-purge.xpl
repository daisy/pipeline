<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="px:fileset-purge" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Remove files that don't exist in memory or on disk.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	<p:output port="result.fileset" primary="true">
		<p:pipe step="fix" port="result.fileset"/>
	</p:output>
	<p:output port="purged">
		<p:pipe step="purged" port="result"/>
	</p:output>

	<p:option name="detect-existing" select="true()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Files without a <code>original-href</code> attribute are considered to not exist on
			disk when the <code>detect-existing</code> option is set to <code>false</code>.</p>
		</p:documentation>
	</p:option>
	<p:option name="warn-on-missing" select="true()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to raise warnings for files that exist neither on disk or in memory.</p>
		</p:documentation>
	</p:option>

	<p:import href="fileset-fix-original-hrefs.xpl"/>
	<p:import href="fileset-diff.xpl"/>

	<pxi:fileset-fix-original-hrefs purge="true" name="fix">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:with-option name="detect-existing" select="$detect-existing"/>
		<p:with-option name="warn-on-missing" select="$warn-on-missing"/>
	</pxi:fileset-fix-original-hrefs>
	<p:sink/>

	<px:fileset-diff name="purged" cx:pure="true">
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="secondary">
			<p:pipe step="fix" port="result.fileset"/>
		</p:input>
	</px:fileset-diff>
	<p:sink/>

</p:declare-step>
