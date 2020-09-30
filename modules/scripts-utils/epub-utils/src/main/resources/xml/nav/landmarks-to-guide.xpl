<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:epub-landmarks-to-guide">

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Either a full EPUB navigation document or the <a
			href="http://idpf.org/epub/301/spec/epub-contentdocs.html#sec-xhtml-nav-def-types-landmarks">"<code>landmarks</code>"
			<code>nav</code> element</a> only.</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>guide</code> document.</p>
			<p>The element has no child nodes if there is no landmarks section in the input.</p>
		</p:documentation>
	</p:output>
	<p:option name="output-base-uri" required="true"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="landmarks-to-guide.xsl"/>
		</p:input>
		<p:with-param name="output-base-uri" select="$output-base-uri"/>
	</p:xslt>

</p:declare-step>
