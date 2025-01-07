<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:zedai-to-opf-metadata"
                exclude-inline-prefixes="#all">
	
	<p:documentation>
		<p>Extract <a
		href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
		metadata</a> from a ZedAI document</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>
	<p:option name="output-base-uri" cx:as="xs:string?" select="()">
		<p:documentation>
			Will determine the relative path to linked resources in the output, which are assumed to
			be relative to the input base URI in the input.
		</p:documentation>
	</p:option>
	<p:option name="source-of-pagination" cx:as="xs:string?" select="()"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/zedai-to-opf-metadata.xsl"/>
		</p:input>
		<p:with-param port="parameters" name="source-of-pagination" select="$source-of-pagination"/>
		<p:with-param name="output-base-uri" select="($output-base-uri,base-uri(/))[1]"/>
		<p:with-option name="output-base-uri" select="($output-base-uri,base-uri(/))[1]"/>
	</p:xslt>
	
</p:declare-step>
