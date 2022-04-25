<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
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
	<p:import href="fileset-add-entries.xpl"/>
	<p:import href="fileset-intersect.xpl"/>
	<p:import href="fileset-diff.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:set-base-uri
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<p:sink/>
	<px:fileset-create/>
	<px:fileset-add-entries name="fileset-from-in-memory">
		<p:input port="entries">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-add-entries>
	<p:sink/>

	<px:fileset-intersect name="result" cx:pure="true">
		<!-- px:fileset-intersect also normalizes filesets -->
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
			<p:pipe step="fileset-from-in-memory" port="result.fileset"/>
		</p:input>
	</px:fileset-intersect>
	<p:sink/>

	<p:group name="result.in-memory" cx:pure="true">
		<p:output port="result" sequence="true"/>
		<p:for-each name="source.in-memory.normalized">
			<p:iteration-source>
				<p:pipe step="main" port="source.in-memory"/>
			</p:iteration-source>
			<p:output port="result"/>
			<p:variable name="new-base" select="pf:normalize-uri(base-uri(/*))"/>
			<p:choose>
				<p:when test="$new-base!=base-uri(/)">
					<px:set-base-uri>
						<p:with-option name="base-uri" select="$new-base"/>
					</px:set-base-uri>
				</p:when>
				<p:otherwise>
					<p:identity/>
				</p:otherwise>
			</p:choose>
		</p:for-each>
		<p:sink/>
		<p:for-each>
			<p:iteration-source select="//d:file">
				<p:pipe step="result" port="result"/>
			</p:iteration-source>
			<p:output port="result" sequence="true"/>
			<p:split-sequence>
				<p:input port="source">
					<p:pipe step="source.in-memory.normalized" port="result"/>
				</p:input>
				<p:with-option name="test" select="concat('base-uri(/*)=&quot;',/*/resolve-uri(@href,base-uri(.)),'&quot;')"/>
			</p:split-sequence>
			<p:split-sequence test="position()=1"/>
		</p:for-each>
	</p:group>
	<p:sink/>

	<px:fileset-diff name="not-in-manifest" cx:pure="true">
		<!-- px:fileset-diff also normalizes filesets -->
		<p:input port="source">
			<p:pipe step="fileset-from-in-memory" port="result.fileset"/>
		</p:input>
		<p:input port="secondary">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
	</px:fileset-diff>
	<p:sink/>

	<px:fileset-diff name="not-in-memory" cx:pure="true">
		<!-- px:fileset-diff also normalizes filesets -->
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="secondary">
			<p:pipe step="fileset-from-in-memory" port="result.fileset"/>
		</p:input>
	</px:fileset-diff>
	<p:sink/>

</p:declare-step>
