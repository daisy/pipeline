<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:opf-spine-to-fileset" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input EPUB3</p>
			<p>Must contain a file with media-type "application/oebps-package+xml".</p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The filtered fileset manifest with only the content items in spine order.</p>
			<p>Missing "media-type" attributes are added based on the info in the package
			document.</p>
		</p:documentation>
	</p:output>

	<p:option name="ignore-missing" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to ignore spine items that are not present in the input fileset, or throw an
			error.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-join
		</p:documentation>
	</p:import>

	<px:fileset-join name="source.fileset">
		<!-- normalize @href -->
	</px:fileset-join>

	<px:fileset-load media-types="application/oebps-package+xml">
		<!-- this also normalizes base URI of OPF -->
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert test-count-min="1" test-count-max="1" error-code="PED01" message="The EPUB must contain exactly one OPF document"
	           name="opf"/>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="opf" port="result"/>
			<p:pipe step="source.fileset" port="result"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="opf-spine-to-fileset.xsl"/>
		</p:input>
		<p:with-param name="ignore-missing" select="$ignore-missing"/>
	</p:xslt>

</p:declare-step>
