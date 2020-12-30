<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

	<!-- ================================================= -->
	<!-- INPUTS, OUTPUTS, OPTIONS USED IN MULTIPLE SCRIPTS -->
	<!-- ================================================= -->

	<!--
	    epub-to-daisy
	    epub2-to-epub3
	    epub3-to-daisy202
	    epub3-to-daisy3
	-->
	<p:option name="validation" required="false" select="'off'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Validation</h2>
			<p px:role="desc">Whether to abort on validation issues.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>off</value>
					<a:documentation xml:lang="en">No validation</a:documentation>
					<value>report</value>
					<a:documentation xml:lang="en">Report validation issues</a:documentation>
					<value>abort</value>
					<a:documentation xml:lang="en">Abort on validation issues</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<!--
	    epub-to-daisy
	    epub2-to-epub3
	    epub3-to-daisy202
	    epub3-to-daisy3
	-->
	<p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Input validation report</h2>
		</p:documentation>
	</p:output>

	<!--
	    epub-to-daisy
	    epub3-to-epub3
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    zedai-to-epub3
	-->
	<p:input port="tts-config">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Text-to-speech configuration file</h2>
			<p px:role="desc" xml:space="preserve">Configuration file for the text-to-speech.

[More details on the configuration file format](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/).</p>
		</p:documentation>
	</p:input>

	<!--
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    zedai-to-epub3
	-->
	<p:output port="tts-log" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">TTS log</h2>
			<p px:role="desc" xml:space="preserve">Log file with information about text-to-speech process.

Can be enabled or disabled with the [`org.daisy.pipeline.tts.log`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/#common-settings) property.
			</p>
		</p:documentation>
	</p:output>

	<!--
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    zedai-to-epub3
	-->
	<p:option name="audio" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Enable text-to-speech</h2>
			<p px:role="desc">Whether to use a speech synthesizer to produce audio files.</p>
		</p:documentation>
	</p:option>

	<!--
	    daisy3-to-epub3
	    dtbook-to-epub3
	    dtbook-to-html
	-->
	<p:option xmlns:_="dtbook" name="_:chunk-size" required="false" px:type="integer" select="'-1'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Chunk size</h2>
			<p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections in the DTBook become separate HTML files in the resulting EPUB, and are further
split up if they exceed the given maximum size.</p>
		</p:documentation>
	</p:option>

	<!--
	    zedai-to-epub3
	-->
	<p:option xmlns:_="zedai" name="_:chunk-size" required="false" px:type="integer" select="'-1'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Chunk size</h2>
			<p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections in the ZedAI become separate HTML files in the resulting EPUB, and are further
split up if they exceed the given maximum size.</p>
		</p:documentation>
	</p:option>

</p:declare-step>
