<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:word-to-dtbook.script" name="main"
                px:input-filesets="docx"
                px:output-filesets="dtbook">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">Word to DTBook</h1>
		<p px:role="desc" xml:space="preserve">Transforms a Microsoft Office Word (.docx) document into a DTBook XML file.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/word-to-dtbook/">
			Online documentation
		</a>
		<dl px:role="author">
			<dt>Name:</dt>
			<dd px:role="name">Nicolas Pavie</dd>
			<dt>E-mail:</dt>
			<dd><a px:role="contact" href="mailto:pavie.nicolas@gmail.com">pavie.nicolas@gmail.com</a></dd>
			<dt>Organisation:</dt>
			<dd px:role="organization">DAISY Consortium</dd>
		</dl>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document">
		<p:documentation>
			<h2 px:role="name">Input Docx file</h2>
			<p px:role="desc" xml:space="preserve">The document you want to convert.</p>
		</p:documentation>
	</p:option>
	<p:option name="result" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation>
			<h2 px:role="name">DTBook output</h2>
			<p px:role="desc" xml:space="preserve">Output folder of the conversion to DTBook XML</p>
		</p:documentation>
	</p:option>

	<p:option name="title" select="''" required="false" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document title</h2>
		</p:documentation>
	</p:option>
	<p:option name="creator" select="''" required="false" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document author</h2>
		</p:documentation>
	</p:option>
	<p:option name="publisher" select="''" required="false" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document publisher</h2>
			<p px:role="desc">Publisher metadata (dc:Publisher) to be added</p>
		</p:documentation>
	</p:option>
	<p:option name="uid" select="''" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document identifier</h2>
			<p px:role="desc">Identifier to be added as dtb:uid metadata</p>
		</p:documentation>
	</p:option>
	<p:option name="subject" select="''" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Subject(s)</h2>
			<p px:role="desc">Subject(s) to be added as dc:Subject metadata</p>
		</p:documentation>
	</p:option>
	<p:option name="accept-revisions" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Accept revisions</h2>
			<p px:role="desc">If the document has revisions that are not accepted, consider them as accepted for the conversion.</p>
		</p:documentation>
	</p:option>
	<p:option name="version" select="'14'" px:hidden="true"/>

	<!-- discarding math type equations preprocessing
	<p:option name="MathML" select="map{'wdTextFrameStory':[],
	                                    'wdFootnotesStory':[],
	                                    'wdMainTextStory':[]
	                                    }" />-->
	<!-- cx:as="map(xs:string,xs:string*)" -->
	<p:option name="MasterSub" px:hidden="true" select="false()" cx:as="xs:boolean"/>
	<!-- from settings -->
	<p:option name="pagination" select="'custom'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Pagination mode</h2>
			<p px:role="desc">Define how page numbers are computed and inserted in the result</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>custom</value>
					<a:documentation xml:lang="en">Use numbers tagged with the style 'PageNumberDAISY' in the document</a:documentation>
					<value>automatic</value>
					<a:documentation xml:lang="en">Use Word page breaks to compute and insert page numbers in content</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>
	<p:option name="image-size" select="'original'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Image resizing</h2>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>original</value>
					<a:documentation xml:lang="en">Keep image size</a:documentation>
					<value>resize</value>
					<a:documentation xml:lang="en">Resize images</a:documentation>
					<value>resample</value>
					<a:documentation xml:lang="en">Resample images</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>
	<p:option name="dpi" select="96" cx:as="xs:integer">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Image resampling value</h2>
			<p px:role="desc">Image resampling targeted resolution in dpi (dot-per-inch)</p>
		</p:documentation>
	</p:option>
	<p:option name="character-styles" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Translate character styles</h2>
		</p:documentation>
	</p:option>
	<p:option name="footnotes-position" select="'end'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes position</h2>
			<p px:role="desc">Footnotes position in content</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>inline</value>
					<a:documentation xml:lang="en">Inline note in content (after the paragraph containing its first reference)</a:documentation>
					<value>end</value>
					<a:documentation xml:lang="en">Put notes at the end of a level defined in footnotes insertion level</a:documentation>
					<value>page</value>
					<a:documentation xml:lang="en">Put the notes near the page break</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>
	<p:option name="footnotes-level" select="0" cx:as="xs:integer">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes insertion level</h2>
			<p px:role="desc">Lowest level into which notes are inserted in content.
			0 means the footnotes will be inserted as close as possible of its first call.</p>
		</p:documentation>
	</p:option>
	<p:option name="footnotes-numbering" cx:as="xs:string" select="'none'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes numbering</h2>
			<p px:role="desc">Customize footnotes numbering</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>none</value>
					<a:documentation xml:lang="en">Disable note numbering</a:documentation>
					<value>word</value>
					<a:documentation xml:lang="en">Use original word numbering</a:documentation>
					<value>number</value>
					<a:documentation xml:lang="en">Use custom numbering, starting from the footnotes start value</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>
	<p:option name="footnotes-start-value" cx:as="xs:integer" select="1" px:reusable="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes starting value</h2>
			<p px:role="desc">If footnotes numbering is required, start the notes numbering process from this value</p>
		</p:documentation>
	</p:option>
	<p:option name="footnotes-numbering-prefix" select="''"> <!-- cx:as="xs:string?" -->
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes number prefix</h2>
			<p px:role="desc">Add a prefix before the note's number if numbering is requested.</p>
		</p:documentation>
	</p:option>
	<p:option name="footnotes-numbering-suffix" select="''"> <!-- cx:as="xs:string?" -->
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Footnotes number suffix</h2>
			<p px:role="desc">Add a text between the note's number and the note's content.</p>
		</p:documentation>
	</p:option>

	<!-- hidden option for tests -->
	<p:option name="disableDateGeneration" cx:as="xs:boolean" select="false()" px:hidden="true"/>
	<p:option name="disableGeneratorGeneration" cx:as="xs:boolean" select="false()" px:hidden="true"/>

	<!-- hidden option to allow saveasdaisy to deactivate shapes extraction
	     This is to avoid word being blocked by one or more dialog managed by the addin.
	 -->
	<p:option name="extract-shapes" cx:as="xs:boolean" select="false()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Extract vector shapes (Experimental)</h2>
			<p px:role="desc">Try to export inline shapes like diagrams or charts during conversion using Microsoft Word.
				If deactivated, those shapes will be replaced by their name and description in the result.

				Proceed with caution : Word must not be blocked by any dialog bound to it, or the process might crash or get stuck indefinitely</p>
		</p:documentation>
	</p:option>

	<!--
	    Options for to DTBook cleanup (options that are not exposed in the Word add-in are marked hidden)
	-->

	<p:option name="repair" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Repair the DTBook</h2>
			<p px:role="desc" xml:space="preserve">Apply repair routines on the DTBook.</p>
		</p:documentation>
	</p:option>
	<p:option name="tidy" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Tidy up the DTBook</h2>
			<p px:role="desc" xml:space="preserve">Apply tidying routines on the DTBook.</p>
		</p:documentation>
	</p:option>
	<p:option name="simplifyHeadingLayout" select="false()" cx:as="xs:boolean" px:hidden="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Tidy - Simplify headings layout</h2>
			<p px:role="desc" xml:space="preserve">Simplify the level structure

Redundant level structure is sometimes used to mimic the original layout, but can pose a problem in
some circumstances. By selecting this option the script simplifies the level structure by removing
redundant levels (subordinate levels will be moved upwards). Note that the headings of the affected
levels will also change, which will alter the appearance of the layout.</p>
		</p:documentation>
	</p:option>
	<p:option name="externalizeWhitespace" select="false()" cx:as="xs:boolean" px:hidden="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Tidy - Externalize whitespaces</h2>
			<p px:role="desc" xml:space="preserve">Externalize leading and trailing whitespace

from em, strong, sub, sup, pagenum, noteref.</p>
		</p:documentation>
	</p:option>
	<p:option name="documentLanguage" select="''" px:hidden="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Tidy - Document language</h2>
			<p px:role="desc">Set a document language</p>
		</p:documentation>
	</p:option>
	<p:option name="narrator" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Cleanup the document for audio synthesis</h2>
			<p px:role="desc" xml:space="preserve">Apply cleaning routines on the document to prepare it for text-to-speech processes.</p>
		</p:documentation>
	</p:option>
	<p:option name="ApplySentenceDetection" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Apply sentences detection</h2>
			<p px:role="desc">Encapsulate sentences within the document</p>
		</p:documentation>
	</p:option>
	<p:option name="WithDoctype" select="true()" cx:as="xs:boolean" px:hidden="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include doctype in resulting DTBook(s)</h2>
			<p px:role="desc" xml:space="preserve">Include doctype in resulting DTBook(s)

The resulting DTBook will have a standard DTBook 2005-3 doctype, optionally with MathML declaration
if MathML is present in the document.</p>
		</p:documentation>
	</p:option>

	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-store
		</p:documentation>
	</p:import>
	<p:import href="word-to-dtbook.xpl">
		<p:documentation>
			px:word-to-dtbook
		</p:documentation>
	</p:import>

	<px:word-to-dtbook name="convert" px:progress="1">
		<p:with-option name="source" select="$source"/>
		<p:with-option name="output-dir" select="$result"/>
		<p:with-option name="title" select="$title"/>
		<p:with-option name="creator" select="$creator"/>
		<p:with-option name="publisher" select="$publisher"/>
		<p:with-option name="uid" select="$uid"/>
		<p:with-option name="subject" select="$subject"/>
		<p:with-option name="accept-revisions" select="$accept-revisions"/>
		<p:with-option name="version" select="$version"/>
		<p:with-option name="MasterSub" select="$MasterSub"/>
		<p:with-option name="pagination" select="$pagination"/>
		<p:with-option name="image-size" select="$image-size"/>
		<p:with-option name="dpi" select="$dpi"/>
		<p:with-option name="character-styles" select="$character-styles"/>
		<p:with-option name="footnotes-position" select="$footnotes-position"/>
		<p:with-option name="footnotes-level" select="$footnotes-level"/>
		<p:with-option name="footnotes-numbering" select="$footnotes-numbering"/>
		<p:with-option name="footnotes-start-value" select="$footnotes-start-value"/>
		<p:with-option name="footnotes-numbering-prefix" select="$footnotes-numbering-prefix"/>
		<p:with-option name="footnotes-numbering-suffix" select="$footnotes-numbering-suffix"/>
		<p:with-option name="disableDateGeneration" select="$disableDateGeneration"/>
		<p:with-option name="disableGeneratorGeneration" select="$disableGeneratorGeneration"/>
		<p:with-option name="extract-shapes" select="$extract-shapes"/>
		<p:with-option name="repair" select="$repair"/>
		<p:with-option name="tidy" select="$tidy"/>
		<p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout"/>
		<p:with-option name="externalizeWhitespace" select="$externalizeWhitespace"/>
		<p:with-option name="documentLanguage" select="$documentLanguage"/>
		<p:with-option name="narrator" select="$narrator"/>
		<p:with-option name="ApplySentenceDetection" select="$ApplySentenceDetection"/>
		<p:with-option name="WithDoctype" select="$WithDoctype"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:word-to-dtbook>

	<px:fileset-store>
		<p:input port="in-memory.in">
			<p:pipe step="convert" port="result.in-memory"/>
		</p:input>
	</px:fileset-store>

</p:declare-step>
