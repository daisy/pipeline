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
			<h2 px:role="name">Docx file</h2>
			<p px:role="desc" xml:space="preserve">The Word document you want to transform.</p>
		</p:documentation>
	</p:option>
	<p:option name="result" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation>
			<h2 px:role="name">DTBook</h2>
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

	<!-- defined in ../../../../../common-options.xpl -->
	<p:option name="accept-revisions" select="true()"/>
	<!-- from settings  -->
	<p:option name="pagination" select="'Custom'"/>
	<p:option name="image-size" select="'original'"/>
	<p:option name="dpi" select="96" cx:as="xs:integer"/>
	<p:option name="character-styles" select="false()"/>
	<p:option name="footnotes-position" select="'end'"/>
	<p:option name="footnotes-level" select="0"/>
	<p:option name="footnotes-numbering" select="'none'"/>
	<p:option name="footnotes-start-value" select="1"/>
	<p:option name="footnotes-numbering-prefix" select="''"/>
	<p:option name="footnotes-numbering-suffix" select="''"/>
	<p:option name="extract-shapes" select="false()"/>

	<!-- discarding math type equations preprocessing
	<p:option name="MathML" select="map{'wdTextFrameStory':[],
	                                    'wdFootnotesStory':[],
	                                    'wdMainTextStory':[]
	                                    }" />-->
	<!-- cx:as="map(xs:string,xs:string*)" -->

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

	<px:word-to-dtbook name="convert">
		<p:with-option name="source" select="$source"/>
		<p:with-option name="output-base-uri"
		               select="concat(
		                         $result,
		                         replace(replace($source,'^.*/([^/]*?)(\.[^/\.]*)?$','$1.xml'),',','_'))"/>
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
		<p:with-option name="footnotes-position" select="$footnotes-position"/>
		<p:with-option name="footnotes-level" select="$footnotes-level"/>
		<p:with-option name="footnotes-numbering" select="$footnotes-numbering"/>
		<p:with-option name="footnotes-start-value" select="$footnotes-start-value"/>
		<p:with-option name="footnotes-numbering-prefix" select="$footnotes-numbering-prefix"/>
		<p:with-option name="footnotes-numbering-suffix" select="$footnotes-numbering-suffix"/>
		<p:with-option name="extract-shapes" select="$extract-shapes"/>
	</px:word-to-dtbook>

	<!-- fileset is already stored, but store just to be sure -->
	<px:fileset-store>
		<p:input port="in-memory.in">
			<p:pipe step="convert" port="result.in-memory"/>
		</p:input>
	</px:fileset-store>

</p:declare-step>
