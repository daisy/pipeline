<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:oebps-to-ncc-metadata"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract NCC metadata from a <a
		href="http://web.archive.org/web/20101221093536/http://www.idpf.org/oebps/oebps1.2/download/oeb12-xhtml.htm">OEBPS</a>
		package document</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="opf-to-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
