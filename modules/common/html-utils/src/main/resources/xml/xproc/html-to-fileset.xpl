<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-to-fileset"
                version="1.0">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p px:role="desc">Creates a fileset document for an XHTML document.</p>
		<p>The fileset entries are ordered as the resources appear in the input document</p>
	</p:documentation>
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Input XHTML</h2>
			<p px:role="desc">An XHTML document.</p>
		</p:documentation>
	</p:input>
	
	<p:output port="fileset.out">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document with the XHTML file itself and all the resources
			referenced from the XHTML, in the order in which they appear. Some media types are
			inferred – users may have to apply additional type detection. A <code>@kind</code>
			attribute is used to annotate the kind of resource:</p>
			<ul>
				<li>stylesheet</li>
				<li>media</li>
				<li>image</li>
				<li>video</li>
				<li>audio</li>
				<li>script</li>
				<li>content</li>
				<li>description</li>
				<li>text-track</li>
				<li>animation</li>
				<li>font</li>
			</ul>
		</p:documentation>
	</p:output>
	
	<p:serialization port="fileset.out" indent="true"/>
	
	<p:add-attribute match="/*" attribute-name="xml:base">
		<p:with-option name="attribute-value" select="base-uri(/*)"/>
	</p:add-attribute>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/html-to-fileset.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
</p:declare-step>
