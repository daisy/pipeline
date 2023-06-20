<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                type="px:daisy202-to-mp3"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Transform a DAISY 2.02 publication into a folder structure with MP3 files.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>DAISY 2.02 fileset</p>
			<p>Audio files must be stored on disk.</p>
			<p>SMIL documents are expected to be listed in spine order.</p>
		</p:documentation>
	</p:input>

	<p:option name="output-dir" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Directory where the MP3 files should be stored.</p>
		</p:documentation>
	</p:option>
	<p:option name="temp-dir" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Empty directory dedicated to this step. If set, the directory will be used to store
			audio files. If not set, the audio files will be stored in temporary directory that is
			automatically created.</p>
		</p:documentation>
	</p:option>
	<p:option name="file-limit" select="[8,20,999,999]">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An array of integers specifying the maximum number of files within each level of the
			folder structure, beginning from the top level and ending with the innermost level. The
			array must be at least one integer long (in which case the output is a flat list of
			audio files).</p>
		</p:documentation>
	</p:option>
	<p:option name="level-offset" cx:as="xs:integer" select="0">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Offset between folder level and section level.</p>
			<p>By default (offset 0) top-level folders correspond with level 1 sections.</p>
			<p>Non-negative integer.</p>
		</p:documentation>
	</p:option>

	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Result fileset listing the MP3 files</p>
		</p:documentation>
	</p:output>

	<p:output port="temp-files">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Temporary audio files. May be deleted when the result fileset is stored.</p>
		</p:documentation>
		<p:pipe step="rearrange" port="temp-files"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-purge
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/audio-common/library.xpl">
		<p:documentation>
			px:audio-rearrange
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<p:group name="ncc">
		<p:output port="result"/>
		<p:group>
			<p:output port="result" sequence="true">
				<p:pipe step="load-ncc" port="result"/>
				<p:pipe step="load-NCC" port="result"/>
			</p:output>
			<px:fileset-load href="*/ncc.html" name="load-ncc">
				<p:input port="in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-load>
			<p:sink/>
			<px:fileset-load href="*/NCC.HTML" name="load-NCC">
				<p:input port="fileset">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-load>
			<p:sink/>
		</p:group>
		<px:assert test-count-min="1" test-count-max="1" error-code="XXXX"
		           message="The input fileset must contain exactly one NCC file"/>
		<p:group name="check-multimedia-type">
			<p:variable name="type" select="/html:html/html:head/html:meta[@name='ncc:multimediaType'][1]/@content"/>
			<px:assert error-code="XXXX" message="No 'ncc:multimediaType' metadata defined in the NCC.">
				<p:with-option name="test" select="$type!=''"/>
			</px:assert>
			<px:assert error-code="XXXX" message="The input DTB type ('$1') is not supported.">
				<p:with-option name="test" select="some $t in ('audioOnly','audioNcc','audioPartText','audioFullText')
				                                   satisfies lower-case($t)=lower-case($type)"/>
				<p:with-option name="param1" select="$type"/>
			</px:assert>
		</p:group>
	</p:group>

	<px:fileset-load name="smils" media-types="application/smil+xml" cx:depends-on="ncc">
		<p:input port="fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>

	<p:xslt px:progress="1/5">
		<p:input port="source">
			<p:pipe step="ncc" port="result"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="ncc-to-audio-mapping.xsl"/>
		</p:input>
		<p:with-param port="parameters" name="smils" select="collection()">
			<p:pipe step="smils" port="result"/>
		</p:with-param>
		<p:with-param port="parameters" name="file-limit" select="$file-limit"/>
		<p:with-param port="parameters" name="level-offset" select="$level-offset"/>
		<p:with-option name="output-base-uri" select="pf:normalize-uri(concat($output-dir,'/'))"/>
	</p:xslt>
	<p:add-xml-base name="mapping"/>
	<p:sink/>

	<px:fileset-purge warn-on-missing="false">
		<p:documentation>
			Remove files that exist in memory only because currently px:audio-rearrange does not
			handle it.
		</p:documentation>
		<p:input port="source.fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
	</px:fileset-purge>
	<px:audio-rearrange name="rearrange" px:progress="4/5">
		<p:input port="desired">
			<p:pipe step="mapping" port="result"/>
		</p:input>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:audio-rearrange>

</p:declare-step>
