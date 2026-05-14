<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:daisy202-audio-transcode"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Transcode audio files in DAISY 2.02 publication.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input DAISY 2.02 fileset.</p>
		</p:documentation>
	</p:input>

	<p:option name="new-audio-file-type" required="true"> <!-- cx:type="audio/mpeg|audio|audio/x-wav"
	                                                           xmlns:cx="http://xmlcalabash.com/ns/extensions" -->
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The desired file type of the transcoded audio files, specified as a MIME type.</p>
		</p:documentation>
	</p:option>
	<p:option name="new-audio-dir" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>URI of the folder within the output fileset that should contain the transcoded audio
			files.</p>
			<p>If not specified or empty, will use the deepest common directory that contains all
			the matched files.</p>
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
			<p>Copy of the input fileset with all the not officially unsupported audio files
			replaced by transcoded versions of those files. NCC and SMIL files are updated
			accordingly.</p>
		</p:documentation>
		<p:pipe step="maybe-skip" port="in-memory"/>
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
			px:daisy202-update-links
		</p:documentation>
	</p:import>

	<!--
	    FIXME: More is needed to ensure that audio files are supported. We don't process files with
	    media types "audio/mpeg" and "audio/x-wav", but we don't check that they are actually MP2
	    (MPEG-1/2 layer II), MP3 (MPEG-1/2 layer III), WAVE (linear PCM RIFF WAVE) or ADPCM2. We
	    also don't check that they have a constant bit rate, and that their number of channels, bit
	    depths and sample frequencies are within the accepted ranges. Note that it is also possible
	    that audio files are already valid but have the wrong file extension or media-type attribute
	    (e.g. audio/wav, audio/mp2 or audio/mp3). In this case the files will still be transcoded,
	    even though it might be a no-op.
	-->

	<!--
	    FIXME: The ADPCM2 format is currently not recognized.
	-->

	<px:audio-transcode name="transcode" not-media-types="audio/mpeg
	                                                      audio/x-wav">
		<p:with-option name="new-audio-file-type" select="$new-audio-file-type"/>
		<p:with-option name="new-audio-dir" select="$new-audio-dir"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:audio-transcode>

	<p:choose name="maybe-skip">
		<!--
		    bypass everything else if no audio files were transcoded
		-->
		<p:xpath-context>
			<p:pipe step="transcode" port="mapping"/>
		</p:xpath-context>
		<p:when test="not(//d:file)">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="main" port="source.in-memory"/>
			</p:output>
			<p:identity/>
		</p:when>
		<p:otherwise>
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="update-links" port="result.in-memory"/>
			</p:output>
			<px:daisy202-update-links name="update-links">
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
				<p:input port="mapping">
					<p:pipe step="transcode" port="mapping"/>
				</p:input>
			</px:daisy202-update-links>
		</p:otherwise>
	</p:choose>

</p:declare-step>
