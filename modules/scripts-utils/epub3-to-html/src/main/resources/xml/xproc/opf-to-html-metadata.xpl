<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:opf-to-html-metadata"
                name="main"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Convert <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		metadata</a> to a HTML metadata.</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			A OPF <code>package</code> or <code>metadata</code> document.
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			A HTML <code>head</code> document.
		</p:documentation>
	</p:output>

	<!-- for testing purposes -->
	<p:input port="parameters" kind="parameter" primary="false"/>

	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub3-merge-prefix
		</p:documentation>
	</p:import>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/opf-to-html-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</p:xslt>

	<!-- normalize epub:prefix attribute -->
	<px:epub3-merge-prefix implicit-output-prefixes="dc: http://purl.org/dc/elements/1.1/"/>

</p:declare-step>
