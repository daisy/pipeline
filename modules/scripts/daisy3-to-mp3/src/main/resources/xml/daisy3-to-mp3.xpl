<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:opf="http://openebook.org/namespaces/oeb-package/1.0/"
                type="px:daisy3-to-mp3"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Transform a DAISY 3 publication into a folder structure with MP3 files.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>DAISY 3 fileset</p>
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

	<p:group>
		<px:fileset-load media-types="application/oebps-package+xml">
			<p:input port="in-memory">
				<p:pipe step="main" port="source.in-memory"/>
			</p:input>
		</px:fileset-load>
		<px:assert test-count-min="1" test-count-max="1" error-code="XXXX"
		           message="The input fileset must contain exactly one OPF file"/>
		<p:group name="check-multimedia-type">
			<p:variable name="type" select="/opf:package/opf:metadata/opf:x-metadata
			                                /opf:meta[@name='dtb:multimediaType'][1]/@content"/>
			<px:assert error-code="XXXX" message="The input DTB type ('$1') is not supported.">
				<p:with-option name="test" select="$type=('audioOnly','audioNCX','audioPartText','audioFullText')"/>
				<p:with-option name="param1" select="$type"/>
			</px:assert>
		</p:group>
		<p:sink/>
		<p:identity cx:depends-on="check-multimedia-type">
			<p:input port="source">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
		</p:identity>
	</p:group>

	<px:fileset-load name="smils" media-types="application/smil+xml">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>
	<px:fileset-load media-types="application/x-dtbncx+xml">
		<p:input port="fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert test-count-min="1" test-count-max="1" error-code="XXXX"
	           message="The input fileset must contain exactly one NCX file"/>
	<p:xslt px:progress="1/5">
		<p:input port="stylesheet">
			<p:document href="ncx-to-audio-mapping.xsl"/>
		</p:input>
		<p:with-param port="parameters" name="smils" select="collection()">
			<p:pipe step="smils" port="result"/>
		</p:with-param>
		<p:with-option name="output-base-uri" select="pf:normalize-uri(concat($output-dir,'/'))"/>
	</p:xslt>
	<p:add-xml-base name="mapping"/>
	<p:sink/>

	<px:audio-rearrange name="rearrange" px:progress="4/5">
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="desired">
			<p:pipe step="mapping" port="result"/>
		</p:input>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:audio-rearrange>

</p:declare-step>
