<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

	<!-- ================================================= -->
	<!-- INPUTS, OUTPUTS, OPTIONS USED IN MULTIPLE SCRIPTS -->
	<!-- ================================================= -->

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="temp-dir" required="false" px:output="temp" px:type="anyDirURI" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Temporary directory</h2>
			<p px:role="desc">Directory for storing temporary files during conversion.</p>
		</p:documentation>
	</p:option>

	<!--
	    epub-to-daisy
	    epub2-to-epub3
	    epub3-to-daisy202
	    epub3-to-daisy3
	    dtbook-to-zedai
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
	<p:input port="tts-config" px:media-type="application/vnd.pipeline.tts-config+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Text-to-speech configuration file</h2>
			<p px:role="desc" xml:space="preserve">Configuration file for the text-to-speech.

[More details on the configuration file format](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/).</p>
		</p:documentation>
	</p:input>

	<!--
	    dtbook-to-daisy3
	    epub3-to-epub3
	    zedai-to-epub3
	-->
	<p:option name="include-tts-log" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Enable TTS log</h2>
			<p px:role="desc" xml:space="preserve">Whether or not to make the TTS log available.

The TTS log contains a great deal of additional information that is not present in the main job log
and that is helpful for troubleshooting. Most of the log entries concern particular chunks of text
of the input document.
</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    epub3-to-epub3
	    zedai-to-epub3
	-->
	<p:output port="tts-log" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">TTS log</h2>
			<p px:role="desc" xml:space="preserve">Log file with information about text-to-speech process.

Can be enabled with the "Include TTS log" option or the
[`org.daisy.pipeline.tts.log`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/#common-settings)
property.
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

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="stylesheet" required="false" px:type="string" select="''" px:sequence="true" px:media-type="text/css application/xslt+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Style sheets</h2>
			<p px:role="desc" xml:space="preserve">A list of CSS/Sass style sheets to apply.

DEPRECATION WARNING: XSLT style sheets are also supported, but this feature might be removed in the
future. It is recommended to apply any XSLT style sheets during pre-processing of the document.

Must be a space separated list of URIs, absolute or relative to the input.

Style sheets specified through this option are called "[user style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". Style sheets can also be attached to the
source document. These are referred to as "[author style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". They can be linked (using an
['xml-stylesheet' processing instruction](https://www.w3.org/TR/xml-stylesheet) or a ['link'
element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using '[style'
attributes](https://www.w3.org/TR/css-style-attr/)).

Style sheets are applied to the document in the following way: XSLT style sheets are applied before
CSS/Sass style sheets. XSLT style sheets are applied one by one, first the user style sheets, then
the author style sheets, in the order in which they are specified.

All CSS/Sass style sheets are applied at once, but the order in which they are specified (first user
style sheets, then author style sheets) has an influence on the [cascading
order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order).

CSS/Sass style sheets are interpreted according to [braille
CSS](http://braillespecs.github.io/braille-css) rules.

For info on how to use Sass (Syntactically Awesome StyleSheets) see the [Sass
manual](http://sass-lang.com/documentation/file.SASS_REFERENCE.html).</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="stylesheet-parameters" required="false" px:type="transform-query" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Style sheet parameters</h2>
			<p px:role="desc" xml:space="preserve">A list of parameters passed to the style sheets.

Style sheets, whether they're specified with the "stylesheets" option or associated with the source,
may have parameters (variables in case of Sass). The "stylesheet-parameters" option can be used to
set these parameters.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="braille-code" px:type="liblouis-table-query" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Braille code</h2>
			<p px:role="desc" xml:space="preserve">Braille code to be used for braille transcription.

If set, [braille transcription](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/) is
done with the selected braille code. If left empty, the braille code is determined by the document
language and the "Transformer features" option.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="transform" required="false" px:type="transform-query" select="'(translator:liblouis)(formatter:dotify)'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Transformer features</h2>
			<p px:role="desc" xml:space="preserve">Features of the braille transformer.

Features of the [braille transformer](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/)
to be used for creating the paginated braille document from the CSS styled input document. Together
with the "Braille code" option this determines the transformer that is selected.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI" px:media-type="text">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Output file</h2>
			<p px:role="desc">The output braille file.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="output-file-format" required="false" px:type="transform-query" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Output file format</h2>
			<p px:role="desc" xml:space="preserve">The file format in which to store the braille result.

If left blank, the braille will be stored in PEF format.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="include-preview" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include preview</h2>
			<p px:role="desc" xml:space="preserve">Whether or not to include a HTML preview of the braille result.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="preview-table" required="false" px:type="transform-query" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">ASCII braille table for HTML preview</h2>
			<p px:role="desc" xml:space="preserve">The ASCII braille table used to render the HTML preview.

If left blank, the locale information in the input document will be used to select a suitable table.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="preview-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Preview</h2>
			<p px:role="desc">An HTML preview of the braille result.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="include-pef" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include PEF</h2>
			<p px:role="desc" xml:space="preserve">Whether or not to keep the intermediary PEF file (for debugging).</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="pef-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="application/x-pef+xml" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">PEF</h2>
			<p px:role="desc">The intermediary PEF file.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="include-obfl" required="false" px:type="boolean" select="'false'">
		<p:documentation>
			<h2 px:role="name">Include OBFL</h2>
			<p px:role="desc" xml:space="preserve">Whether or not the keep the intermediary OBFL file (for debugging).</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="obfl-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">OBFL</h2>
			<p px:role="desc">The intermediary OBFL file.</p>
		</p:documentation>
	</p:option>

</p:declare-step>
