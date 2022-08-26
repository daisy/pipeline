<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:audio-transcode"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Transcode audio files.</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input fileset.</p>
		</p:documentation>
	</p:input>

	<p:option name="media-types" select="'audio/*'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Space separated list of white-listed media types. Suppports the glob characters '*'
			and '?'.</p>
			<p>Matched files are expected to be audio files and stored on disk.</p>
		</p:documentation>
	</p:option>
	<p:option name="not-media-types" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Space separated list of black-listed media types. Suppports the glob characters '*'
			and '?'.</p>
		</p:documentation>
	</p:option>
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

	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Copy of the input fileset with all the audio files that are matched by the
			"media-types" and "not-media-types" filters replaced by transcoded versions of those
			files.</p>
		</p:documentation>
		<p:pipe step="move-to-new-audio-dir" port="result"/>
	</p:output>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that contains the mapping from the original audio
			files (<code>@original-href</code>) to the transcoded versions (<code>@href</code>). The
			<code>d:file</code> elements can have an optional <code>d:clip</code> child with
			attributes <code>clipBegin</code>, <code>clipEnd</code>, <code>original-clipBegin</code>
			and <code>original-clipEnd</code>, to indicate if the audio contained in the original
			audio file starts at an offset within the transcoded audio file. The attributes values
			are positive and are expressed in seconds (with millisecond precision).</p>
		</p:documentation>
		<p:pipe step="mapping" port="result"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:tempdir
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-filter
			px:fileset-apply
			px:fileset-join
			px:fileset-copy
			px:fileset-compose
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<p:group name="transcode-files">
		<p:output port="result" primary="true"/>
		<p:output port="mapping">
			<p:pipe step="xslt" port="secondary"/>
		</p:output>

		<px:fileset-filter>
			<p:with-option name="media-types" select="$media-types"/>
			<p:with-option name="not-media-types" select="$not-media-types"/>
		</px:fileset-filter>

		<p:xslt name="xslt">
			<p:input port="stylesheet">
				<p:document href="audio-transcode.xsl"/>
			</p:input>
			<p:with-param name="new-audio-file-type" select="$new-audio-file-type"/>
			<!--
			    initially place new audio files in temporary directory
			-->
			<p:with-param name="new-audio-dir" select="xs:anyURI(string(/*))">
				<p:pipe step="temp-dir" port="result"/>
			</p:with-param>
			<p:with-param name="temp-dir" select="xs:anyURI(string(/*))">
				<p:pipe step="temp-dir" port="result"/>
			</p:with-param>
		</p:xslt>
		<p:sink/>

		<!--
		    update href, original-href and media-type in input fileset
		-->
		<px:fileset-apply name="apply-to-fileset">
			<!-- update href -->
			<p:input port="source.fileset">
				<p:pipe step="main" port="source"/>
			</p:input>
			<p:input port="mapping">
				<p:pipe step="xslt" port="secondary"/>
			</p:input>
		</px:fileset-apply>
		<p:sink/>
		<px:fileset-join>
			<!-- update original-href and media-type -->
			<!-- also normalizes hrefs -->
			<p:input port="source">
				<p:pipe step="apply-to-fileset" port="result.fileset"/>
				<p:pipe step="xslt" port="result"/>
			</p:input>
		</px:fileset-join>
	</p:group>

	<!--
	    move audio files from temporary directory to $new-audio-dir
	-->
	<p:group name="move-to-new-audio-dir">
		<p:output port="result" primary="true"/>
		<p:output port="mapping">
			<p:pipe step="move" port="mapping"/>
		</p:output>
		<p:delete>
			<p:with-option name="match"
			               select="concat(
			                         'd:file[not(resolve-uri(&quot;./&quot;,resolve-uri(@href,base-uri(.)))=(&quot;',
			                         pf:normalize-uri(string(/*)),
			                         '&quot;,&quot;',
			                         pf:normalize-uri($new-audio-dir),
			                         '&quot;))]'
			                         )">
				<p:pipe step="temp-dir" port="result"/>
			</p:with-option>
		</p:delete>
		<px:fileset-copy flatten="true" dry-run="true" name="move">
			<p:with-option name="target" select="$new-audio-dir"/>
		</px:fileset-copy>
		<p:sink/>
		<px:fileset-apply>
			<p:input port="source.fileset">
				<p:pipe step="transcode-files" port="result"/>
			</p:input>
			<p:input port="mapping">
				<p:pipe step="move" port="mapping"/>
			</p:input>
		</px:fileset-apply>
	</p:group>
	<p:sink/>

	<!--
	    composed mapping
	-->
	<px:fileset-compose name="mapping">
		<p:input port="source">
			<p:pipe step="transcode-files" port="mapping"/>
			<p:pipe step="move-to-new-audio-dir" port="mapping"/>
		</p:input>
	</px:fileset-compose>
	<p:sink/>

	<!--
	    temporary directory to store new audio files
	-->
	<p:choose>
		<p:when test="not($temp-dir='')">
			<p:in-scope-names name="vars"/>
			<p:template>
				<p:input port="template">
					<p:inline><c:result>{$temp-dir}/</c:result></p:inline>
				</p:input>
				<p:input port="parameters">
					<p:pipe step="vars" port="result"/>
				</p:input>
				<p:input port="source">
					<p:empty/>
				</p:input>
			</p:template>
		</p:when>
		<p:otherwise>
			<px:tempdir delete-on-exit="true"/>
		</p:otherwise>
	</p:choose>
	<p:identity name="temp-dir"/>
	<p:sink/>

</p:declare-step>
