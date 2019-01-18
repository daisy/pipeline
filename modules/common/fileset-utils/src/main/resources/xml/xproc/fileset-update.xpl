<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:fileset-update" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Update a fileset with documents from memory.</p>
	</p:documentation>
	
	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input fileset</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true"/>
	
	<p:input port="update" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The documents to be added to the fileset memory, possibly replacing existing
			in-memory documents. The documents must already be part of the fileset, meaning the
			fileset manifest must have entries corresponding to their base URIs, but they don't
			necessarily have to be in memory yet. It is an error if the documents are not in the
			fileset.</p>
		</p:documentation>
	</p:input>
	
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output fileset. The memory is updated, the manifest is left unchanged.</p>
		</p:documentation>
		<p:pipe step="main" port="source.fileset"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="result" port="result"/>
	</p:output>
	
	<p:import href="fileset-filter-in-memory.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
	
	<p:count name="count-update">
		<p:input port="source">
			<p:pipe step="main" port="update"/>
		</p:input>
	</p:count>
	<p:sink/>
	
	<pxi:fileset-filter-in-memory>
		<p:input port="source.fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="update"/>
		</p:input>
	</pxi:fileset-filter-in-memory>
	<p:group>
		<p:variable name="count-update" select=".">
			<p:pipe step="count-update" port="result"/>
		</p:variable>
		<px:assert message="Trying to update a fileset with a document that is not in the manifest" error-code="XXXXX">
			<p:with-option name="test" select="$count-update=count(//d:file)"/>
		</px:assert>
	</p:group>
	<p:identity name="update.fileset"/>
	
	<pxi:fileset-filter-in-memory>
		<p:input port="source.fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</pxi:fileset-filter-in-memory>
	<px:fileset-diff>
		<p:input port="secondary">
			<p:pipe step="update.fileset" port="result"/>
		</p:input>
	</px:fileset-diff>
	<px:fileset-load>
		<!-- this will just pick documents, everything is already loaded -->
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:identity name="not-update.in-memory"/>
	
	<p:identity name="result">
		<p:input port="source">
			<p:pipe step="not-update.in-memory" port="result"/>
			<p:pipe step="main" port="update"/>
		</p:input>
	</p:identity>
	
</p:declare-step>
