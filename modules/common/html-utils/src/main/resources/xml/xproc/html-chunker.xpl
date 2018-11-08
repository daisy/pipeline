<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:html-chunker"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                exclude-inline-prefixes="#all"
                version="1.0"
                name="main">
	
	<p:documentation>
		<p xmlns="http://www.w3.org/1999/xhtml">Break a HTML document into smaller parts based on
			its structure.</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:option name="max-chunk-size" select="'-1'"/>
	<p:output port="result" sequence="true"/>
	
	<p:import href="chunker.xpl"/>
	
	<p:delete match="/html:html/html:head"/>

	<px:chunker allow-break-before="html:section"
	            allow-break-after="html:section"
	            prefer-break-before="/html:html/html:body/html:section/html:section|
	                                 /html:html/html:body/html:section/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	            prefer-break-after="/html:html/html:body/html:section/html:section|
	                                 /html:html/html:body/html:section/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	            always-break-before="/html:html/html:body/html:section|
	                                 /html:html/html:body/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	            always-break-after="/html:html/html:body/html:section|
	                                /html:html/html:body/html:section[tokenize(@epub:type,'\s+')='bodymatter']/html:section"
	            link-attribute-name="href">
		<p:with-option name="max-chunk-size" select="$max-chunk-size"/>
	</px:chunker>
	
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
	</p:for-each>
	
</p:declare-step>
