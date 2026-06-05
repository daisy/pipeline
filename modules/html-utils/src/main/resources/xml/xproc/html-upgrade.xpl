<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-upgrade"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Upgrade a HTML document to HTML 5</p>
	</p:documentation>
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input HTML document</p>
		</p:documentation>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The HTML 5 document</p>
		</p:documentation>
	</p:output>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/html5-upgrade.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
