<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:parse-xml-stylesheet-instructions" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="px">
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:xslt template-name="main">
		<p:input port="stylesheet">
			<p:document href="parse-xml-stylesheet-instructions.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
</p:declare-step>
