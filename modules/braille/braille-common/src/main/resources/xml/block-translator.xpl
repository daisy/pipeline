<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            exclude-inline-prefixes="#all"
            type="px:block-translate"
            name="main">
	
	<p:option name="text-transform" select="''"/>
	<p:option name="braille-charset" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl">
		<p:documentation>
			px:css-parse-properties
		</p:documentation>
	</p:import>
	
	<p:xslt px:progress="0.05">
		<p:documentation>
			Make css:before and css:after elements from pseudo-element rules.
		</p:documentation>
		<p:input port="stylesheet">
			<p:document href="expand-pseudo-elements.xsl"/>
		</p:input>
	</p:xslt>
	
	<px:css-parse-properties px:message="Parsing CSS properties" px:message-severity="DEBUG" px:progress=".05"
	                         properties="display"/>
	
	<p:xslt px:message="Translating CSS blocks" px:message-severity="DEBUG" px:progress=".85">
		<p:input port="stylesheet">
			<p:document href="block-translator-from-text-transform.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
		<p:with-param name="braille-charset" select="$braille-charset"/>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</p:xslt>
	
	<p:xslt px:progress="0.05">
		<p:documentation>
			Convert css:before and css:after elements back to pseudo-element rules.
		</p:documentation>
		<p:input port="stylesheet">
			<p:document href="collapse-pseudo-elements.xsl"/>
		</p:input>
	</p:xslt>
	
</p:pipeline>
