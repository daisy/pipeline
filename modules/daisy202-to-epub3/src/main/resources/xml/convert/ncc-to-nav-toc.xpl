<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:ncc-to-epub3-nav-toc" name="main">

	<p:input port="source">
		<p:documentation  xmlns="http://www.w3.org/1999/xhtml">
			<p>A NCC</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation  xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>epub:type='toc'</code> document for inclusion in a EPUB navigation
			document.</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="ncc-to-nav-toc.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
