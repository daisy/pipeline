<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:daisy202-fix-audio-file-order" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Rename audio files in a DAISY 2.02 publication in such a way that when sorted
		alphabetically, they are also sorted according to the reading order.</p>
		<p>Note that within the flow of a document audio files are not necessarily played in a
		continuous manner. "Sorted according to reading order" therefore means that the position of
		an audio file is determined by the position of its first clip in the flow.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input fileset</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that contains the mapping from original audio files
			(<code>@original-href</code>) to renamed audio files (<code>@href</code>).</p>
		</p:documentation>
		<p:pipe step="mapping" port="result"/>
	</p:output>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output fileset</p>
			<p>The files are renamed in the fileset manifest and the base URIs of the in-memory
			documents are updated accordingly. Cross-references in HTML (including NCC) and SMIL
			documents are updated too.</p>
		</p:documentation>
		<p:pipe step="result" port="result.in-memory"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-intersect
		</p:documentation>
	</p:import>
	<p:import href="rename-files.xpl">
		<p:documentation>
			px:daisy202-rename-files
		</p:documentation>
	</p:import>

	<px:fileset-load href="*/ncc.html">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert message="Input fileset must contain exactly one ncc.html"
	           test-count-min="1" test-count-max="1" name="ncc"/>

	<p:xslt name="smils-in-reading-order">
		<p:input port="stylesheet">
			<p:document href="smils-in-reading-order.xsl"/>
		</p:input>
		<p:with-option name="output-base-uri" select="resolve-uri('./',base-uri(/))"/>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

	<px:fileset-intersect>
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
			<p:pipe step="smils-in-reading-order" port="result"/>
		</p:input>
	</px:fileset-intersect>

	<px:fileset-load media-types="application/smil+xml">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>

	<p:xslt name="audio-files-in-reading-order">
		<p:input port="stylesheet">
			<p:document href="audio-files-in-reading-order.xsl"/>
		</p:input>
		<p:with-option name="output-base-uri" select="resolve-uri('./',base-uri(/))"/>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

	<px:fileset-intersect>
		<p:input port="source">
			<p:pipe step="audio-files-in-reading-order" port="result"/>
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
	</px:fileset-intersect>

	<p:label-elements match="*[@href]" attribute="original-href" label="resolve-uri(@href,base-uri(.))"/>
	<p:label-elements match="*[@original-href]" attribute="href"
	                  label="concat(format-number(count(preceding-sibling::*) + 1,'0000'),
	                                replace(@original-href,'^.*(\.[^\.]+)$','$1'))"/>
	<p:identity name="mapping"/>
	<p:sink/>

	<px:daisy202-rename-files name="result">
		<p:input port="source.fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:input port="mapping">
			<p:pipe step="mapping" port="result"/>
		</p:input>
	</px:daisy202-rename-files>

</p:declare-step>
