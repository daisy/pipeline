<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:fileset-filter-in-memory" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Remove the entries from a fileset that are not loaded into memory.</p>
	</p:documentation>
	
	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>
	<p:output port="result" primary="true">
		<p:pipe step="result" port="result"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The pruned fileset</p>
			<p>The manifest contains only the files from "source.fileset" for which a document
			exists on the "source.in-memory port", and the "result.in-memory" port contains only
			documents that are present in the manifest. No documents are loaded from disk.</p>
		</p:documentation>
		<p:pipe step="result.in-memory" port="result"/>
	</p:output>
	<p:output port="not-in-memory">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Files from "source" that are not included in "result", i.e. the files that were
			filtered out.</p>
		</p:documentation>
		<p:pipe step="not-in-memory" port="result"/>
	</p:output>
	<p:output port="not-in-manifest">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset of in-memory documents that are not contained in the input fileset.</p>
		</p:documentation>
		<p:pipe step="not-in-manifest" port="result"/>
	</p:output>
	
	<p:import href="fileset-create.xpl"/>
	<p:import href="fileset-add-entry.xpl"/>
	<p:import href="fileset-join.xpl"/>
	<p:import href="fileset-intersect.xpl"/>
	<p:import href="fileset-diff.xpl"/>
	<p:import href="fileset-load.xpl"/>
	
	<px:fileset-create name="base"/>
	
	<p:for-each>
		<p:iteration-source>
			<p:pipe step="main" port="source.in-memory"/>
		</p:iteration-source>
		<px:fileset-add-entry>
			<p:with-option name="href" select="resolve-uri(base-uri(/*))"/>
			<p:input port="source">
				<p:pipe step="base" port="result"/>
			</p:input>
		</px:fileset-add-entry>
	</p:for-each>
	<px:fileset-join name="fileset-from-in-memory"/>
	<p:sink/>
	
	<px:fileset-intersect name="result">
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
			<p:pipe step="fileset-from-in-memory" port="result"/>
		</p:input>
	</px:fileset-intersect>
	<px:fileset-load name="result.in-memory">
		<!-- this will just pick documents, everything is already loaded -->
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>
	
	<px:fileset-diff name="not-in-manifest">
		<p:input port="source">
			<p:pipe step="fileset-from-in-memory" port="result"/>
		</p:input>
		<p:input port="secondary">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
	</px:fileset-diff>
	<p:sink/>
	
	<px:fileset-diff name="not-in-memory">
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="secondary">
			<p:pipe step="fileset-from-in-memory" port="result"/>
		</p:input>
	</px:fileset-diff>
	<p:sink/>
	
</p:declare-step>
