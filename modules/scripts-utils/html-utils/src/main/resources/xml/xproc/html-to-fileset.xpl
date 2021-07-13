<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:html-to-fileset" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p px:role="desc">Creates a fileset document for an XHTML document.</p>
		<p>The fileset entries are ordered as the resources appear in the input document</p>
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Input XHTML</h2>
			<p px:role="desc">An XHTML document.</p>
		</p:documentation>
	</p:input>
	
	<p:input port="context.fileset" primary="false">
		<p:inline><d:fileset/></p:inline>
	</p:input>
	<p:input port="context.in-memory" primary="false" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Context fileset.</p>
			<p>Will be used for loading resources. If files are present in memory, they are expected
			to be <code>c:data</code> documents. Only when files are not present in this fileset, it
			will be attempted to load them from disk.</p>
		</p:documentation>
		<p:empty/>
	</p:input>

	<p:output port="result">
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
	
	<p:serialization port="result" indent="true"/>

	<p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
		<p:documentation>
			px:mediatype-detect
		</p:documentation>
	</p:import>
	
	<p:add-attribute match="/*" attribute-name="xml:base">
		<p:with-option name="attribute-value" select="base-uri(/*)"/>
	</p:add-attribute>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/html-to-fileset.xsl"/>
		</p:input>
		<p:with-param port="parameters" name="context.fileset" select="/">
			<p:pipe step="main" port="context.fileset"/>
		</p:with-param>
		<p:with-param port="parameters" name="context.in-memory" select="collection()">
			<p:pipe step="main" port="context.in-memory"/>
		</p:with-param>
	</p:xslt>
	
	<px:mediatype-detect/>
	
</p:declare-step>
