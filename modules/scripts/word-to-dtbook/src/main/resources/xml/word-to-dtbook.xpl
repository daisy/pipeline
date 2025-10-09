<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
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
			<p px:role="desc">Publisher metadata (dc:Publisher) to be added</p>
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
	<p:option name="disableDateGeneration" cx:as="xs:boolean" select="false()" px:hidden="true"/>

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

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
			px:log-error
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-add-entry
			px:fileset-copy
			px:fileset-filter
			px:fileset-store
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
		<p:documentation>
			px:dtbook-break-detect
			px:dtbook-unwrap-words
			px:dtbook-upgrade
			px:dtbook-load
		</p:documentation>
	</p:import>
	<p:import href="fix-dtbook/fix-dtbook.xpl">
		<p:documentation>
			pxi:dtbook-fix
		</p:documentation>
	</p:import>
	<p:import href="fix-dtbook/doctyping.xpl">
		<p:documentation>
			pxi:dtbook-doctyping
		</p:documentation>
	</p:import>

	<p:xslt template-name="main" cx:serialize="true" px:message="Converting DOCX to DTBook" px:progess="1/2">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="oox2Daisy.xsl"/>
		</p:input>
		<p:with-param name="InputFile" select="$source"/>
		<p:with-param name="OutputDir" select="$temp-dir"/>
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
	<p:group>
		<p:documentation>Store plain text file and load as XML</p:documentation>
		<p:variable name="path" select="concat(
		                                  $temp-dir,
		                                  replace(replace($source,'^.*/([^/]*?)(\.[^/\.]*)?$','$1.xml'),',','_'))"/>
		<p:store name="store">
			<p:with-option name="href" select="$path"/>
		</p:store>
		<p:try>
			<p:group>
				<p:load cx:depends-on="store">
					<p:with-option name="href" select="$path"/>
				</p:load>
			</p:group>
			<p:catch name="catch">
				<p:choose>
					<p:xpath-context>
						<p:pipe step="catch" port="error"/>
					</p:xpath-context>
					<p:when test="/c:errors/c:error/@code='err:XD0011'">
						<px:log-error severity="DEBUG">
							<p:input port="source">
								<p:empty/>
							</p:input>
							<p:input port="error">
								<p:pipe step="catch" port="error"/>
							</p:input>
						</px:log-error>
						<px:error code="BUG" message="An unexpected error happened. Please contact maintainer."/>
					</p:when>
					<p:otherwise>
						<!-- re-throw error -->
						<px:error>
							<p:input port="error">
								<p:pipe step="catch" port="error"/>
							</p:input>
						</px:error>
					</p:otherwise>
				</p:choose>
			</p:catch>
		</p:try>
	</p:group>

	<!-- ******************************************************************* -->
	<!-- DTBOOK CLEANUP: apply cleanup routines and optionally tag sentences -->
	<!-- ******************************************************************* -->

	<p:for-each px:message="Cleaning DTBook(s)" px:progess="1/2">
		<p:variable name="output-name" select="concat(replace(replace(base-uri(.),'^.*/([^/]+)$','$1'),'\.[^\.]*$',''),'.xml')"/>
		<p:group name="cleaned" px:message="Cleaning '{$output-name}' ...">
			<p:output port="result"/>
			<!-- Update the DTBook -->
			<px:dtbook-upgrade/>
			<!-- Apply routines -->
			<pxi:dtbook-fix>
				<p:with-option name="repair" select="$repair"/>
				<p:with-option name="tidy" select="$tidy"/>
				<p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout"/>
				<p:with-option name="externalizeWhitespace" select="$externalizeWhitespace"/>
				<p:with-option name="documentLanguage" select="$documentLanguage"/>
				<p:with-option name="narrator" select="$narrator"/>
				<p:with-option name="publisher" select="$publisher"/>
			</pxi:dtbook-fix>
			<p:choose>
				<p:when test="$ApplySentenceDetection">
					<px:dtbook-break-detect/>
					<px:dtbook-unwrap-words/>
				</p:when>
				<p:otherwise>
					<p:identity/>
				</p:otherwise>
			</p:choose>
			<p:choose>
				<p:when test="$WithDoctype">
					<!-- DTBook with doctype (result is serialized) -->
					<!--
					    FIXME: this should be handled with px:fileset-store
					-->
					<pxi:dtbook-doctyping/>
				</p:when>
				<p:otherwise>
					<p:identity/>
				</p:otherwise>
			</p:choose>
		</p:group>
		<p:store px:message="Storing the cleaned DTBook and its resources ...">
			<p:with-option name="href" select="concat(resolve-uri($result),$output-name)"/>
		</p:store>
		<!-- Copying dtbook side resources -->
		<px:fileset-add-entry media-type="application/x-dtbook+xml" name="dtbook">
			<p:input port="entry">
				<p:pipe step="cleaned" port="result"/>
			</p:input>
		</px:fileset-add-entry>
		<px:dtbook-load name="load" />
		<px:fileset-filter not-media-types="application/x-dtbook+xml"/>
		<px:fileset-copy name="copy">
			<p:with-option name="target" select="resolve-uri($result)"/>
		</px:fileset-copy>
		<px:fileset-store>
			<p:input port="in-memory.in">
				<p:pipe step="copy" port="result.in-memory"/>
			</p:input>
		</px:fileset-store>
	</p:for-each>

</p:declare-step>
