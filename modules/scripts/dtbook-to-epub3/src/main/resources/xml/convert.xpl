<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
                type="px:dtbook-to-epub3" version="1.0" name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="dtbook-to-epub3" port="in-memory"/>
	</p:output>
	<p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
		<p:pipe step="dtbook-to-epub3" port="status"/>
	</p:output>
	<p:output port="tts-log" sequence="true">
		<p:pipe step="dtbook-to-epub3" port="tts-log"/>
	</p:output>

	<p:input port="tts-config">
		<p:inline><d:config/></p:inline>
	</p:input>

	<p:option name="stylesheet" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>CSS style sheets as space separated list of absolute URIs.</p>
		</p:documentation>
	</p:option>

	<p:option name="stylesheet-parameters" cx:as="xs:string" select="'()'"/>

	<p:option name="lexicon" cx:as="xs:anyURI*" select="()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>PLS lexicons as list of absolute URIs.</p>
		</p:documentation>
	</p:option>

	<p:option name="language" required="false" cx:type="xs:string" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Language code of the input document. Only used when DTBook has no xml:lang attribute.</p>
		</p:documentation>
	</p:option>
	<p:option name="validation" cx:type="off|report|abort" select="'off'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to stop processing and raise an error on validation issues (abort), only
			report them (report), or to ignore any validation issues (off).</p>
		</p:documentation>
	</p:option>
	<p:option name="output-validation" cx:type="off|report|abort" select="$validation">
		<p:documentation>
			Determines whether to validate the EPUB output and what to do on validation errors. When
			not specified, follows the <code>validation</code> option.
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
	<p:option name="audio" select="'false'" cx:as="xs:string"/>
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
	<p:option name="xhtml-file-extension" required="false" select="'.xhtml'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>File name extension to use for XHTML documents.</p>
			<p><code>.xhtml</code> or <code>.html</code>.</p>
		</p:documentation>
	</p:option>

	<p:option name="output-dir" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Root directory of the (expanded) EPUB 3.</p>
		</p:documentation>
	</p:option>
	<p:option name="package-doc-path" required="false" select="'EPUB/package.opf'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>File path, relative to the root directory of the package document.</p>
		</p:documentation>
	</p:option>
	<p:option name="navigation-doc-path" required="false" select="'EPUB/toc.xhtml'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>File path, relative to the root directory, of the navigation document.</p>
		</p:documentation>
	</p:option>
	<p:option name="content-path" required="false" select="'EPUB/'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>File path, relative to the root directory, of the directory that will contain all
			files except <code>mimetype</code>, the files that need to be in <code>META-INF</code>,
			and the package and navigation documents which have their own setting.</p>
		</p:documentation>
	</p:option>
	<p:option name="output-name" required="true">
		<!-- used for intermediary ZedAI, which also determines HTML file names -->
	</p:option>
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-update
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
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
	<cx:import href="source-of-pagination.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:dtbook-source-of-pagination
		</p:documentation>
	</cx:import>

	<!--
		Determine source of pagination
	-->
	<px:fileset-load media-types="application/x-dtbook+xml" name="load-dtbook">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:group name="dtbook-to-epub3" px:progress="1">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="zedai-to-epub3" port="in-memory.out"/>
		</p:output>
		<p:output port="status">
			<p:pipe step="zedai-to-epub3" port="status"/>
		</p:output>
		<p:output port="tts-log" sequence="true">
			<p:pipe step="zedai-to-epub3" port="tts-log"/>
		</p:output>

		<p:variable name="source-of-pagination" cx:as="xs:string?" select="if (collection()//dtb:pagenum)
		                                                                   then pf:dtbook-source-of-pagination(collection()[1])
		                                                                   else ()"/>
		<p:sink/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
		</p:identity>

		<!-- CSS inlining -->
		<p:choose px:progress=".1">
			<p:when test="$audio = 'true'">
				<px:css-speech-cascade include-user-agent-stylesheet="true" content-type="application/x-dtbook+xml" name="cascade">
					<p:input port="source.in-memory">
						<p:pipe step="load-dtbook" port="unfiltered.in-memory"/>
					</p:input>
					<p:with-option name="user-stylesheet" select="$stylesheet"/>
					<p:with-option name="parameters" select="$stylesheet-parameters"/>
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
						<p:pipe step="load-dtbook" port="unfiltered.in-memory"/>
					</p:input>
				</p:identity>
			</p:otherwise>
		</p:choose>
		<p:identity name="dtbook-with-css"/>
		<p:sink/>

		<px:dtbook-to-zedai name="dtbook-to-zedai" px:message="Converting DTBook to ZedAI" px:progress=".2">
			<p:input port="source.fileset">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="dtbook-with-css" port="result"/>
			</p:input>
			<p:with-option name="output-dir" select="concat($temp-dir,'dtbook-to-zedai/')"/>
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

		<p:choose name="zedai">
			<p:documentation>Drop (required) dc:publisher metadata from ZedAI if missing from the
			input, since it is not required in EPUB and would otherwise be set to
			"Anonymous".</p:documentation>
			<p:xpath-context>
				<p:pipe step="load-dtbook" port="result"/>
			</p:xpath-context>
			<p:when test="collection()//dtb:head/dtb:meta[@name='dc:Publisher']">
				<p:output port="fileset" primary="true"/>
				<p:output port="in-memory" sequence="true">
					<p:pipe step="dtbook-to-zedai" port="result.in-memory"/>
				</p:output>
				<p:identity/>
			</p:when>
			<p:otherwise>
				<p:output port="fileset" primary="true"/>
				<p:output port="in-memory" sequence="true">
					<p:pipe step="update" port="result.in-memory"/>
				</p:output>
				<px:fileset-load media-types="application/z3998-auth+xml" name="load-zedai">
					<p:input port="in-memory">
						<p:pipe step="dtbook-to-zedai" port="result.in-memory"/>
					</p:input>
				</px:fileset-load>
				<p:delete match="z:head/z:meta[@property='dc:publisher']" name="zedai-without-publisher"/>
				<p:sink/>
				<px:fileset-update name="update">
					<p:input port="source.fileset">
						<p:pipe step="load-zedai" port="unfiltered.fileset"/>
					</p:input>
					<p:input port="source.in-memory">
						<p:pipe step="load-zedai" port="unfiltered.in-memory"/>
					</p:input>
					<p:input port="update.fileset">
						<p:pipe step="load-zedai" port="result.fileset"/>
					</p:input>
					<p:input port="update.in-memory">
						<p:pipe step="zedai-without-publisher" port="result"/>
					</p:input>
				</px:fileset-update>
			</p:otherwise>
		</p:choose>

		<px:zedai-to-epub3 name="zedai-to-epub3" process-css="false" px:message="Converting ZedAI to EPUB 3" px:progress=".7">
			<p:input port="in-memory.in">
				<p:pipe step="zedai" port="in-memory"/>
			</p:input>
			<p:input port="tts-config">
				<p:pipe step="main" port="tts-config"/>
			</p:input>
			<p:with-option name="output-dir" select="$output-dir"/>
			<p:with-option name="temp-dir" select="concat($temp-dir,'zedai-to-epub3/')"/>
			<p:with-option name="source-of-pagination" select="$source-of-pagination"/>
			<p:with-option name="audio" select="$audio"/>
			<p:with-option name="audio-file-type" select="$audio-file-type"/>
			<p:with-option name="lexicon" select="$lexicon"/>
			<p:with-option name="chunk-size" select="$chunk-size"/>
			<p:with-option name="output-validation" select="if ($output-validation='abort')
			                                                then 'report'
			                                                else $output-validation"/>
			<p:with-option name="xhtml-file-extension" select="$xhtml-file-extension"/>
			<p:with-option name="content-path" select="$content-path"/>
			<p:with-option name="package-doc-path" select="$package-doc-path"/>
			<p:with-option name="navigation-doc-path" select="$navigation-doc-path"/>
		</px:zedai-to-epub3>
	</p:group>

</p:declare-step>
