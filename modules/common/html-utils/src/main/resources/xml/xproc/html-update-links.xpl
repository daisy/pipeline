<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-update-links"
                name="main"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Update the links in a HTML document after resources have been relocated.</p>
		<p>Can also be used to process an SVG document.</p>
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input HTML document</p>
			<p>If the HTML document itself is being relocated, the base URI of this document is
			assumed to correspond with the original location.</p>
		</p:documentation>
	</p:input>
	
	<p:input port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that defines the relocation of resources.</p>
		</p:documentation>
	</p:input>
	
	<p:option name="source-renamed" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the source document itself has previously been renamed according to "mapping"
			or not. In other words, whether the URI of the source document is to be compared with
			the <code>href</code> rather than the <code>original-href</code> attributes of the
			"mapping" document. By default it is assumed that the renaming is done after this
			step.</p>
		</p:documentation>
	</p:option>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output HTML document with updated links</p>
		</p:documentation>
	</p:output>
	
	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="main" port="mapping"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="../xslt/html-update-links.xsl"/>
		</p:input>
		<p:with-param name="source-renamed" select="$source-renamed"/>
	</p:xslt>

</p:declare-step>
