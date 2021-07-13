<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="pxi:html-extract-svg" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract SVG images from the HTML documents into their own files.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="update" port="result.in-memory"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:set-base-uri
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-filter
			px:fileset-filter-in-memory
			px:fileset-load
			px:fileset-join
			px:fileset-update
		</p:documentation>
	</p:import>

	<px:fileset-filter media-types="application/xhtml+xml" name="filter-html">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-filter>
	<px:fileset-load>
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>

	<p:for-each name="extract">
		<p:output port="html-with-links">
			<p:pipe step="html-with-links" port="result"/>
		</p:output>
		<p:output port="svg" sequence="true">
			<p:pipe step="svg" port="result"/>
		</p:output>
		<p:identity name="html"/>
		<p:sink/>
		<p:xslt name="xslt">
			<p:input port="source">
				<p:pipe step="html" port="result"/>
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
			<p:input port="stylesheet">
				<p:document href="../xslt/extract-svg.xsl"/>
			</p:input>
			<p:input port="parameters">
				<p:empty/>
			</p:input>
		</p:xslt>
		<px:set-base-uri>
			<!-- don't know why this is needed -->
			<p:with-option name="base-uri" select="base-uri(/*)">
				<p:pipe step="html" port="result"/>
			</p:with-option>
		</px:set-base-uri>
		<p:identity name="html-with-links"/>
		<p:sink/>
		<p:identity name="svg">
			<p:input port="source">
				<p:pipe step="xslt" port="secondary"/>
			</p:input>
		</p:identity>
	</p:for-each>

	<px:fileset-filter-in-memory name="filter-in-memory">
		<p:input port="source.fileset">
			<p:inline><d:fileset/></p:inline>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="extract" port="svg"/>
		</p:input>
	</px:fileset-filter-in-memory>
	<p:sink/>
	<p:add-attribute match="d:file" attribute-name="media-type" attribute-value="image/svg+xml" name="svg.fileset">
		<p:input port="source">
			<p:pipe step="filter-in-memory" port="not-in-manifest"/>
		</p:input>
	</p:add-attribute>
	<p:sink/>
	<px:fileset-join>
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
			<p:pipe step="svg.fileset" port="result"/>
		</p:input>
	</px:fileset-join>
	<px:fileset-update name="update">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
			<p:pipe step="extract" port="svg"/>
		</p:input>
		<p:input port="update.fileset">
			<p:pipe step="filter-html" port="result"/>
		</p:input>
		<p:input port="update.in-memory">
			<p:pipe step="extract" port="html-with-links"/>
		</p:input>
	</px:fileset-update>

</p:declare-step>
