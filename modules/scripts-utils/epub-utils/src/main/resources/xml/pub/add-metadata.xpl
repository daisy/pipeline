<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                type="px:epub3-add-metadata"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Inject new metadata into a EPUB package document.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The source fileset</p>
			<p>Must include the package document.</p>
		</p:documentation>
	</p:input>

	<p:input port="metadata" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A set of <a
			href="https://www.w3.org/publishing/epub3/epub-packages.html#sec-metadata-elem"><code>opf:metadata</code></a>
			or <code>opf:package</code> documents. In case of the former, a <a
			href="https://www.w3.org/publishing/epub3/epub-packages.html#sec-prefix-attr"><code>prefix</code></a>
			attribute is allowed on the root element. <code>refines</code> attributes must reference
			an element within the document itself.</p>
		</p:documentation>
	</p:input>

	<p:output port="result.fileset" primary="true">
		<p:pipe step="main" port="source.fileset"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result fileset</p>
			<p>A copy of the source fileset with the updated package document with the existing and
			new metadata merged. When the same metadata (the same property) exists in multiple input
			metadata documents, the first occurences win.</p>
		</p:documentation>
		<p:pipe step="updated-package-doc" port="result"/>
		<p:pipe step="filter-package-doc" port="not-matched.in-memory"/>
	</p:output>

	<p:option name="compatibility-mode" required="false" select="'true'" px:type="boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to be backward compatible with <a
			href="http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm">Open Package Format
			2.0.1</a>.</p>
		</p:documentation>
	</p:option>

	<p:option name="reserved-prefixes" required="false" select="'#default'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The <a
			href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-default-vocab">reserved
			prefix mappings</a> of the resulting package document. By default, prefixes that are
			used but not declared in the input are also not declared in the output.</p>
		</p:documentation>
	</p:option>

	<p:option name="log-conflicts" required="false" select="'true'"/>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-filter
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="merge-metadata.xpl">
		<p:documentation>
			pxi:merge-metadata
		</p:documentation>
	</p:import>
	<p:import href="opf3-to-opf2-metadata.xpl">
		<p:documentation>
			pxi:opf3-to-opf2-metadata
		</p:documentation>
	</p:import>

	<p:documentation>Load package document</p:documentation>
	<px:fileset-filter media-types="application/oebps-package+xml" name="filter-package-doc">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-filter>
	<px:fileset-load name="load-package-doc">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert message="There must be exactly one package document in the fileset"
	           test-count-min="1" test-count-max="1" error-code="XXXXX"
	           name="package-doc"/>

	<p:viewport match="/*/opf:metadata">
		<p:sink/>
		<pxi:merge-metadata>
			<p:input port="source">
				<!-- first occurences win -->
				<p:pipe step="main" port="metadata"/>
				<!-- We need to pass the whole package document because metadata can refine manifest
				     items. The other reason is to make sure that the resulting metadata document
				     will have no id attributes that collide with other ids in the package document -->
				<p:pipe step="package-doc" port="result"/>
			</p:input>
			<p:with-option name="reserved-prefixes" select="$reserved-prefixes"/>
			<p:with-option name="log-conflicts" select="$log-conflicts"/>
		</pxi:merge-metadata>
		<p:documentation>For compatibility with OPF 2, add a second meta element with "name" and
		"content" attributes for every meta element.</p:documentation>
		<!-- Note that all OPF 2 metadata that was already present has been
		     removed by the previous step. -->
		<p:choose>
			<p:when test="$compatibility-mode='true'">
				<pxi:opf3-to-opf2-metadata compatibility-mode="true"/>
			</p:when>
			<p:otherwise>
				<p:identity/>
			</p:otherwise>
		</p:choose>
	</p:viewport>
	<p:add-attribute match="/*" attribute-name="unique-identifier">
		<!-- this assumes there is at least one dc:identifier in source or metadata (required for valid EPUB) -->
		<!-- and we know that if there is a dc:identifier with a @refines, pxi:merge-metadata puts it first -->
		<p:with-option name="attribute-value" select="/opf:package/opf:metadata/dc:identifier[1]/@id"/>
	</p:add-attribute>
	<p:choose>
		<p:when test="/*/opf:metadata/@prefix">
			<p:add-attribute attribute-name="prefix" match="/*">
				<p:with-option name="attribute-value" select="/*/opf:metadata/@prefix"/>
			</p:add-attribute>
			<p:delete match="/*/opf:metadata/@prefix"/>
		</p:when>
		<p:otherwise>
			<p:delete match="/*/@prefix"/>
		</p:otherwise>
	</p:choose>
	<p:identity name="updated-package-doc"/>
	<p:sink/>

</p:declare-step>
