<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-inline-prefixes="#all"
                type="px:daisy3-upgrader.script" name="main"
                px:input-filesets="daisy3"
                px:output-filesets="daisy3">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DAISY 3 Upgrader</h1>
		<p px:role="desc">Upgrades a DAISY 3 publication from version 1.1.0 (Z39.86-2002) to version 2005 (Z39.86-2005).</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy3-upgrader/">
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

	<p:input port="source" px:media-type="application/oebps-package+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 3</h2>
			<p px:role="desc">The package file (OPF) of the input DAISY 3.</p>
		</p:documentation>
	</p:input>

	<p:option xmlns:_="daisy3" name="_:ensure-core-media" select="'false'">
		<!-- defined in ../../../../../../scripts/common-options.xpl -->
	</p:option>

	<p:option name="result" px:output="result" px:type="anyDirURI" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Output DAISY 3</h2>
			<p px:role="desc">The upgraded DAISY 3.</p>
		</p:documentation>
	</p:option>

	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-create
			px:fileset-add-entry
			px:fileset-store
		</p:documentation>
	</p:import>
	<p:import href="internal/load/load.xpl">
		<p:documentation>
			px:daisy3-load
		</p:documentation>
	</p:import>
	<p:import href="daisy3-upgrader.xpl">
		<p:documentation>
			px:daisy3-upgrader
		</p:documentation>
	</p:import>

	<px:fileset-create>
		<p:with-option name="base" select="resolve-uri('./',base-uri(/*))"/>
	</px:fileset-create>
	<px:fileset-add-entry media-type="application/oebps-package+xml">
		<p:input port="entry">
			<p:pipe step="main" port="source"/>
		</p:input>
	</px:fileset-add-entry>
	<px:daisy3-load name="load" px:progress="1/10" px:message="Loading DAISY 3"/>

	<px:daisy3-upgrader name="convert" px:progress="0.8" px:message="Upgrading DAISY 3">
		<p:input port="source.in-memory">
			<p:pipe step="load" port="result.in-memory"/>
		</p:input>
		<p:with-option name="output-dir" select="$result"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
		<p:with-option name="ensure-core-media" xmlns:_="daisy3" select="$_:ensure-core-media='true'"/>
	</px:daisy3-upgrader>

	<px:fileset-store px:progress="0.1" px:message="Storing DAISY 2.02">
		<p:input port="in-memory.in">
			<p:pipe step="convert" port="result.in-memory"/>
		</p:input>
	</px:fileset-store>

</p:declare-step>
