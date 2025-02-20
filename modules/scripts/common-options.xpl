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
	    daisy3-to-epub3
	    dtbook-to-epub3
	    dtbook-to-html
	    dtbook-to-epub3
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
	    dtbook-to-html
	    dtbook-to-zedai
	-->
	<p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Validation reports</h2>
		</p:documentation>
	</p:output>

	<!--
		dtbook-to-epub3
		dtbook-to-html
		dtbook-to-zedai
	-->
	<p:option name="language" required="false" px:type="string" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Language code</h2>
			<p px:role="desc">Language code of the input document.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-validator
	    nimas-fileset-validator
	-->
	<p:option name="mathml-version" required="false" select="'3.0'" px:type="string">
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>3.0</value>
					<value>2.0</value>
					<!-- <value>1.01</value> -->
					<!-- <value>1.0</value> -->
				</choice>
			</px:type>
		</p:pipeinfo>
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">MathML version</h2>
			<p px:role="desc">Version of MathML in the DTBook file(s).</p>
		</p:documentation>
	</p:option>

	<!--
		dtbook-validator
		dtbook-to-epub3
		dtbook-to-html
		dtbook-to-zedai
	-->
	<p:option name="nimas" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">NIMAS input</h2>
			<p px:role="desc">Whether the input DTBook is a NIMAS 1.1-conformant XML content file.</p>
		</p:documentation>
	</p:option>

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
			<p px:role="desc" xml:space="preserve">Configuration file for text-to-speech.

[More details on the configuration file format](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/).</p>
		</p:documentation>
	</p:input>

	<!--
	    dtbook-to-daisy3
	    epub3-to-epub3
	    epub-to-daisy
	    zedai-to-epub3
	-->
	<p:option name="include-tts-log" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Enable TTS log</h2>
			<p px:role="desc" xml:space="preserve">Whether or not to make the TTS log available.

The TTS log contains a great deal of additional information that is not present in the main job log
and that is helpful for troubleshooting. Most of the log entries concern particular chunks of text
of the input document.

The default can be changed using the
[`org.daisy.pipeline.tts.log`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/#common-settings)
property.
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
			<p px:role="desc">Log file with information about text-to-speech process.</p>
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
	    daisy3-upgrader
	-->
	<p:option xmlns:_="daisy3" name="_:ensure-core-media" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Ensure core media</h2>
			<p px:role="desc" xml:space="preserve">Ensure that the output DAISY 3 uses allowed file formats only.

[Allowed
formats](https://daisy.org/activities/standards/daisy/daisy-3/z39-86-2005-r2012-specifications-for-the-digital-talking-book/#d-pacfile#para_53c)
for audio files are MP3, MPEG-4 AAC and WAVE. Audio files in other formats are transcoded to
MP3.</p>
		</p:documentation>
	</p:option>

	<!--
	    daisy3-to-daisy202
	-->
	<p:option xmlns:_="daisy202" name="_:ensure-core-media" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Ensure core media</h2>
			<p px:role="desc" xml:space="preserve">Ensure that the output DAISY 2.02 uses allowed file formats only.

[Allowed formats](https://www.daisy.org/z3986/specifications/daisy_202.html#audioformats) for audio
files are MP2, MP3 and WAVE. Audio files in other formats are transcoded to MP3.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	    epub3-to-epub3
	-->
	<p:option name="stylesheet" required="false" px:type="anyURI" select="''" px:sequence="true" px:separator=" "
	          px:media-type="text/css text/x-scss application/xslt+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Style sheets</h2>
			<p px:role="desc" xml:space="preserve">A list of CSS/Sass style sheets to take into account.

Must be a space separated list of URIs, absolute or relative to the input.

Style sheets specified through this option are called "[user style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". Style sheets can also be attached to the
source document. These are referred to as "[author style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". They can be linked (using an
['xml-stylesheet' processing instruction](https://www.w3.org/TR/xml-stylesheet) or a ['link'
element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using '[style'
attributes](https://www.w3.org/TR/css-style-attr/)). Only author styles that apply to "embossed"
media are taken into account.

All style sheets are applied at once, but the order in which they are specified has an influence on
the [cascading order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order). Author styles take
precedence over user styles.

Style sheets are interpreted according to [braille
CSS](http://braillespecs.github.io/braille-css) rules.

For info on how to use Sass (Syntactically Awesome StyleSheets) see the [Sass
manual](http://sass-lang.com/documentation/file.SASS_REFERENCE.html).</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    zedai-to-epub3
	    epub-to-daisy
	-->
	<p:option xmlns:_="tts" name="_:stylesheet" required="false" px:type="anyURI" select="''" px:sequence="true" px:separator=" "
	          px:media-type="text/css text/x-scss">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Style sheets</h2>
			<p px:role="desc" xml:space="preserve">A list of CSS style sheets to take into account.

Must be a space separated list of URIs, absolute or relative to the input.

Style sheets specified through this option are called "[user style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". Style sheets can also be attached to the
source document. These are referred to as "[author style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". They can be linked (using an
['xml-stylesheet' processing instruction](https://www.w3.org/TR/xml-stylesheet) or a ['link'
element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using '[style'
attributes](https://www.w3.org/TR/css-style-attr/)). Only author styles that apply to
"[speech](https://www.w3.org/TR/CSS2/aural.html)" media are taken into account.

All style sheets are applied at once, but the order in which they are specified has an influence on
the [cascading order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order). Author styles take
precedence over user styles.
			</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    zedai-to-epub3
	    epub-to-daisy
	    epub3-to-epub3
	-->
	<p:option name="lexicon" required="false" px:type="anyURI" select="''" px:sequence="true" px:separator=" "
	          px:media-type="application/pls+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Lexicons</h2>
			<p px:role="desc" xml:space="preserve">A list of PLS lexicons to take into account.

Must be a space separated list of URIs, absolute or relative to the input.

Lexicons can also be attached to the source document, using a ['link'
element](http://kb.daisy.org/publishing/docs/text-to-speech/pls.html#ex-07).

PLS lexicons allow you to define custom pronunciations of words. It is
meant to help TTS processors deal with ambiguous abbreviations and
pronunciation of proper names. When a word is defined in a lexicon,
the processor will use the provided pronunciation instead of the
default rendering.

The syntax of a PLS lexicon is defined in [Pronunciation Lexicon
Specification (PLS) Version
1.0](https://www.w3.org/TR/pronunciation-lexicon), extended with
regular expression matching. To enable regular expression matching,
add the "regex" attribute, as follows:

~~~xml
&lt;lexicon xmlns="http://www.w3.org/2005/01/pronunciation-lexicon" version="1.0"
         alphabet="ipa" xml:lang="en"&gt;
  &lt;lexeme regex="true"&gt;
    &lt;grapheme&gt;([0-9]+)-([0-9]+)&lt;/grapheme&gt;
    &lt;alias&gt;between $1 and $2&lt;/alias&gt;
  &lt;/lexeme&gt;
&lt;/lexicon&gt;
~~~

The regex feature works only with alias-based substitutions. The regex
syntax used is that from [XQuery 1.0 and XPath
2.0](https://www.w3.org/TR/xpath-functions/#regex-syntax).

Whether or not the regex attribute is set to "true", the grapheme
matching can be made more accurate by specifying the
"positive-lookahead" and "negative-lookahead" attributes:

~~~xml
&lt;lexicon version="1.0" xmlns="http://www.w3.org/2005/01/pronunciation-lexicon"
         alphabet="ipa" xml:lang="en"&gt;
  &lt;lexeme&gt;
    &lt;grapheme positive-lookahead="[ ]+is"&gt;SB&lt;/grapheme&gt;
    &lt;alias&gt;somebody&lt;/alias&gt;
  &lt;/lexeme&gt;
  &lt;lexeme&gt;
    &lt;grapheme&gt;SB&lt;/grapheme&gt;
    &lt;alias&gt;should be&lt;/alias&gt;
  &lt;/lexeme&gt;
  &lt;lexeme xml:lang="fr"&gt;
    &lt;grapheme positive-lookahead="[ ]+[cC]ity"&gt;boston&lt;/grapheme&gt;
    &lt;phoneme&gt;bɔstøn&lt;/phoneme&gt;
  &lt;/lexeme&gt;
&lt;/lexicon&gt;
~~~

Graphemes with "positive-lookahead" will match if the beginning of
what follows matches the "position-lookahead" pattern. Graphemes with
"negative-lookahead" will match if the beginning of what follows does
not match the "negative-lookahead" pattern. The lookaheads are
case-sensitive while the grapheme contents are not.

The lexemes are matched in this order:

1. Graphemes with regex="false" come first, no matter if there is a lookahead or not;
2. then come graphemes with regex="true" and no lookahead;
3. then graphemes with regex="true" and one or two lookaheads.

Within these categories, lexemes are matched in the same order as they
appear in the lexicons.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    dtbook-to-daisy3
	    dtbook-to-epub3
	    epub3-to-epub3
	    epub-to-daisy
	-->
	<p:option name="stylesheet-parameters" required="false" px:type="stylesheet-parameters" select="'()'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Style sheet parameters</h2>
			<p px:role="desc" xml:space="preserve">A list of parameters passed to the style sheets.

Style sheets, whether they're user style sheets (specified with the "stylesheet" option) or author
style sheets (associated with the source), may have parameters (Sass variables). The
"stylesheet-parameters" option, which takes a comma-separated list of key-value pairs enclosed in
parenthesis, can be used to set these variables.

For example, if a style sheet uses the Sass variable "foo":

~~~sass
@if $foo {
   /* some style that should only be enabled when "foo" is truthy */
}
~~~

you can control that variable with the following parameters list: `(foo:true)`.</p>
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
done using the selected braille code. If left empty, the braille code is determined by the document
language. Note that braille transcription can also be controlled through CSS `@text-transform`
rules.</p>
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
	<p:option name="result" required="true" px:output="result" px:type="anyDirURI" px:media-type="text">
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

The file format must be expressed as a list of parenthesis enclosed key-value pairs. For example, to
select a file format suited for the U.S., set the option to `(locale:en-US)`. To use the braille
character set used in the Netherlands and store to a file with extension ".brl", set the option to
`(locale:nl)(file-extension:'.brl')`.

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
	<p:option name="preview-table" required="false" px:type="preview-table" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">ASCII braille table for HTML preview</h2>
			<p px:role="desc" xml:space="preserve">The ASCII braille table used to render the HTML and PDF previews.

If left blank, the locale information in the input document will be used to select a suitable table.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	    zedai-to-pef
	-->
	<p:option name="preview" required="false" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Preview</h2>
			<p px:role="desc">An HTML preview of the braille result.</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	-->
	<p:option name="include-pdf" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include PDF</h2>
			<p px:role="desc" xml:space="preserve">Whether or not to include a PDF version of the braille result showing ASCII braille.</p>
		</p:documentation>
	</p:option>

	<!--
	    html-to-pef
	-->
	<p:option name="pdf" required="false" px:output="result" px:type="anyDirURI" px:media-type="application/pdf" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">PDF</h2>
			<p px:role="desc">A PDF version of the braille showing ASCII braille.</p>
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
	<p:option name="pef" required="false" px:output="result" px:type="anyDirURI" px:media-type="application/x-pef+xml" select="''">
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
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include OBFL</h2>
			<p px:role="desc" xml:space="preserve">Whether or not the keep the intermediary OBFL file (for debugging).</p>
		</p:documentation>
	</p:option>

	<!--
	    dtbook-to-pef
	    html-to-pef
	    epub3-to-pef
	-->
	<p:option name="obfl" required="false" px:output="result" px:type="anyDirURI" px:media-type="application/x-obfl+xml" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">OBFL</h2>
			<p px:role="desc">The intermediary OBFL file.</p>
		</p:documentation>
	</p:option>

	<!--
	    daisy202-to-mp3
	    daisy3-to-mp3
	-->
	<p:option name="folder-depth" select="'1'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Folder depth</h2>
			<p px:role="desc" xml:space="preserve">The number of folder levels in the produced folder structure.

The book is always, if possible, contained in a single top-level folder with MP3 files or
sub-folders (files for folder depth 1, sub-folders for folder depths greater than 1) that correspond
with top-level sections of the book.

If there are more top-level sections than the maximum number of files/folders that a top-level
folder can contain, the book is divided over multiple top-level folders. Similarly, if the number of
level-two sections within a top-level section exceeds the maximum number of files/folders that a
level-two folder can contain, the top-level section is divided over multiple level-two folders.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>1</value>
					<a:documentation xml:lang="en" xml:space="preserve">1

Produces a folder structure that is one level deep. At the top level there is 1 folder, that
contains entire book.  This folder can have up to 999 MP3 files. Each MP3 file corresponds with a
top-level section of the book. This setting is suited for simple devices of type Envoy Connect/Vine
C2.</a:documentation>
					<value>2</value>
					<a:documentation xml:lang="en" xml:space="preserve">2

Produces a folder structure that is two levels deep. At the top level there is 1 folder that
contains entire book. This folder can have up to 999 sub-folders, each of which can contain up to
999 MP3 files. Each MP3 file corresponds with a level-two section, a top-level section without
sub-sections, or the content within a top-level section before the first
sub-section.</a:documentation>
					<value>3</value>
					<a:documentation xml:lang="en" xml:space="preserve">3

Produces a folder structure that is three levels deep. At the top level there is 1 folder that
contains entire book. This folder can have up to 999 sub-folders. The sub-folders can have up to 999
sub-sub-folders, each of which can contain up to 999 MP3 files. Each MP3 file corresponds with a
level-three section, a top-level or level-two section without sub-sections, or the content within a
top-level or level-two section before the first sub-section.</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

</p:declare-step>
