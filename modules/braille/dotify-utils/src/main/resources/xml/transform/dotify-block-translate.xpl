<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:dotify-block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">
	
	<p:option name="text-transform" select="''"/>
	<p:option name="no-wrap" select="'false'"/>
	<p:option name="main-locale" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
	
	<css:parse-properties px:message="Parsing CSS properties" px:progress="1/2"
	                      properties="display"/>
	
	<p:xslt px:message="Translating Dotify blocks" px:progress="1/2">
		<p:input port="stylesheet">
			<p:document href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-from-text-transform.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="concat('(translator:dotify)',$text-transform)"/>
		<p:with-param name="no-wrap" select="$no-wrap"/>
		<p:with-param name="main-locale" select="$main-locale"/>
	</p:xslt>
	
</p:pipeline>
