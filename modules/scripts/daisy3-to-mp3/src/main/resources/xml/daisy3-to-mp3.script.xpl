<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:daisy3-to-mp3.script"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DAISY 3 to MegaVoice multi-level</h1>
		<p px:role="desc">Transforms a DAISY 3 publication into a folder structure with MP3 files suitable for playback on MegaVoice Envoy devices (all versions except the Envoy Connect).</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy3-to-mp3/">
			Online documentation
		</a>
	</p:documentation>

	<p:input port="source" px:media-type="application/oebps-package+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 3</h2>
			<p px:role="desc">The package file (OPF) of the input DAISY 3.</p>
		</p:documentation>
	</p:input>

	<p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">MP3 files</h2>
			<p px:role="desc">The produced folder structure with MP3 files.</p>
		</p:documentation>
	</p:option>
	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
		<p:documentation>
			px:daisy3-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-create
			px:fileset-add-entry
			px:fileset-store
			px:fileset-delete
		</p:documentation>
	</p:import>
	<p:import href="daisy3-to-mp3.xpl">
		<p:documentation>
			px:daisy3-to-mp3
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
	<px:daisy3-load name="load" px:progress="1/10" px:message="Loading DAISY 3">
		<p:documentation>Lists SMILS in spine order.</p:documentation>
	</px:daisy3-load>

	<px:daisy3-to-mp3 name="convert" px:progress="8/10" px:message="Rearranging audio into folder structure">
		<p:input port="source.in-memory">
			<p:pipe step="load" port="result.in-memory"/>
		</p:input>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:daisy3-to-mp3>

	<px:fileset-store px:progress="1/10" name="store" px:message="Storing MP3 files"/>

	<px:fileset-delete cx:depends-on="store">
		<p:input port="source">
			<p:pipe step="convert" port="temp-files"/>
		</p:input>
	</px:fileset-delete>

</p:declare-step>
