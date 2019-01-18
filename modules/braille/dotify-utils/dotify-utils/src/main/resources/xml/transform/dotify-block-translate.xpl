<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:dotify-block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">
	
	<p:option name="query" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
	
	<css:parse-properties px:message="Parsing CSS properties" px:progress="1/2"
	                      properties="display"/>
	
	<p:xslt px:message="Translating Dotify blocks" px:progress="1/2">
		<p:input port="stylesheet">
			<p:document href="dotify-block-translate.xsl"/>
		</p:input>
		<p:with-param name="query" select="$query"/>
	</p:xslt>
	
</p:pipeline>
