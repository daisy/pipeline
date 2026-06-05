<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:epub3-nav-from-ncx">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Creates a EPUB 3 Navigation Document from a NCX</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A NCX document</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The navigation document</p>
			<p>The links in the navigation document point to whatever the NCX points to. If these
			need to be transformed, for example from SMIL to HTML references, this should either be
			done by pre-processing the NCX or by post-processing the navigation document.</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="ncx-to-nav.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
