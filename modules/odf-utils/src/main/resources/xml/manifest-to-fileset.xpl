<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:odf-manifest-to-fileset" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <a
			href="http://docs.oasis-open.org/office/v1.2/os/OpenDocument-v1.2-os-part3.html#Manifest_File">ODF
			manifest document</a></p>
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A d:fileset manifest</p>
		</p:documentation>
	</p:output>
	<p:option name="base" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI of the resulting fileset</p>
		</p:documentation>
	</p:option>
	<p:option name="original-base" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI against which to resolve files when loading them into memory (optional)</p>
		</p:documentation>
	</p:option>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="manifest-to-fileset.xsl"/>
		</p:input>
		<p:with-param name="base" select="$base"/>
		<p:with-param name="original-base" select="$original-base"/>
	</p:xslt>
	
</p:declare-step>
