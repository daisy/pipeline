<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="px:daisy202-fix-audio-file-order.script" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DAISY 2.02 Unscrambler</h1>
		<p px:role="desc">Renames audio files in a DAISY 2.02 publication in such a way that when
		sorted alphabetically, they are also sorted according to the reading order.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy202-unscrambler/">
			Online documentation
		</a>
		<address>
			Authors:
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Bert Frees</dd>
				<dt>E-mail:</dt>
				<dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
			</dl>
		</address>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Input DAISY 2.02</h2>
			<p px:role="desc">NCC of the input DAISY 2.02.</p>
		</p:documentation>
	</p:option>

	<p:option name="result" px:output="result" px:type="anyDirURI" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Output DAISY 2.02</h2>
			<p px:role="desc">The unscrambled DAISY 2.02.</p>
		</p:documentation>
	</p:option>

	<p:import href="load/load.xpl">
		<p:documentation>
			px:daisy202-load
		</p:documentation>
	</p:import>
	<p:import href="fix-audio-file-order.xpl">
		<p:documentation>
			px:daisy202-fix-audio-file-order
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-copy
			px:fileset-store
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<px:daisy202-load name="load" px:progress="0.1" px:message="Loading DAISY 2.02">
		<p:with-option name="ncc" select="$source"/>
	</px:daisy202-load>

	<px:fileset-copy name="copy">
		<p:with-option name="target" select="pf:normalize-uri(concat($result,'/'))"/>
		<p:input port="source.in-memory">
			<p:pipe step="load" port="in-memory.out"/>
		</p:input>
	</px:fileset-copy>

	<px:daisy202-fix-audio-file-order name="convert" px:progress="0.8" px:message="Unscrambling DAISY 2.02">
		<p:input port="source.in-memory">
			<p:pipe step="copy" port="result.in-memory"/>
		</p:input>
	</px:daisy202-fix-audio-file-order>

	<px:fileset-store px:progress="0.1" px:message="Storing DAISY 2.02">
		<p:input port="in-memory.in">
			<p:pipe step="convert" port="result.in-memory"/>
		</p:input>
	</px:fileset-store>

</p:declare-step>
