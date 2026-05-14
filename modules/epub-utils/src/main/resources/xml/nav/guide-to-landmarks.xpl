<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:epub-guide-to-landmarks">

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Either a full EPUB package document or the <code>guide</code> element only.</p>
		</p:documentation>
	</p:input>
	<p:output port="result" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <a
			href="http://idpf.org/epub/301/spec/epub-contentdocs.html#sec-xhtml-nav-def-types-landmarks">"<code>landmarks</code>"
			<code>nav</code> document</a>, or no document if there is no <code>guide</code> element
			in the input.</p>
		</p:documentation>
	</p:output>
	<p:option name="output-base-uri" required="true"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="guide-to-landmarks.xsl"/>
		</p:input>
		<p:with-param name="output-base-uri" select="$output-base-uri"/>
	</p:xslt>

</p:declare-step>
