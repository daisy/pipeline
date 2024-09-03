<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:word-to-dtbook" name="main">

	<p:option name="source" required="true"/>
	<p:option name="output-base-uri" required="true"/>
	<p:option name="title" select="''" required="false"/>
	<p:option name="creator" select="''" required="false"/>
	<p:option name="publisher" select="''" required="false"/>
	<p:option name="uid" select="''"/>
	<p:option name="subject" select="''"/>
	<p:option name="accept-revisions" select="true()" cx:as="xs:boolean"/>
	<p:option name="pagination" select="'Custom'"/>
	<p:option name="image-size" select="'original'"/>
	<p:option name="dpi" select="96" cx:as="xs:integer"/>
	<p:option name="character-styles" select="false()" cx:as="xs:boolean"/>
	<p:option name="footnotes-position" select="'end'"/>
	<p:option name="footnotes-level" select="0" cx:as="xs:integer"/>
	<p:option name="footnotes-numbering" cx:as="xs:string" select="'none'"/>
	<p:option name="footnotes-start-value" cx:as="xs:integer" select="1"/>
	<p:option name="footnotes-numbering-prefix" select="''"/> <!-- cx:as="xs:string?" -->
	<p:option name="footnotes-numbering-suffix" select="''"/> <!-- cx:as="xs:string?" -->
	<p:option name="extract-shapes" cx:as="xs:boolean" select="false()"/>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="load" port="result.in-memory"/>
	</p:output>

	<!-- for tests -->
	<p:option name="disable-date-generation" cx:as="xs:boolean" select="false()"/>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-add-entry
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
		<p:documentation>
			px:dtbook-load
		</p:documentation>
	</p:import>

	<p:xslt template-name="main" cx:serialize="true">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="oox2Daisy.xsl"/>
		</p:input>
		<p:with-param name="InputFile" select="$source"/>
		<p:with-param name="OutputDir" select="resolve-uri('./',$output-base-uri)"/>
		<p:with-param name="title" select="$title"/>
		<p:with-param name="creator" select="$creator"/>
		<p:with-param name="publisher" select="$publisher"/>
		<p:with-param name="uid" select="$uid"/>
		<p:with-param name="subject" select="$subject"/>
		<p:with-param name="acceptRevisions" select="$accept-revisions"/>
		<p:with-param name="version" select="'14'"/>
		<p:with-param name="pagination" select="$pagination"/>
		<p:with-param name="MasterSub" select="false()"/>
		<p:with-param name="ImageSizeOption" select="$image-size"/>
		<p:with-param name="DPI" select="$dpi"/>
		<p:with-param name="CharacterStyles" select="$character-styles"/>
		<p:with-param name="FootnotesPosition" select="$footnotes-position"/>
		<p:with-param name="FootnotesLevel" select="$footnotes-level"/>
		<p:with-param name="FootnotesNumbering" select="$footnotes-numbering"/>
		<p:with-param name="FootnotesStartValue" select="$footnotes-start-value"/>
		<p:with-param name="FootnotesNumberingPrefix" select="$footnotes-numbering-prefix"/>
		<p:with-param name="FootnotesNumberingSuffix" select="$footnotes-numbering-suffix"/>
		<p:with-param name="disableDateGeneration" select="$disable-date-generation"/>
		<p:with-param name="extractShapes" select="$extract-shapes"/>
	</p:xslt>
	
	<p:store name="store">
		<p:with-option name="href" select="$output-base-uri"/>
	</p:store>
	<px:fileset-add-entry media-type="application/x-dtbook+xml" name="fileset" cx:depends-on="store">
		<p:with-option name="href" select="string(/*)">
			<p:pipe step="store" port="result"/>
		</p:with-option>
	</px:fileset-add-entry>
	<px:dtbook-load name="load">
		<p:input port="source.in-memory">
			<p:pipe step="fileset" port="result.in-memory"/>
		</p:input>
	</px:dtbook-load>

</p:declare-step>
