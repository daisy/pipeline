<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0" xmlns:louis="http://liblouis.org/liblouis">
	
	<p:declare-step type="louis:translate-file">
		<p:input port="source" sequence="false" primary="true"/>
		<p:input port="styles" sequence="true"/>
		<p:input port="semantics" sequence="true"/>
		<p:input port="page-layout" kind="parameter" primary="true"/>
		<p:option name="table" required="false"/>
		<p:option name="paged" required="false"/>
		<p:option name="temp-dir" required="true"/>
		<p:output port="result" sequence="true" primary="true"/>
	</p:declare-step>
	
</p:library>
