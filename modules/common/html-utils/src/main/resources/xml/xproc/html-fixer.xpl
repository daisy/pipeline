<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-fixer"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Fix structure of HTML document.</p>
		<p>Make all elements' contents match the content models.</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/html-fixer.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
