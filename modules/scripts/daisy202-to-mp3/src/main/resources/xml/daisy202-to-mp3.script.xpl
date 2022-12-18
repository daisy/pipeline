<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:daisy202-to-mp3.script"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DAISY 2.02 to MP3 (beta)</h1>
		<p px:role="desc">Transforms a DAISY 2.02 publication into a folder structure with MP3 files (beta).</p>
		<p><a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy202-to-mp3/"
		      >Online documentation</a></p>
	</p:documentation>

	<p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DAISY 2.02</h2>
			<p px:role="desc">The NCC file of the input DAISY 2.02.</p>
		</p:documentation>
	</p:option>

	<p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">MP3 files</h2>
			<p px:role="desc">The produced folder structure with MP3 files.</p>
		</p:documentation>
	</p:option>
	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Temporary directory</h2>
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
			px:fileset-delete
		</p:documentation>
	</p:import>
	<p:import href="daisy202-to-mp3.xpl">
		<p:documentation>
			px:daisy202-to-mp3
		</p:documentation>
	</p:import>

	<px:daisy202-load name="load" px:progress="1/10" px:message="Loading DAISY 2.02">
		<p:documentation>Lists SMILS in reading order.</p:documentation>
		<p:with-option name="ncc" select="$source"/>
	</px:daisy202-load>

	<px:daisy202-to-mp3 name="convert" px:progress="8/10" px:message="Rearranging audio into folder structure">
		<p:input port="source.in-memory">
			<p:pipe step="load" port="in-memory.out"/>
		</p:input>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:daisy202-to-mp3>

	<px:fileset-store px:progress="1/10" name="store" px:message="Storing MP3 files"/>

	<px:fileset-delete cx:depends-on="store">
		<p:input port="source">
			<p:pipe step="convert" port="temp-files"/>
		</p:input>
	</px:fileset-delete>

</p:declare-step>
