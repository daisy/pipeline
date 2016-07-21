<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:liblouis-transform" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:louis="http://liblouis.org/liblouis"
                exclude-inline-prefixes="#all">
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:option name="query" select="''"/>
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
	<p:import href="../format.xpl"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="handle-list-item.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
	<px:transform>
		<p:with-option name="query" select="$query"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:transform>
	
	<louis:format>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</louis:format>
	
</p:declare-step>
