<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:epub-to-daisy.script"
                name="main"
                px:input-filesets="epub2 epub3"
                px:output-filesets="daisy202 daisy3">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">EPUB to DAISY</h1>
		<p px:role="desc">Transforms an EPUB 2 or EPUB 3 publication into DAISY 2.02 and DAISY 3.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub-to-daisy/">
			Online documentation
		</a>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/epub+zip application/oebps-package+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">EPUB Publication</h2>
			<p px:role="desc" xml:space="preserve">The EPUB 2 or EPUB 3 you want to transform.

You may alternatively use the "mimetype" document if your input is a unzipped/"exploded" version of an EPUB.</p>
		</p:documentation>
	</p:option>

	<p:option name="validation" select="'off'">
		<!-- defined in ../../../../../common-options.xpl -->
	</p:option>

	<p:option name="tts" required="false" px:type="boolean" select="'default'">
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>true</value>
					<a:documentation xml:lang="en">Yes</a:documentation>
					<value>false</value>
					<a:documentation xml:lang="en">No</a:documentation>
					<value>default</value>
					<a:documentation xml:lang="en">If publication has no media overlays yet</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Perform text-to-speech</h2>
			<p px:role="desc" xml:space="preserve">Whether to use a speech synthesizer to produce media overlays.

This will remove any existing media overlays in the EPUB.</p>
		</p:documentation>
	</p:option>

	<p:input port="tts-config" primary="false">
		<!-- defined in ../../../../../common-options.xpl -->
		<p:inline><d:config/></p:inline>
	</p:input>

	<p:option xmlns:_="tts" name="_:stylesheet" select="''">
		<!-- defined in ../../../../../common-options.xpl -->
	</p:option>

	<p:option name="stylesheet-parameters" select="'()'">
		<!-- defined in ../../../../../common-options.xpl -->
	</p:option>

	<p:option name="lexicon" select="p:system-property('d:org.daisy.pipeline.tts.default-lexicon')">
		<!-- defined in ../../../../../common-options.xpl -->
	</p:option>

	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>

	<p:option name="include-tts-log" select="p:system-property('d:org.daisy.pipeline.tts.log')">
		<!-- defined in ../../../../../common-options.xpl -->
	</p:option>
	<p:output port="tts-log" sequence="true">
		<!-- defined in ../../../../../common-options.xpl -->
		<p:pipe step="status" port="tts-log"/>
	</p:output>
	<p:serialization port="tts-log" indent="true" omit-xml-declaration="false"/>

	<p:option name="epub3" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Intermediary EPUB 3 with media-overlays</h2>
			<p>Note that the conversion may fail but still output a EPUB 3 document.</p>
		</p:documentation>
	</p:option>

	<p:option name="daisy202" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 2.02</h2>
		</p:documentation>
	</p:option>

	<p:option name="daisy3" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 3</h2>
		</p:documentation>
	</p:option>

	<p:output port="validation-report" sequence="true">
		<!-- defined in ../../../../../common-options.xpl -->
		<p:pipe step="load" port="validation-report"/>
	</p:output>

	<p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
		<!-- whether or not the conversion was successful -->
		<p:pipe step="status" port="result"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-store
			px:fileset-delete
		</p:documentation>
	</p:import>
	<p:import href="epub-to-daisy.xpl">
		<p:documentation>
			px:epub-to-daisy
		</p:documentation>
	</p:import>

	<px:epub-load name="load" px:message="Loading EPUB" px:progress="1/20">
		<p:with-option name="href" select="$source"/>
		<p:with-option name="validation" select="not($validation='off')"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:epub-load>
	<p:sink/>

	<p:identity>
		<p:input port="source">
			<p:pipe step="load" port="validation-status"/>
		</p:input>
	</p:identity>
	<p:choose>
		<p:when test="/d:validation-status[@result='error']">
			<p:choose>
				<p:when test="$validation='abort'">
					<p:identity px:message="The EPUB input is invalid. See validation report for more info."
								px:message-severity="ERROR"/>
				</p:when>
				<p:otherwise>
					<p:identity px:message="The EPUB input is invalid. See validation report for more info."
								px:message-severity="WARN"/>
				</p:otherwise>
			</p:choose>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	<p:choose name="status" px:progress="19/20">
		<p:when test="/d:validation-status[@result='error'] and $validation='abort'">
			<p:output port="result" primary="true"/>
			<p:output port="tts-log" sequence="true">
				<p:empty/>
			</p:output>
			<p:identity/>
		</p:when>
		<p:otherwise>
			<p:output port="result" primary="true"/>
			<p:output port="tts-log" sequence="true">
				<p:pipe step="convert" port="tts-log"/>
			</p:output>

			<px:epub-to-daisy name="convert" px:message="Converting to DAISY" px:progress="15/19">
				<p:input port="source.fileset">
					<p:pipe step="load" port="result.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="load" port="result.in-memory"/>
				</p:input>
				<p:with-option name="epub3-output-dir" select="$epub3"/>
				<p:with-option name="daisy202-output-dir" select="$daisy202"/>
				<p:with-option name="daisy3-output-dir" select="$daisy3"/>
				<p:with-option name="tts" select="$tts"/>
				<p:input port="tts-config">
					<p:pipe step="main" port="tts-config"/>
				</p:input>
				<p:with-option name="include-tts-log" select="$include-tts-log='true'"/>
				<p:with-option name="stylesheet" xmlns:_="tts" select="string-join(
				                                                         for $s in tokenize($_:stylesheet,'\s+')[not(.='')] return
				                                                           resolve-uri($s,$source),
				                                                         ' ')"/>
				<p:with-option name="stylesheet-parameters" select="$stylesheet-parameters"/>
				<p:with-option name="lexicon" select="for $l in tokenize($lexicon,'\s+')[not(.='')] return
				                                        resolve-uri($l,$source)"/>
				<p:with-option name="temp-dir" select="$temp-dir"/>
			</px:epub-to-daisy>

			<px:fileset-store name="store-epub3" px:message="Storing intermediary EPUB 3" px:progress="1/19">
				<p:input port="fileset.in">
					<p:pipe step="convert" port="epub3.fileset"/>
				</p:input>
				<p:input port="in-memory.in">
					<p:pipe step="convert" port="epub3.in-memory"/>
				</p:input>
			</px:fileset-store>

			<p:choose name="store-daisy202" px:progress="1/19">
				<p:xpath-context>
					<p:pipe step="convert" port="daisy202.fileset"/>
				</p:xpath-context>
				<p:when test="//d:file">
					<p:output port="result">
						<p:pipe step="store" port="fileset.out"/>
					</p:output>
					<px:fileset-store name="store" px:progress="1" px:message="Storing DAISY 2.02">
						<p:input port="fileset.in">
							<p:pipe step="convert" port="daisy202.fileset"/>
						</p:input>
						<p:input port="in-memory.in">
							<p:pipe step="convert" port="daisy202.in-memory"/>
						</p:input>
					</px:fileset-store>
				</p:when>
				<p:otherwise>
					<p:output port="result"/>
					<p:identity>
						<p:input port="source">
							<p:pipe step="convert" port="daisy202.fileset"/>
						</p:input>
					</p:identity>
				</p:otherwise>
			</p:choose>

			<p:choose name="store-daisy3" px:progress="1/19">
				<p:xpath-context>
					<p:pipe step="convert" port="daisy3.fileset"/>
				</p:xpath-context>
				<p:when test="//d:file">
					<p:output port="result">
						<p:pipe step="store" port="fileset.out"/>
					</p:output>
					<px:fileset-store name="store" px:progress="1" px:message="Storing DAISY 3">
						<p:input port="fileset.in">
							<p:pipe step="convert" port="daisy3.fileset"/>
						</p:input>
						<p:input port="in-memory.in">
							<p:pipe step="convert" port="daisy3.in-memory"/>
						</p:input>
					</px:fileset-store>
				</p:when>
				<p:otherwise>
					<p:output port="result"/>
					<p:identity>
						<p:input port="source">
							<p:pipe step="convert" port="daisy3.fileset"/>
						</p:input>
					</p:identity>
				</p:otherwise>
			</p:choose>

			<p:identity cx:depends-on="store-epub3">
				<p:input port="source">
					<p:pipe step="convert" port="temp-audio-files"/>
				</p:input>
			</p:identity>
			<p:identity cx:depends-on="store-daisy3"/>
			<px:fileset-delete cx:depends-on="store-daisy202" name="delete-temp-files" px:progress="1/19"
			                   px:message="Cleaning up temporary files"/>

			<p:identity cx:depends-on="delete-temp-files">
				<p:input port="source">
					<p:inline><d:validation-status result="ok"/></p:inline>
				</p:input>
			</p:identity>
		</p:otherwise>
	</p:choose>

</p:declare-step>
