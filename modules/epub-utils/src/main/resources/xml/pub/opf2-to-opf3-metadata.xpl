<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:opf2-to-opf3-metadata"
                exclude-inline-prefixes="#all">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Convert <a href="http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2">OPF 2.0.1
		metadata</a> to <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		3 metadata</a></p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>OPF 2.0.1 <code>package</code> or <code>metadata</code> document.</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>OPF 3 <code>package</code> or <code>metadata</code> document.</p>
		</p:documentation>
	</p:output>
	<p:option name="compatibility-mode" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to stay backward compatible with OPF 2.0.1.</p>
		</p:documentation>
	</p:option>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="opf2-to-opf3-metadata.xsl"/>
		</p:input>
		<p:with-param name="compatibility-mode" select="$compatibility-mode"/>
	</p:xslt>

</p:declare-step>
