<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:opf-spine-to-fileset" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:opf="http://www.idpf.org/2007/opf"
                exclude-inline-prefixes="#all"
                name="main">

	<p:input port="source" px:media-type="application/oebps-package+xml">
		<p:documentation>
			An EPUB3 package document.
		</p:documentation>
	</p:input>
	<p:output port="result">
		<p:documentation>
			A fileset manifest of the content items in spine order.
		</p:documentation>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-add-entry
		</p:documentation>
	</p:import>

	<px:fileset-create base="/" name="base"/>

	<p:for-each>
		<p:iteration-source select="/*/opf:spine/opf:itemref">
			<p:pipe step="main" port="source"/>
		</p:iteration-source>
		<p:variable name="idref" select="/*/@idref"/>
		<p:filter>
			<p:input port="source">
				<p:pipe step="main" port="source"/>
			</p:input>
			<p:with-option name="select" select="concat('/*/opf:manifest/opf:item[@id=&quot;',$idref,'&quot;]')"/>
		</p:filter>
		<px:fileset-add-entry>
			<p:with-option name="href" select="/*/resolve-uri(@href,base-uri())"/>
			<p:with-option name="media-type" select="/*/@media-type"/>
			<p:input port="source">
				<p:pipe step="base" port="result"/>
			</p:input>
		</px:fileset-add-entry>
	</p:for-each>

	<px:fileset-join/>

</p:declare-step>
