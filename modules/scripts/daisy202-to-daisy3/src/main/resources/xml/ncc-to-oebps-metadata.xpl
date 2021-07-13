<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:ncc-to-oebps-metadata"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract <a
		href="http://web.archive.org/web/20101221093536/http://www.idpf.org/oebps/oebps1.2/download/oeb12-xhtml.htm">OEBPS
		metadata</a> from a NCC document</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="ncc-to-oebps-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
