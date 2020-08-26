<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:dtbook-to-opf-metadata"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		metadata</a> from a DTBook</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="dtbook-to-opf-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
