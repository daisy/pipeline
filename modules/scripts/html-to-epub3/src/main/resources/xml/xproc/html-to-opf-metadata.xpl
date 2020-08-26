<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-to-opf-metadata"
                exclude-inline-prefixes="#all">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		metadata</a> from a HTML document</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>
	<p:option name="identifier-id" select="'pub-id'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The <code>id</code> attribute of the <code>dc:identifier</code> element.</p>
			<p>Note that if the document produced by this step is the first document passed on the
			"metadata" port of the px:epub3-pub-create-package-doc step, the value of this option
			will become the value of the <a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#attrdef-package-unique-identifier"><code>unique-identifier</code></a>
			attribute of the resulting package document.</p>
		</p:documentation>
	</p:option>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/html-to-metadata.xsl"/>
		</p:input>
		<p:with-param name="identifier-id" select="$identifier-id"/>
	</p:xslt>
	
</p:declare-step>
