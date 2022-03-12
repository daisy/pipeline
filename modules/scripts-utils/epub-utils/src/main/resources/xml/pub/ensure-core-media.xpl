<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:epub3-ensure-core-media"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Ensure that a EPUB publication contains only resources that are <a
		href="https://www.w3.org/publishing/epub3/epub-spec.html#sec-core-media-types">EPUB 3 core
		media types</a>.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input EPUB publication</p>
		</p:documentation>
	</p:input>
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The resulting EPUB publication</p>
			<p>Resources that are not core media types are filtered out. The package document is
			updated accordingly. References from HTML documents to the unsupported resources are
			fixed.</p>
			<p>Also fixes dead links in HTML documents.</p>
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="clean-html" port="in-memory"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-join
			px:fileset-diff
			px:fileset-intersect
			px:fileset-update
		</p:documentation>
	</p:import>
	<p:import href="../ocf/opf-manifest-to-fileset.xpl">
		<p:documentation>
			pxi:opf-manifest-to-fileset
		</p:documentation>
	</p:import>

	<!--
	    see https://www.w3.org/publishing/epub3/epub-spec.html#sec-core-media-types
	-->
	<p:variable name="core-media-types" select="'application/font-sfnt
	                                             application/font-woff
	                                             application/javascript
	                                             application/pls+xml
	                                             application/smil+xml
	                                             application/vnd.ms-opentype
	                                             application/x-dtbncx+xml
	                                             application/xhtml+xml
	                                             audio/mp4
	                                             audio/mpeg
	                                             font/otf
	                                             font/ttf
	                                             font/woff
	                                             font/woff2
	                                             image/gif
	                                             image/jpeg
	                                             image/png
	                                             image/svg+xml
	                                             text/css
	                                             text/javascript'"/>

	<p:documentation>
		Filter the fileset
	</p:documentation>
	<px:fileset-load media-types="application/oebps-package+xml" name="opf">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:choose name="filter-resources">
		<p:xpath-context>
			<p:pipe step="opf" port="result.fileset"/>
		</p:xpath-context>
		<p:when test="//d:file">
			<!--
			    if there are package documents, filter only the resources referenced from them
			-->
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="main" port="source.in-memory"/>
			</p:output>
			<p:for-each>
				<pxi:opf-manifest-to-fileset/>
			</p:for-each>
			<px:fileset-join name="in-manifest"/>
			<p:sink/>
			<px:fileset-intersect>
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
					<p:pipe step="in-manifest" port="result"/>
				</p:input>
			</px:fileset-intersect>
			<px:fileset-filter name="in-manifest-filtered">
				<p:with-option name="media-types"
				               select="string-join(($core-media-types,'application/oebps-package+xml'),' ')"/>
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-filter>
			<p:sink/>
			<px:fileset-diff name="not-in-manifest">
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="secondary">
					<p:pipe step="in-manifest" port="result"/>
				</p:input>
			</px:fileset-diff>
			<p:sink/>
			<px:fileset-join name="filtered-unsorted">
				<p:input port="source">
					<p:pipe step="in-manifest-filtered" port="result"/>
					<p:pipe step="not-in-manifest" port="result"/>
				</p:input>
			</px:fileset-join>
			<p:sink/>
			<px:fileset-intersect>
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
					<p:pipe step="filtered-unsorted" port="result"/>
				</p:input>
			</px:fileset-intersect>
		</p:when>
		<p:otherwise>
			<!--
			    otherwise filter all resources
			-->
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="filter" port="result.in-memory"/>
			</p:output>
			<p:sink/>
			<px:fileset-filter name="filter">
				<p:with-option name="media-types" select="$core-media-types"/>
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-filter>
		</p:otherwise>
	</p:choose>

	<p:documentation>
		Remove OPF items that are not in the fileset
	</p:documentation>
	<p:group name="clean-package-doc">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<p:sink/>
		<p:for-each name="docs">
			<p:iteration-source>
				<p:pipe step="opf" port="result"/>
			</p:iteration-source>
			<p:output port="result"/>
			<p:xslt>
				<p:input port="source">
					<p:pipe step="docs" port="current"/>
					<p:pipe step="filter-resources" port="fileset"/>
				</p:input>
				<p:input port="stylesheet">
					<p:document href="clean-package-doc.xsl"/>
				</p:input>
				<p:input port="parameters">
					<p:empty/>
				</p:input>
			</p:xslt>
		</p:for-each>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="filter-resources" port="fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="filter-resources" port="in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="opf" port="result.fileset"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="docs" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

	<p:documentation>
		Clean references in content documents
	</p:documentation>
	<p:group name="clean-html">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<px:fileset-load media-types="application/xhtml+xml" name="load">
			<p:input port="in-memory">
				<p:pipe step="clean-package-doc" port="in-memory"/>
			</p:input>
		</px:fileset-load>
		<p:for-each name="docs">
			<p:output port="result"/>
			<p:sink/>
			<p:xslt>
				<p:input port="source">
					<p:pipe step="docs" port="current"/>
					<p:pipe step="clean-package-doc" port="fileset"/>
				</p:input>
				<p:input port="stylesheet">
					<p:document href="html-clean-resources.xsl"/>
				</p:input>
				<p:input port="parameters">
					<p:empty/>
				</p:input>
			</p:xslt>
		</p:for-each>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="clean-package-doc" port="fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="clean-package-doc" port="in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="load" port="result.fileset"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="docs" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

</p:declare-step>
