<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-to-epub3" version="1.0" name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="zedai-to-epub3" port="in-memory.out"/>
	</p:output>
	<p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
		<p:pipe step="zedai-to-epub3" port="validation-status"/>
	</p:output>
	<p:output port="tts-log" sequence="true">
		<p:pipe step="zedai-to-epub3" port="tts-log"/>
	</p:output>

	<p:input port="tts-config"/>

	<p:option name="language" required="true"/>
	<p:option name="validation" cx:type="off|report|abort" select="'off'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to stop processing and raise an error on validation issues (abort), only
			report them (report), or to ignore any validation issues (off).</p>
		</p:documentation>
	</p:option>
	<p:option name="dtbook-is-valid" cx:as="xs:boolean" select="true()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the input is a valid DTBook.</p>
		</p:documentation>
	</p:option>
	<p:option name="nimas" cx:as="xs:boolean" select="false()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the input is NIMAS.</p>
		</p:documentation>
	</p:option>
	<p:option name="audio" required="true" cx:as="xs:string"/>
	<p:option name="audio-file-type" select="'audio/mpeg'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The desired file type of the generated audio files, specified as a MIME type.</p>
			<p>Examples:</p>
			<ul>
				<li>"audio/mpeg"</li>
				<li>"audio/x-wav" (but note that this is not a core media type)</li>
			</ul>
		</p:documentation>
	</p:option>
	<p:option name="chunk-size" required="false" select="'-1'"/>

	<p:option name="output-name" required="true"/>
	<p:option name="output-dir" required="true"/>
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl">
		<p:documentation>
			px:css-speech-cascade
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl">
		<p:documentation>
			px:dtbook-to-zedai
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl">
		<p:documentation>
			px:zedai-to-epub3
		</p:documentation>
	</p:import>

	<!-- CSS inlining -->
	<p:choose px:progress=".1">
		<p:when test="$audio = 'true'">
			<px:css-speech-cascade content-type="application/x-dtbook+xml" name="cascade">
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
				<p:input port="config">
					<p:pipe step="main" port="tts-config"/>
				</p:input>
			</px:css-speech-cascade>
			<p:sink/>
			<p:identity>
				<p:input port="source">
					<p:pipe step="cascade" port="result.in-memory"/>
				</p:input>
			</p:identity>
		</p:when>
		<p:otherwise>
			<p:identity>
				<p:input port="source">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</p:identity>
		</p:otherwise>
	</p:choose>
	<p:identity name="dtbook-with-css"/>
	<p:sink/>
	
	<px:dtbook-to-zedai name="dtbook-to-zedai" px:message="Converting DTBook to ZedAI" px:progress="4/10">
		<p:input port="source.fileset">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="dtbook-with-css" port="result"/>
		</p:input>
		<p:with-option name="output-dir" select="concat($output-dir,'zedai/')"/>
		<p:with-option name="zedai-filename" select="concat($output-name,'.xml')"/>
		<p:with-option name="lang" select="$language"/>
		<p:with-option name="validation" select="$validation"/>
		<p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
		<!-- reporting validation issues in intermediary documents is not helpful for user -->
		<!-- FIXME: for now we even completely disabled output validation because px:dtbook-to-zedai
		     is not perfect, but the issues in the ZedAI do not necessarily result in bad HTML -->
		<p:with-option name="output-validation" select="'off'"/> <!--if ($validation='abort') then 'abort'
		                                                             else if ($validation='report' and $dtbook-is-valid) then 'abort'
		                                                             else 'off'-->
		<p:with-option name="nimas" select="$nimas"/>
	</px:dtbook-to-zedai>

	<!--TODO better handle core media type filtering-->
	<!--TODO copy/translate CSS ?-->
	<p:delete match="d:file[not(@media-type=('application/z3998-auth+xml',
	                                         'image/gif','image/jpeg','image/png',
	                                         'image/svg+xml','application/pls+xml',
	                                         'audio/mpeg','audio/mp4','text/javascript'))]"/>

	<px:zedai-to-epub3 name="zedai-to-epub3" process-css="false" px:message="Converting ZedAI to EPUB 3" px:progress="5/10">
		<p:input port="in-memory.in">
			<p:pipe step="dtbook-to-zedai" port="result.in-memory"/>
		</p:input>
		<p:input port="tts-config">
			<p:pipe step="main" port="tts-config"/>
		</p:input>
		<p:with-option name="output-dir" select="concat($temp-dir,'epub3/out/')"/>
		<p:with-option name="temp-dir" select="concat($temp-dir,'epub3/temp/')"/>
		<p:with-option name="audio" select="$audio"/>
		<p:with-option name="audio-file-type" select="$audio-file-type"/>
		<p:with-option name="chunk-size" select="$chunk-size"/>
	</px:zedai-to-epub3>
	
</p:declare-step>
