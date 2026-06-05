<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                type="px:dtbook-to-rtf" name="main">
	
	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	
	<p:output port="result"/>
	
	<p:option name="include-table-of-content" required="true" cx:as="xs:string"/>
	<p:option name="include-page-number" required="true" cx:as="xs:string"/>
	
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:delete
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
		<p:documentation>
			px:css-cascade
		</p:documentation>
	</p:import>
	
	<p:variable name="tmpfile-uri" select="concat($temp-dir,'tmp.xml')"/>
	
	<!-- Find the first and only DTBook file -->
	<px:fileset-load media-types="application/x-dtbook+xml">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert message="No DTBook document found." test-count-min="1" error-code="PEZE00"/>
	<px:assert message="More than one DTBook found in fileset." test-count-max="1" error-code="PEZE00"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="add_ids_to_dtbook.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

	<!-- number lists using CSS -->
	<p:choose>
		<p:when test="//dtb:list[@type='ol'][@enum|@start]">
			<px:css-cascade media="print and (counter-support: none)" multiple-attributes="true">
				<p:with-option name="attribute-name" select="QName('css', 'css:_')"/>
				<p:with-option name="user-stylesheet" select="resolve-uri('number-lists.scss')">
					<p:inline><irrelevant/></p:inline>
				</p:with-option>
			</px:css-cascade>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
	<p:store name="store-tmpfile">
		<p:with-option name="href" select="$tmpfile-uri"/>
	</p:store>

	<p:xslt name="convert-to-rtf" template-name="start">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="dtbook_to_rtf.xsl"/>
		</p:input>
		<p:with-param name="inclTOC" select="$include-table-of-content"/>
		<p:with-param name="inclPagenum" select="$include-page-number"/>
		<p:with-param name="sourceFile" select="/c:result/string()">
			<p:pipe step="store-tmpfile" port="result"/>
		</p:with-param>
	</p:xslt>

	<px:delete cx:depends-on="convert-to-rtf" name="delete-tmpfile">
		<p:with-option name="href" select="$tmpfile-uri"/>
	</px:delete>
	
	<p:identity cx:depends-on="delete-tmpfile">
		<p:input port="source">
			<p:pipe step="convert-to-rtf" port="result"/>
		</p:input>
	</p:identity>
	
</p:declare-step>
