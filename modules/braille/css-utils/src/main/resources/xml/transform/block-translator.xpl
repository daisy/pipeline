<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">
	
	<p:option name="text-transform" select="''"/>
	<p:option name="no-wrap" select="'false'"/>
	<p:option name="main-locale" select="''"/>
	
	<p:import href="../library.xpl"/>
	
	<css:parse-properties px:message="Parsing CSS properties" px:progress=".05"
	                      properties="display"/>
	
	<p:xslt px:message="Translating CSS blocks" px:progress=".95">
		<p:input port="stylesheet">
			<p:document href="block-translator-from-text-transform.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
		<p:with-param name="no-wrap" select="$no-wrap"/>
		<p:with-param name="main-locale" select="$main-locale"/>
	</p:xslt>
	
</p:pipeline>
