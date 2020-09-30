<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="px:epub-upgrade-package-doc"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Upgrade from a <a href="http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm">EPUB 2.0.1
		Package Document</a> to a <a
		href="http://www.idpf.org/epub/301/spec/epub-publications.html">EPUB 3 Package
		Document</a>.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset containing the OPF 2.0.1 <code>package</code> document and all other
			resources in the publication.</p>
			<p>References in the package document to resources that are not included in the fileset
			are removed.</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true"/>
	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>OPF 3 <code>package</code> document.</p>
		</p:documentation>
		<p:pipe step="opf3" port="result"/>
	</p:output>
	<p:output port="result.fileset">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset manifest with as single file the package document.</p>
		</p:documentation>
		<p:pipe step="load" port="result.fileset"/>
	</p:output>
	<p:option name="compatibility-mode" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to be backward compatible with OPF 2.0.1.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="detect-properties.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			pxi:epub3-detect-properties
		</p:documentation>
	</p:import>

	<px:fileset-load media-types="application/oebps-package+xml" name="load">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>

	<p:documentation>
		* Update metadata
		* Remove "guide" element
		* Remove "tours" element
		* Remove "required-namespace" and "fallback-style" attributes
	</p:documentation>
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="upgrade-package-doc.xsl"/>
		</p:input>
		<p:with-param name="compatibility-mode" select="$compatibility-mode"/>
	</p:xslt>

	<p:documentation>
		Detect properties of content documents
	</p:documentation>
	<pxi:epub3-detect-properties name="opf3">
		<p:input port="content-docs">
			<p:pipe step="html" port="result"/>
		</p:input>
	</pxi:epub3-detect-properties>
	<p:sink/>

	<px:fileset-load media-types="application/xhtml+xml" name="html">
		<p:input port="fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>

</p:declare-step>
