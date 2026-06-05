<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:epub3-nav-to-ncx">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Creates a text-only NCX based on a EPUB3 Navigation Document</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="nav-to-ncx.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
