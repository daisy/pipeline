<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:epub3-detect-properties"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Detect <a
		href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-item-property-values">manifest
		item properties</a>:</p>
		<ul>
			<ol><a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#mathml"><code>mathml</code></a>:
			when a document contains instances of MathML markup</ol>
			<ol><a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#remote-resources"><code>remote-resources</code></a>:
			when a document contains references to other publication resources that are <a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-resource-locations">located
			outside of the EPUB container</a></ol>
			<ol><a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#scripted"><code>scripted</code></a>:
			when a document is a <a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub-scripted">scripted
			content document</a> (contains scripted content and/or elements from HTML5 forms)</ol>
			<ol><a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#svg"><code>svg</code></a>:
			when a document is a <a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub-svg">SVG
			content document</a> or contains instances of SVG markup</ol>
			<ol><a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#switch"><code>switch</code></a>:
			when a document contains <a
			href="http://www.idpf.org/epub/301/spec/epub-contentdocs.html#elemdef-switch"><code>epub:switch</code></a>
			elements</ol>
		</ul>
	</p:documentation>

	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>package</code> or <code>manifest</code> document.</p>
		</p:documentation>
	</p:input>
	<p:input port="content-docs" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The content documents</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Resulting <code>package</code> or <code>manifest</code> document with item properties
			added.</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="main" port="content-docs"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="detect-properties.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
