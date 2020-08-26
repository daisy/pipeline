<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:opf-to-ncc-metadata"
                name="main"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract NCC metadata from a <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-doc">EPUB package document</a></p>
	</p:documentation>
	
	<p:input port="source" primary="true"/>
	<p:input port="ncc-body">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The <code>body</code> element of the NCC.</p>
		</p:documentation>
	</p:input>
	<p:input port="smil" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The SMIL documents</p>
		</p:documentation>
	</p:input>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="main" port="ncc-body"/>
			<p:pipe step="main" port="smil"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="../../xslt/opf-to-ncc-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
