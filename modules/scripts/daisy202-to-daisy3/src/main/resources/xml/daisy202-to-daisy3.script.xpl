<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:daisy202-to-daisy3.script"
                px:input-filesets="daisy202"
                px:output-filesets="daisy3">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DAISY 2.02 to DAISY 3</h1>
		<p px:role="desc">Upgrades a DAISY 2.02 DTB to a DAISY 3 DTB.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy202-to-daisy3/">
			Online documentation
		</a>
		<address>
			Authors:
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Markus Gylling</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Brandon Nelson</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Per Sennels</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Bert Frees</dd>
			</dl>
		</address>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 2.02</h2>
			<p px:role="desc">NCC of the input DAISY 2.02.</p>
		</p:documentation>
	</p:option>

	<p:option name="result" px:output="result" px:type="anyDirURI" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 3</h2>
			<p px:role="desc">The resulting DAISY 3.</p>
		</p:documentation>
	</p:option>

	<p:option name="identifier" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Identifier</h2>
			<p px:role="desc" xml:space="preserve">A string that uniquely identifies the DAISY 3 DTB to be generated.

If no value is supplied, a value is extracted from the input NCC.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl">
		<p:documentation>
			px:daisy202-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-store
		</p:documentation>
	</p:import>
	<p:import href="daisy202-to-daisy3.xpl">
		<p:documentation>
			px:daisy202-to-daisy3
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<!-- this will list the SMIL files according to reading order -->
	<px:daisy202-load name="load" px:progress="0.1" px:message="Loading DAISY 2.02">
		<p:with-option name="ncc" select="$source"/>
	</px:daisy202-load>

	<px:daisy202-to-daisy3 name="convert" px:progress="0.8" px:message="Upgrading DAISY 2.02 to DAISY 3">
		<p:input port="source.in-memory">
			<p:pipe step="load" port="in-memory.out"/>
		</p:input>
		<p:with-option name="output-dir" select="pf:normalize-uri(concat($result,'/'))"/>
	</px:daisy202-to-daisy3>

	<px:fileset-store px:progress="0.1" px:message="Storing DAISY 3">
		<p:input port="in-memory.in">
			<p:pipe step="convert" port="result.in-memory"/>
		</p:input>
	</px:fileset-store>

</p:declare-step>
