<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:xml-to-css-fileset"
                version="1.0">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extract a list of associated CSS stylesheets from an XML document.</p>
	</p:documentation>
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An XML with a number of CSS stylesheets associated with it, either through an <a
			href="https://www.w3.org/TR/xml-stylesheet">'xml-stylesheet' processing instructions</a>
			or a <a href="https://www.w3.org/Style/styling-XML#External">'link' element</a>.</p>
		</p:documentation>
	</p:input>
	
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document.</p>
		</p:documentation>
	</p:output>
	
	<p:option name="include-links" required="false" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether or not to process link elements.</p>
		</p:documentation>
	</p:option>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="xml-to-css-uris.xsl"/>
		</p:input>
		<p:with-param name="include-links" select="$include-links"/>
	</p:xslt>
	
</p:declare-step>
