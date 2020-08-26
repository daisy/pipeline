<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-downgrade"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Downgrade a HTML 5 document to HTML 4</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input HTML 5 document</p>
		</p:documentation>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The HTML 4 document</p>
		</p:documentation>
	</p:output>

	<p:import href="html-outline.xpl">
		<p:documentation>
			px:html-outline
		</p:documentation>
	</p:import>

	<p:documentation>Normalize the heading ranks to match their respective outline depth.</p:documentation>
	<px:html-outline name="outline" fix-heading-ranks="outline-depth" output-base-uri="file:/irrelevant"/>
	<p:sink/>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="outline" port="content-doc"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="../xslt/html5-to-html4.xsl"/>
		</p:input>
	</p:xslt>

</p:declare-step>
