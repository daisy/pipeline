<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:fileset-update" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Update a fileset with documents from memory.</p>
	</p:documentation>
	
	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input fileset</p>
		</p:documentation>
	</p:input>
	
	<p:input port="update.fileset"/>
	<p:input port="update.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The files to be updated in the "source" fileset. The files must already be part of
			the source fileset, meaning that the manifest must have the corresponding entries, but
			they don't necessarily have to be in memory yet. It is an error if the "update" fileset
			contains files that are not in the "source" fileset. The manifest entries replace the
			existing entries. The documents possibly replace existing documents. If a file in
			"update.fileset" has no corresponding document in "update.in-memory", a document for
			that file will not be present in "result.in-memory" either.</p>
		</p:documentation>
	</p:input>
	
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="result.in-memory" port="result"/>
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output fileset. The memory is updated and the attributes of the manifest entries
			may be changed, but the ordering of the manifest entries is unchanged.</p>
		</p:documentation>
	</p:output>
	
	<p:import href="fileset-filter-in-memory.xpl">
		<p:documentation>
			px:fileset-filter-in-memory
		</p:documentation>
	</p:import>
	<p:import href="fileset-diff.xpl">
		<p:documentation>
			px:fileset-diff
		</p:documentation>
	</p:import>
	<p:import href="fileset-load.xpl">
		<p:documentation>
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	
	<p:group>
		<!-- error when files in update that are not in source -->
		<px:fileset-diff name="diff">
			<p:input port="source">
				<p:pipe step="main" port="update.fileset"/>
			</p:input>
			<p:input port="secondary">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
		</px:fileset-diff>
		<p:sink/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="main" port="update.fileset"/>
			</p:input>
		</p:identity>
		<p:group>
			<p:variable name="file-not-in-manifest" cx:as="xs:string" select="/*/d:file[1]/resolve-uri(@href,base-uri(.))">
				<p:pipe step="diff" port="result"/>
			</p:variable>
			<px:assert message="Trying to update a fileset with a file that is not in the source manifest: $1" error-code="XXXXX">
				<p:with-option name="test" select="$file-not-in-manifest=''"/>
				<p:with-option name="param1" select="$file-not-in-manifest"/>
			</px:assert>
		</p:group>
	</p:group>
	<p:identity name="update.fileset"/>
	
	<p:group>
		<!-- all in-memory documents in update -->
		<px:fileset-filter-in-memory name="filter">
			<p:input port="source.in-memory">
				<p:pipe step="main" port="update.in-memory"/>
			</p:input>
		</px:fileset-filter-in-memory>
		<p:sink/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="filter" port="result.in-memory"/>
			</p:input>
		</p:identity>
		<!-- show error for documents not listed in update manifest -->
		<px:assert message="Trying to update a fileset with documents that are not in the update manifest: $1" error-code="XXXXX">
			<p:with-option name="test" select="not(/*/d:file)">
				<p:pipe step="filter" port="not-in-manifest"/>
			</p:with-option>
			<p:with-option name="param1" select="string-join(/*/d:file/@href,', ')">
				<p:pipe step="filter" port="not-in-manifest"/>
			</p:with-option>
		</px:assert>
	</p:group>
	<p:identity name="update.in-memory"/>
	<p:sink/>
	
	<p:group>
		<!-- all in-memory documents in source -->
		<px:fileset-filter-in-memory>
			<p:input port="source.fileset">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="main" port="source.in-memory"/>
			</p:input>
		</px:fileset-filter-in-memory>
		<!-- ... that are not in update -->
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
	</p:group>
	<p:identity name="not-update.in-memory"/>
	<p:sink/>
	
	<p:identity name="result.in-memory">
		<p:input port="source">
			<p:pipe step="not-update.in-memory" port="result"/>
			<p:pipe step="update.in-memory" port="result"/>
		</p:input>
	</p:identity>
	<p:sink/>
	
	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
			<p:pipe step="update.fileset" port="result"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="../xslt/fileset-update.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
</p:declare-step>
