<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:smil-update-links"
                name="main"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Update the links in a SMIL document after resources have been relocated.</p>
	</p:documentation>

	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input SMIL document</p>
			<p>If the SMIL document itself is being relocated, the base URI of this document is
			assumed to correspond with the original location.</p>
		</p:documentation>
	</p:input>

	<p:input port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that defines the relocation of resources.</p>
		</p:documentation>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output SMIL document with updated links</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="main" port="mapping"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="smil-update-links.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
