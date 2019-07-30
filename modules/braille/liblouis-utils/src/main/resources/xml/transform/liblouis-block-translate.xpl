<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:liblouis-block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">
	
	<p:option name="text-transform" select="''"/>
	<p:option name="no-wrap" select="'false'"/>
	<p:option name="main-locale" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
	
	<css:parse-properties px:progress=".05"
	                      properties="display"/>
	
	<!--
	    Suppress warning message "The source document is in namespace foo, but none of the template
	    rules match elements in this namespace"
	-->
	<p:wrap wrapper="css:wrapper" match="/*"/>
	
	<p:xslt px:message="Translating with Liblouis" px:progress=".95">
		<p:input port="stylesheet">
			<p:document href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-from-text-transform.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
		<p:with-param name="no-wrap" select="$no-wrap"/>
		<p:with-param name="main-locale" select="$main-locale"/>
	</p:xslt>
	
</p:pipeline>
