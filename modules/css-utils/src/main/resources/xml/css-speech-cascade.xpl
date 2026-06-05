<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:css-speech-cascade" name="main"
                exclude-inline-prefixes="#all">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>CSS cascading and inlining of CSS Aural style sheets</p>
		<p>The inlining is done through special <code>@tts:*</code> attributes for each of the
		properties.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="update" port="result.in-memory"/>
	</p:output>
	<p:option name="content-type" required="false" select="'text/html application/xhtml+xml application/x-dtbook+xml'"/>
	<p:option name="user-stylesheet" required="false" select="''"/>
	<p:option name="parameters" select="map{}"/> <!-- (map(xs:string,item()) | xs:string)* -->
	<p:option name="include-user-agent-stylesheet" required="false" cx:as="xs:boolean" select="false()"/>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-update
		</p:documentation>
	</p:import>
	<p:import href="css-cascade.xpl">
		<p:documentation>
			px:css-cascade
		</p:documentation>
	</p:import>
	<p:import href="clean-up-namespaces.xpl">
		<p:documentation>
			pxi:clean-up-namespaces
		</p:documentation>
	</p:import>

	<px:css-cascade media="speech" multiple-attributes="true" name="cascade">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
		<p:with-option name="content-type" select="$content-type"/>
		<p:with-option name="user-stylesheet" select="$user-stylesheet"/>
		<p:with-option name="include-user-agent-stylesheet" select="$include-user-agent-stylesheet"/>
		<p:with-option name="parameters" select="$parameters"/>
		<p:with-option name="attribute-name" select="QName('http://www.daisy.org/ns/pipeline/tts','tts:_')"/>
	</px:css-cascade>

	<px:fileset-load name="content">
		<p:input port="in-memory">
			<p:pipe step="cascade" port="result.in-memory"/>
		</p:input>
		<p:with-option name="media-types" select="$content-type"/>
	</px:fileset-load>

	<p:for-each name="clean-up-namespaces">
		<p:output port="result"/>
		<pxi:clean-up-namespaces/>
	</p:for-each>
	<p:sink/>

	<px:fileset-update name="update">
		<p:input port="source.fileset">
			<p:pipe step="cascade" port="result"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="cascade" port="result.in-memory"/>
		</p:input>
		<p:input port="update.fileset">
			<p:pipe step="content" port="result.fileset"/>
		</p:input>
		<p:input port="update.in-memory">
			<p:pipe step="clean-up-namespaces" port="result"/>
		</p:input>
	</px:fileset-update>

</p:declare-step>
