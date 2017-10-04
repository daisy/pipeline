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
	
	<css:parse-properties properties="display"/>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="liblouis-block-translate.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
		<p:with-param name="no-wrap" select="$no-wrap"/>
		<p:with-param name="main-locale" select="$main-locale"/>
	</p:xslt>
	
</p:pipeline>
