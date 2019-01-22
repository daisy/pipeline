<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
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

	<p:input port="tts-config"/>

	<p:option name="language" required="true"/>
	<p:option name="assert-valid" required="true"/>
	<p:option name="audio" required="true"/>
	<p:option name="chunk-size" required="false" select="'-1'"/>

	<p:option name="output-name" required="true"/>
	<p:option name="output-dir" required="true"/>
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl"/>

	<!-- CSS inlining -->
	<p:choose>
		<p:xpath-context>
			<p:empty/>
		</p:xpath-context>
		<p:when test="$audio = 'true'">
			
			<!-- first load DTBook into memory -->
			<px:fileset-load media-types="application/x-dtbook+xml">
				<p:input port="in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-load>
			
			<!-- process -->
			<px:inline-css-speech content-type="application/x-dtbook+xml" name="processed-xml">
				<p:input port="fileset.in">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="config">
					<p:pipe step="main" port="tts-config"/>
				</p:input>
			</px:inline-css-speech>
			<p:sink/>
			
			<!-- combine with other documents from fileset -->
			<px:fileset-update name="updated">
				<p:input port="source.fileset">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
				<p:input port="update">
					<p:pipe step="processed-xml" port="result"/>
				</p:input>
			</px:fileset-update>
			<p:sink/>
			<p:identity>
				<p:input port="source">
					<p:pipe step="updated" port="result.in-memory"/>
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
	
	<px:dtbook-to-zedai name="dtbook-to-zedai">
		<p:input port="fileset.in">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
		<p:input port="in-memory.in">
			<p:pipe step="dtbook-with-css" port="result"/>
		</p:input>
		<p:with-option name="opt-output-dir" select="concat($output-dir,'zedai/')"/>
		<p:with-option name="opt-zedai-filename" select="concat($output-name,'.xml')"/>
		<p:with-option name="opt-lang" select="$language"/>
		<p:with-option name="opt-assert-valid" select="$assert-valid"/>
	</px:dtbook-to-zedai>

	<!--TODO better handle core media type filtering-->
	<!--TODO copy/translate CSS ?-->
	<p:delete match="d:file[not(@media-type=('application/z3998-auth+xml',
	                                         'image/gif','image/jpeg','image/png',
	                                         'image/svg+xml','application/pls+xml',
	                                         'audio/mpeg','audio/mp4','text/javascript'))]"/>

	<px:zedai-to-epub3 name="zedai-to-epub3" process-css="false">
		<p:input port="in-memory.in">
			<p:pipe step="dtbook-to-zedai" port="in-memory.out"/>
		</p:input>
		<p:input port="tts-config">
			<p:pipe step="main" port="tts-config"/>
		</p:input>
		<p:with-option name="output-dir" select="concat($temp-dir,'epub3/out/')"/>
		<p:with-option name="temp-dir" select="concat($temp-dir,'epub3/temp/')"/>
		<p:with-option name="audio" select="$audio"/>
		<p:with-option name="chunk-size" select="$chunk-size"/>
	</px:zedai-to-epub3>
	
</p:declare-step>
