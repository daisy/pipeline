<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:opf-to-html-metadata"
                name="main"
                exclude-inline-prefixes="#all">
	
	<!--
	    This step is specific to EPUB 3 to DAISY 3. The result is only suitable for further
	    processing into DTBook metadata (see epub3-to-dtbook.xsl). In HTML, dublin core metadata is
	    supposed to be encoded with a "DC." prefix, e.g.:
	    
	        <meta name    = "DC.Creator"
	              content = "Simpson, Homer">
	    
	    Associating prefixes with namespaces should not be done using an epub:prefix attribute, but
	    with link tags, e.g.:
	    
	        <link rel  = "schema.DC"
	              href = "http://purl.org/DC/elements/1.0/">
	    
	    See https://datatracker.ietf.org/doc/html/rfc2731
	-->

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Convert <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		metadata</a> to HTML metadata.</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			A OPF <code>package</code> or <code>metadata</code> document.
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			A HTML <code>head</code> document.
		</p:documentation>
	</p:output>

	<!-- for testing purposes -->
	<p:input port="parameters" kind="parameter" primary="false"/>

	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub3-merge-prefix
		</p:documentation>
	</p:import>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/opf-to-html-metadata.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</p:xslt>

	<!-- normalize epub:prefix attribute -->
	<px:epub3-merge-prefix implicit-input-prefixes="a11y:      http://www.idpf.org/epub/vocab/package/a11y/#
	                                                dcterms:   http://purl.org/dc/terms/
	                                                dc:        http://purl.org/dc/elements/1.1/
	                                                marc:      http://id.loc.gov/vocabulary/
	                                                media:     http://www.idpf.org/epub/vocab/overlays/#
	                                                onix:      http://www.editeur.org/ONIX/book/codelists/current.html#
	                                                rendition: http://www.idpf.org/vocab/rendition/#
	                                                schema:    http://schema.org/
	                                                xsd:       http://www.w3.org/2001/XMLSchema#"
	                       implicit-output-prefixes="dc: http://purl.org/dc/elements/1.1/"/>

</p:declare-step>
