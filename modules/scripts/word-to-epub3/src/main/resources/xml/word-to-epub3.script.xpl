<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:word-to-epub3.script" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">Word to EPUB 3</h1>
		<p px:role="desc" xml:space="preserve">Transforms a Microsoft Office Word (.docx) document into an EPUB 3 publication.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/word-to-epub3/">
			Online documentation
		</a>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI">
		<p:documentation>
			<h2 px:role="name">Docx file</h2>
			<p px:role="desc" xml:space="preserve">The Word document you want to transform.</p>
		</p:documentation>
	</p:option>
	<p:option name="result" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation>
			<h2 px:role="name">EPUB 3</h2>
		</p:documentation>
	</p:option>

	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>
	<p:output port="status" px:media-type="application/vnd.pipeline.status+xml" primary="true">
		<!-- when text-to-speech is enabled, the conversion may output a (incomplete) EPUB 3
		     publication even if the text-to-speech process has errors -->
		<!-- a `tts-success-rate' attribute contains the percentage of the input text that got
		     successfully converted to speech -->
	</p:output>

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
	<p:option name="chunk-size" required="false" px:type="integer" select="'-1'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Chunk size</h2>
			<p px:role="desc" xml:space="preserve">The maximum size of HTML files in kB. Specify "-1" for no maximum.

Top-level sections become separate HTML files in the resulting EPUB, and are further split up
if they exceed the given maximum size.</p>
		</p:documentation>
	</p:option>

	<!-- defined in ../../../../../common-options.xpl -->
	<p:option name="accept-revisions" select="true()"/>
	<!-- from settings  -->
	<p:option name="pagination" select="'custom'"/>
	<p:option name="image-size" select="'original'"/>
	<p:option name="dpi" select="96" cx:as="xs:integer"/>
	<p:option name="character-styles" select="false()"/>
	<p:option name="extract-shapes" select="false()"/>
	<!-- for dtbook-to-epub3 -->
	<p:option name="audio" select="'false'"/>
	<p:input port="tts-config">
		<p:inline><d:config/></p:inline>
	</p:input>
	<p:option name="lexicon" select="p:system-property('d:org.daisy.pipeline.tts.default-lexicon')"/>
	<p:output port="tts-log" sequence="true">
		<p:pipe step="epub3" port="tts-log"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub3-store
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/word-to-dtbook/library.xpl">
		<p:documentation>
			px:word-to-dtbook
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-epub3/library.xpl">
		<p:documentation>
			px:dtbook-to-epub3
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<p:variable name="output-name" select="replace(replace($source,'^.*/([^/]*?)(\.[^/\.]*)?$','$1'),',','_')"/>
	<p:variable name="output-dir-uri" select="pf:normalize-uri(concat($result,'/'))"/>

	<px:word-to-dtbook name="dtbook" px:progress="1/4" px:message="Converting Word to DTBook">
		<p:with-option name="source" select="$source"/>
		<p:with-option name="output-base-uri" select="pf:normalize-uri(concat($temp-dir,'/dtbook/',$output-name,'.xml'))"/>
		<p:with-option name="title" select="$title"/>
		<p:with-option name="creator" select="$creator"/>
		<p:with-option name="publisher" select="$publisher"/>
		<p:with-option name="uid" select="$uid"/>
		<p:with-option name="subject" select="$subject"/>
		<p:with-option name="accept-revisions" select="$accept-revisions"/>
		<p:with-option name="pagination" select="$pagination"/>
		<p:with-option name="image-size" select="$image-size"/>
		<p:with-option name="dpi" select="$dpi"/>
		<p:with-option name="character-styles" select="$character-styles"/>
		<p:with-option name="extract-shapes" select="$extract-shapes"/>
	</px:word-to-dtbook>

	<px:dtbook-to-epub3 name="epub3" px:progress="3/4" px:message="Converting DTBook to EPUB 3">
		<p:input port="source.in-memory">
			<p:pipe step="dtbook" port="result.in-memory"/>
		</p:input>
		<p:input port="tts-config">
			<p:pipe step="main" port="tts-config"/>
		</p:input>
		<p:with-option name="lexicon" select="for $l in tokenize($lexicon,'\s+')[not(.='')] return
		                                      resolve-uri($l,$source)"/>
		<p:with-option name="audio" select="$audio"/>
		<!-- reporting validation issues in intermediary documents is not helpful for user -->
		<p:with-option name="validation" select="'off'"/>
		<p:with-option name="chunk-size" select="$chunk-size"/>
		<p:with-option name="output-name" select="$output-name"/>
		<p:with-option name="output-dir" select="$output-dir-uri"/>
		<p:with-option name="temp-dir" select="pf:normalize-uri(concat($temp-dir,'/epub3/'))"/>
	</px:dtbook-to-epub3>

	<px:epub3-store name="store" px:message="Storing EPUB 3">
		<p:input port="in-memory.in">
			<p:pipe step="epub3" port="result.in-memory"/>
		</p:input>
		<p:with-option name="href" select="concat($output-dir-uri,$output-name,'.epub')"/>
	</px:epub3-store>

	<p:identity cx:depends-on="store">
		<p:input port="source">
			<p:pipe step="epub3" port="status"/>
		</p:input>
	</p:identity>

</p:declare-step>
