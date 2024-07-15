<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:word-to-dtbook.script" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">Word to DTBook</h1>
		<p px:role="desc" xml:space="preserve">Transforms a Microsoft Office Word (.docx) document into a DTBook XML file.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/word-to-dtbook/">
			Online documentation
		</a>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI">
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

	<p:option name="title" select="''" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document title</h2>
		</p:documentation>
	</p:option>
	<p:option name="creator" select="''" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document author</h2>
		</p:documentation>
	</p:option>
	<p:option name="publisher" select="''" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document publisher</h2>
		</p:documentation>
	</p:option>
	<p:option name="uid" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Document identifier</h2>
			<p px:role="desc">Identifier to be added as dtb:uid metadata</p>
		</p:documentation>
	</p:option>
	<p:option name="subject" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Subject(s)</h2>
			<p px:role="desc">Subject(s) to be added as dc:Subject metadata</p>
		</p:documentation>
	</p:option>
	<p:option name="accept-revisions" select="true()" cx:as="xs:boolean">
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
	<p:option name="MasterSub" px:hidden="true" select="false()" cx:as="xs:boolean" />
	<!-- from settings  -->
	<p:option name="pagination" select="'Custom'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Pagination mode</h2>
			<p px:role="desc">Define how page numbers are computed and inserted in the result</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>Custom</value>
					<a:documentation xml:lang="en">Use numbers tagged with the style 'PageNumberDAISY' in the document</a:documentation>
					<value>Automatic</value>
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
	<p:option name="footnotes-start-value" cx:as="xs:integer" select="1">
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
	<p:option name="disableDateGeneration" cx:as="xs:boolean" select="false()" px:hidden="true" />

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

	<p:xslt template-name="main" name="convert-to-dtbook" cx:serialize="true">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="oox2Daisy.xsl"/>
		</p:input>
		<p:with-param name="InputFile" select="$source"/>
		<p:with-param name="OutputDir" select="$result"/>
		<p:with-param name="title" select="$title"/>
		<p:with-param name="creator" select="$creator"/>
		<p:with-param name="publisher" select="$publisher"/>
		<p:with-param name="uid" select="$uid"/>
		<p:with-param name="subject" select="$subject"/>
		<p:with-param name="acceptRevisions" select="$accept-revisions"/>
		<p:with-param name="version" select="$version"/>
		<p:with-param name="pagination" select="$pagination"/>
		<p:with-param name="MasterSub" select="$MasterSub"/>
		<p:with-param name="ImageSizeOption" select="$image-size"/>
		<p:with-param name="DPI" select="$dpi"/>
		<p:with-param name="CharacterStyles" select="$character-styles"/>
		<p:with-param name="FootnotesPosition" select="$footnotes-position"/>
		<p:with-param name="FootnotesLevel" select="$footnotes-level"/>
		<p:with-param name="FootnotesNumbering" select="$footnotes-numbering"/>
		<p:with-param name="FootnotesStartValue" select="$footnotes-start-value"/>
		<p:with-param name="FootnotesNumberingPrefix" select="$footnotes-numbering-prefix"/>
		<p:with-param name="FootnotesNumberingSuffix" select="$footnotes-numbering-suffix"/>
		<p:with-param name="disableDateGeneration" select="$disableDateGeneration"/>
		<p:with-param name="extractShapes" select="$extract-shapes"/>
	</p:xslt>
	<p:store name="store-xml">
		<p:with-option name="href" select="concat(
		$result,
		replace(replace($source,'^.*/([^/]*?)(\.[^/\.]*)?$','$1.xml'),',','_')
	)"/>
	</p:store>


</p:declare-step>
