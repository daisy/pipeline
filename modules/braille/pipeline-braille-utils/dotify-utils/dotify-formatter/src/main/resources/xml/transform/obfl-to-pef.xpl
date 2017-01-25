<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dotify-obfl-to-pef" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:dotify="http://code.google.com/p/dotify/"
            xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
            exclude-inline-prefixes="#all"
            name="main">
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:option name="text-transform" required="true"/>
	<p:input port="parameters" kind="parameter" primary="false"/>
	
	<p:import href="../obfl-normalize-space.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl"/>
	
	<pxi:obfl-normalize-space/>
	
	<dotify:obfl-to-pef locale="und">
		<p:with-option name="mode" select="$text-transform"/>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</dotify:obfl-to-pef>
	
</p:declare-step>
