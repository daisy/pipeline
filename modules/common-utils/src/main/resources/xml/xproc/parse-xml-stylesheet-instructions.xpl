<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="px"
                type="px:parse-xml-stylesheet-instructions">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract and parse <code>&lt;?xml-stylesheet?&gt;</code> processing instructions from a
		document.</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>XML document with <code>&lt;?xml-stylesheet?&gt;</code> processing instructions</p>
		</p:documentation>
	</p:input>
	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><code>d:xml-stylesheet-instructions</code> document</p>
		</p:documentation>
		<p:pipe step="parsed" port="result"/>
	</p:output>
	<p:output port="fileset">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result as a <code>d:fileset</code> document. Files that stem from a PI with a
			<code>media</code> pseudo-attribute get a <code>stylesheet-media</code> attribute. The
			<code>media-type</code> is inferred from the file extension if the PI has no
			<code>type</code> pseudo-attribute.</p>
		</p:documentation>
		<p:pipe step="fileset" port="result"/>
	</p:output>

	<p:xslt name="parsed">
		<p:input port="stylesheet">
			<!--
			    note that we do it in XSLT but it could also be implemented in Java (see
			    https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/tree/util/ProcInstParser.html)
			-->
			<p:document href="../xslt/parse-xml-stylesheet-instructions.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

	<p:xslt name="fileset">
		<p:input port="stylesheet">
			<p:document href="../xslt/fileset-from-xml-stylesheet-instructions.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
