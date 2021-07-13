<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:css-to-fileset" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Create a fileset document for a CSS file.</p>
	</p:documentation>

	<p:option name="source" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>URI of the CSS file.</p>
		</p:documentation>
	</p:option>

	<p:input port="context.fileset" primary="false">
		<p:inline><d:fileset/></p:inline>
	</p:input>
	<p:input port="context.in-memory" primary="false" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Context fileset.</p>
			<p>Will be used for loading the CSS file and other resources. If files are present in
			memory, they are expected to be <code>c:data</code> documents. Only when files are not
			present in this fileset, it will be attempted to load them from disk.</p>
		</p:documentation>
		<p:empty/>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset containing the CSS file itself and all the resources referenced from it.</p>
		</p:documentation>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
		<p:documentation>
			px:mediatype-detect
		</p:documentation>
	</p:import>

	<p:xslt template-name="main">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="css-to-fileset.xsl"/>
		</p:input>
		<p:with-param port="parameters" name="source" select="$source"/>
		<p:with-param port="parameters" name="context.fileset" select="/">
			<p:pipe step="main" port="context.fileset"/>
		</p:with-param>
		<p:with-param port="parameters" name="context.in-memory" select="collection()">
			<p:pipe step="main" port="context.in-memory"/>
		</p:with-param>
	</p:xslt>

	<px:mediatype-detect/>

</p:declare-step>
