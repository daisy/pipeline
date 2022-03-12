<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:css-to-fileset" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Create a fileset document for a set of CSS files.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input fileset containing the CSS files (marked with
			<code>media-type="text/css"</code>).</p>
			<p>Will also be used for loading other resources. If files are present in memory, they
			are expected to be <code>c:data</code> documents. Only when files are not present in
			this fileset, it will be attempted to load them from disk.</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" primary="false" sequence="true">
		<p:empty/>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset containing the CSS files and all the resources referenced from it.</p>
		</p:documentation>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
		<p:documentation>
			px:mediatype-detect
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-purge
			px:fileset-join
		</p:documentation>
	</p:import>

	<!--
	    remove files that are not on disk or in memory, and make @original-href reflect which files are in memory
	-->
	<px:fileset-purge name="purge">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-purge>

	<p:delete match="d:file[not(@media-type='text/css')]" name="css"/>

	<p:for-each>
		<p:iteration-source select="/*/d:file"/>
		<p:xslt template-name="main">
			<p:input port="source">
				<p:empty/>
			</p:input>
			<p:input port="stylesheet">
				<p:document href="css-to-fileset.xsl"/>
			</p:input>
			<p:with-param port="parameters" name="source" select="resolve-uri(/*/@href,base-uri(/))"/>
			<p:with-param port="parameters" name="context.fileset" select="/">
				<p:pipe step="purge" port="result.fileset"/>
			</p:with-param>
			<p:with-param port="parameters" name="context.in-memory" select="collection()">
				<p:pipe step="main" port="source.in-memory"/>
			</p:with-param>
		</p:xslt>
	</p:for-each>
	<p:identity name="css-and-resources"/>
	<p:sink/>

	<px:fileset-join>
		<p:input port="source">
			<p:pipe step="css" port="result"/>
			<p:pipe step="css-and-resources" port="result"/>
		</p:input>
	</px:fileset-join>

	<px:mediatype-detect/>

</p:declare-step>
