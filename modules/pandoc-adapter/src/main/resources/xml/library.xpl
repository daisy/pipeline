<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
           xmlns:c="http://www.w3.org/ns/xproc-step"
           xmlns:cx="http://xmlcalabash.com/ns/extensions"
           xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:h="http://www.w3.org/1999/xhtml"
           exclude-inline-prefixes="#all">

	<p:declare-step type="px:pandoc-markdown-to-html" name="main">

		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Transform Markdown to HTML</p>
		</p:documentation>

		<p:input port="source.fileset" primary="true">
			<p:documentation xmlns="http://www.w3.org/1999/xhtml">
				<p>The input fileset. Must contain exactly one file with media type
				<code>text/markdown</code>.</p>
				<p>References resources (such as images) do not need to be in the fileset if
				they are present on disk.</p>
			</p:documentation>
		</p:input>
		<p:input port="source.in-memory" sequence="true">
			<p:empty/>
		</p:input>

		<p:option name="result-dir" px:type="anyDirURI">
			<p:documentation xmlns="http://www.w3.org/1999/xhtml">
				<p>The path to the output directory where the HTML fileset should be stored.</p>
			</p:documentation>
		</p:option>

		<p:output port="result.fileset" primary="true">
			<p:documentation xmlns="http://www.w3.org/1999/xhtml">
				<p>The fileset containing the HTML file(s) and all references resources.</p>
			</p:documentation>
		</p:output>
		<p:output port="result.in-memory" sequence="true">
			<p:pipe step="copy" port="result.in-memory"/>
		</p:output>

		<p:option name="detect-image-captions" cx:as="xs:boolean" select="false()">
			<p:documentation xmlns="http://www.w3.org/1999/xhtml">
				<p>Detect image captions</p>
			</p:documentation>
		</p:option>

		<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
			<p:documentation>
				px:assert
			</p:documentation>
		</p:import>
		<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
			<p:documentation>
				px:set-base-uri
			</p:documentation>
		</p:import>
		<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
			<p:documentation>
				px:fileset-filter
				px:fileset-load
				px:fileset-copy
				px:fileset-add-entry
			</p:documentation>
		</p:import>
		<p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
			<p:documentation>
				px:html-load
			</p:documentation>
		</p:import>

		<p:declare-step type="pxi:markdown-to-html">
			<p:option name="source">
				<p:documentation>The Markdown string</p:documentation>
			</p:option>
			<p:output port="result">
				<p:documentation>The HTML document</p:documentation>
			</p:output>
			<p:option name="detect-image-captions"/>
			<!--
			    Implemented in ../../java/org/daisy/pipeline/pandoc/calabash/impl/MarkdownToHTML.java
			-->
		</p:declare-step>

		<px:fileset-filter media-types="text/markdown text/x-markdown" name="markdown">
			<p:input port="source.in-memory">
				<p:pipe step="main" port="source.in-memory"/>
			</p:input>
		</px:fileset-filter>
		<px:fileset-load>
			<p:input port="in-memory">
				<p:pipe step="main" port="source.in-memory"/>
			</p:input>
		</px:fileset-load>
		<px:assert test-count-min="1" test-count-max="1" error-code="XXXXX"
		           message="There must be exactly one markdown file in the input"/>

		<p:group name="html">
			<p:output port="result"/>
			<p:variable name="base" select="p:base-uri(/)"/>
			<p:variable name="name" select="replace($base,'^.*/([^/]*)\.[^/\.]*$','$1')"/>

			<pxi:markdown-to-html>
				<p:with-option name="source" select="string(/c:data)"/>
				<p:with-option name="detect-image-captions" select="$detect-image-captions"/>
			</pxi:markdown-to-html>

			<p:delete match="/h:html/h:head/h:style"/>

			<px:set-base-uri>
				<p:with-option name="base-uri" select="resolve-uri(concat($name,'.html'),$base)"/>
			</px:set-base-uri>
		</p:group>
		<p:sink/>

		<px:fileset-add-entry media-type="application/xhtml+xml" name="html-and-resources">
			<p:input port="source.fileset">
				<p:pipe step="markdown" port="not-matched"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="markdown" port="not-matched.in-memory"/>
			</p:input>
			<p:input port="entry">
				<p:pipe step="html" port="result"/>
			</p:input>
		</px:fileset-add-entry>

		<px:html-load name="load">
			<p:input port="source.in-memory">
				<p:pipe step="html-and-resources" port="result.in-memory"/>
			</p:input>
		</px:html-load>

		<px:fileset-rebase>
			<p:with-option name="new-base" select="base-uri(/)">
				<p:pipe step="html" port="result"/>
			</p:with-option>
		</px:fileset-rebase>
		<px:fileset-copy name="copy">
			<p:input port="source.in-memory">
				<p:pipe step="load" port="result.in-memory"/>
			</p:input>
			<p:with-option name="target" select="$result-dir"/>
		</px:fileset-copy>
	</p:declare-step>

</p:library>
