<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xml-chunker"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                version="1.0"
                name="main">
	
	<p:documentation>
		<p xmlns="http://www.w3.org/1999/xhtml">Break a document into smaller parts.</p>
	</p:documentation>
	
	<p:input port="source"/>
	
	<p:option name="max-chunk-size" select="'-1'"/>
	
	<p:option name="part-attribute" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>If specified, all elements that occur in multiple chunks get an attribute with this
			QName. The value is one of 'head', 'middle' or 'tail'.</p>
		</p:documentation>
	</p:option>

	<p:option name="propagate" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to propagate break opportunities across white space nodes and element
			start/end tags.</p>
		</p:documentation>
	</p:option>

	<p:option name="allow-break-before" select="'/*'"/>
	<p:option name="allow-break-after" select="'/*'"/>
	<p:option name="prefer-break-before" select="'/*'"/>
	<p:option name="prefer-break-after" select="'/*'"/>
	
	<p:option name="always-break-before" select="'/*'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An XSLTMatchPattern that specifies break points. Each element that matches this
			expression will have a break point before it.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="always-break-after" select="'/*'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An XSLTMatchPattern that specifies break points. Each element that matches this
			expression will have a break point after it.</p>
		</p:documentation>
	</p:option>
	
	<p:output port="result" sequence="true" primary="true">
		<p:documentation>
			<p xmlns="http://www.w3.org/1999/xhtml">Every output document gets a different base URI
			derived from the input base URI.</p>
		</p:documentation>
	</p:output>
	
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			A <p><code>d:fileset</code> document that contains a mapping from input file
			(<code>@original-href</code>) to output files (<code>@href</code>) with contained
			<code>id</code> attributes (<code>d:anchor</code>).</p>
		</p:documentation>
	</p:output>
	
	<!--
	    implemented in Java
	-->
	
</p:declare-step>
