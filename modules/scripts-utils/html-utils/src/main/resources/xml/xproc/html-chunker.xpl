<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:html-chunker"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                exclude-inline-prefixes="#all"
                version="1.0"
                name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Break a HTML document into smaller parts based on its structure.</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:option name="max-chunk-size" select="'-1'"/>
	<p:output port="result" primary="true" sequence="true"/>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that contains a mapping from input file
			(<code>@original-href</code>) to output files (<code>@href</code>) with contained
			<code>id</code> attributes (<code>d:anchor</code>).</p>
		</p:documentation>
		<p:pipe step="chunker" port="mapping"/>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:xml-chunker
		</p:documentation>
	</p:import>
	<p:import href="html-update-links.xpl">
		<p:documentation>
			px:html-update-links
		</p:documentation>
	</p:import>
	
	<p:delete match="/html:html/html:head"/>

	<px:xml-chunker name="chunker"
	                allow-break-before="html:section"
	                allow-break-after="html:section"
	                prefer-break-before="/html:html/html:body/html:section/html:section|
	                                     /html:html/html:body/html:section/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	                prefer-break-after="/html:html/html:body/html:section/html:section|
	                                     /html:html/html:body/html:section/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	                always-break-before="/html:html/html:body/html:section|
	                                     /html:html/html:body/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	                always-break-after="/html:html/html:body/html:section|
	                                    /html:html/html:body/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section">
		<p:with-option name="max-chunk-size" select="$max-chunk-size"/>
	</px:xml-chunker>
	
	<p:for-each name="chunks">
		<p:xslt>
			<p:input port="source">
				<p:pipe step="chunks" port="current"/>
				<p:pipe step="main" port="source"/>
			</p:input>
			<p:input port="stylesheet">
				<p:document href="../xslt/html-chunker-finalize.xsl"/>
			</p:input>
			<p:input port="parameters">
				<p:empty/>
			</p:input>
		</p:xslt>
		<px:html-update-links source-renamed="true">
			<p:input port="mapping">
				<p:pipe step="chunker" port="mapping"/>
			</p:input>
		</px:html-update-links>
	</p:for-each>
	
</p:declare-step>
