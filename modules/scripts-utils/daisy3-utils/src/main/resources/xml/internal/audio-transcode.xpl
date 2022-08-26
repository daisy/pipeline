<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:daisy3-audio-transcode"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Transcode audio files in DAISY 3 publication.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input DAISY 3 fileset.</p>
		</p:documentation>
	</p:input>

	<p:option name="new-audio-file-type" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The desired file type of the transcoded audio files, specified as a MIME type.</p>
		</p:documentation>
	</p:option>
	<p:option name="new-audio-dir" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>URI of the folder within the output fileset that should contain the transcoded audio
			files.</p>
			<p>The actual files will be stored in a temporary location.</p>
		</p:documentation>
	</p:option>
	<p:option name="temp-dir" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>If not empty, this directory will be used to store audio files. The directory must
			not exist yet.</p>
		</p:documentation>
	</p:option>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Copy of the input fileset with all the audio files that are matched by the
			"media-types" and "not-media-types" filters replaced by transcoded versions of those
			files.</p>
		</p:documentation>
		<p:pipe step="update-opf" port="in-memory"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/audio-common/library.xpl">
		<p:documentation>
			px:audio-transcode
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-filter
			px:fileset-load
			px:fileset-update
		</p:documentation>
	</p:import>
	<p:import href="update-links.xpl">
		<p:documentation>
			px:daisy3-update-links
		</p:documentation>
	</p:import>

	<px:audio-transcode name="transcode">
		<p:with-option name="new-audio-file-type" select="$new-audio-file-type"/>
		<p:with-option name="new-audio-dir" select="$new-audio-dir"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:audio-transcode>

	<px:daisy3-update-links name="update-links">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:input port="mapping">
			<p:pipe step="transcode" port="mapping"/>
		</p:input>
	</px:daisy3-update-links>

	<p:group name="update-opf">
		<p:documentation>Update metadata and media-type in OPF document</p:documentation>
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<px:fileset-filter media-types="application/oebps-package+xml" name="opf">
			<p:input port="source.in-memory">
				<p:pipe step="update-links" port="result.in-memory"/>
			</p:input>
		</px:fileset-filter>
		<px:fileset-load>
			<p:input port="in-memory">
				<p:pipe step="update-links" port="result.in-memory"/>
			</p:input>
		</px:fileset-load>
		<p:for-each name="opf-updated">
			<p:output port="result"/>
			<p:xslt>
				<p:input port="stylesheet">
					<p:document href="audio-transcode-update-opf.xsl"/>
				</p:input>
				<p:with-param port="parameters" name="new-audio-file-type" select="$new-audio-file-type"/>
				<p:with-param port="parameters" name="mapping" select="/*">
					<p:pipe step="transcode" port="mapping"/>
				</p:with-param>
			</p:xslt>
		</p:for-each>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="update-links" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="update-links" port="result.in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="opf" port="result"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="opf-updated" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

</p:declare-step>
