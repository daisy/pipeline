<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-to-rtf" name="main">
	
	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	
	<p:output port="result"/>
	
	<p:option name="include-table-of-content" required="true" cx:as="xs:string"/>
	<p:option name="include-page-number" required="true" cx:as="xs:string"/>
	
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
	
	<p:variable name="tmpfile-uri" select="concat($temp-dir,'tmp.xml')"/>
	
	<!-- Find the first and only DTBook file -->
	<px:fileset-load media-types="application/x-dtbook+xml">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert message="No DTBook document found." test-count-min="1" error-code="PEZE00"/>
	<px:assert message="More than one DTBook found in fileset." test-count-max="1" error-code="PEZE00"/>
	
	<p:xslt name="add-dtbook-id">
		<p:input port="stylesheet">
			<p:document href="add_ids_to_dtbook.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	<p:sink/>
	
	<p:store name="store-tmpfile">
		<p:with-option name="href" select="$tmpfile-uri"/>
		<p:input port="source">
			<p:pipe port="result" step="add-dtbook-id"/>
		</p:input>
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
