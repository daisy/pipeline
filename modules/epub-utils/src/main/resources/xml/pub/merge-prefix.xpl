<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:epub3-merge-prefix">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Merge <code>prefix</code> attributes</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>OPF, HTML or ZedAI document that may have more than one <code>prefix</code>
			(<code>epub:prefix</code>) attribute.</p>
			<p><code>prefix</code> attributes are allowed on any element, not only on the roote
			element (<code>package</code>, <code>html</code> or <code>document</code>).</p>
			<p>If an element has more than one ancestor element with a <code>prefix</code>, all
			except the closest one are ignored.</p>
			<p>In case of ZedAI input, unprefixed CURIEs resolve to the URI
			<code>http://www.daisy.org/z3998/2012/vocab/structure/#</code> (which is the default
			vocabulary as defined by the <a
			href="http://www.daisy.org/z3998/2012/vocab/context/default/">default RDFa
			context</a>).</p>
		</p:documentation>
	</p:input>

	<p:option name="implicit-input-prefixes" select="'a11y:      http://www.idpf.org/epub/vocab/package/a11y/#
	                                                  dc:        http://purl.org/dc/elements/1.1/
	                                                  dcterms:   http://purl.org/dc/terms/
	                                                  marc:      http://id.loc.gov/vocabulary/
	                                                  media:     http://www.idpf.org/epub/vocab/overlays/#
	                                                  onix:      http://www.editeur.org/ONIX/book/codelists/current.html#
	                                                  rendition: http://www.idpf.org/vocab/rendition/#
	                                                  schema:    http://schema.org/
	                                                  xsd:       http://www.w3.org/2001/XMLSchema#'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Prefix declarations that are assumed in the input, i.e. they don't need to be
			declared in order to be recognized.</p>
		</p:documentation>
	</p:option>

	<p:option name="implicit-output-prefixes" select="'#default'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Prefix declarations that will be omitted from the output.</p>
			<p>By default (#default) prefixes that are used but not declared (i.e. implied) in the
			input are also implied in the output.</p>
		</p:documentation>
	</p:option>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result document has at most one <code>prefix</code> (<code>epub:prefix</code>)
			attribute, on the root element. The different <code>prefix</code> attributes in the
			input document are merged in such a way that every prefix is unique and no two prefixes
			are mapped to the same URI. The document is updated at the places where a prefix is used
			that was renamed. Prefixes that are not used anywhere inside the document are skipped
			from the declaration.</p>
		</p:documentation>
	</p:output>

	<p:xslt name="metadata-with-merged-prefix">
		<p:input port="stylesheet">
			<p:document href="merge-prefix.xsl"/>
		</p:input>
		<p:with-param name="implicit-input-prefixes" select="$implicit-input-prefixes">
			<p:empty/>
		</p:with-param>
		<p:with-param name="implicit-output-prefixes" select="$implicit-output-prefixes">
			<p:empty/>
		</p:with-param>
	</p:xslt>

</p:declare-step>
