<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">
	
	<p:option name="text-transform" select="''"/>
	<p:option name="braille-charset" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
	
	<css:parse-properties px:message="Parsing CSS properties" px:message-severity="DEBUG" px:progress=".05"
	                      properties="display"/>
	
	<p:xslt px:message="Translating CSS blocks" px:message-severity="DEBUG" px:progress=".95">
		<p:input port="stylesheet">
			<p:document href="block-translator-from-text-transform.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
		<p:with-param name="braille-charset" select="$braille-charset"/>
	</p:xslt>
	
</p:pipeline>
