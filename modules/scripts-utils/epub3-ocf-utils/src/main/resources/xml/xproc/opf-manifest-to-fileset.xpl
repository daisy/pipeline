<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:opf-manifest-to-fileset" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
	
	<p:input port="source" px:media-type="application/oebps-package+xml">
		<p:documentation>
			An EPUB3 package document
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation>
			A fileset
		</p:documentation>
	</p:output>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/opf-manifest-to-fileset.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
</p:declare-step>
