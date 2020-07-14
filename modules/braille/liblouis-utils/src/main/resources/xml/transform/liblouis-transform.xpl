<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:c="http://www.w3.org/ns/xproc-step"
            xmlns:louis="http://liblouis.org/liblouis"
            type="px:liblouis-transform" name="main"
            exclude-inline-prefixes="#all">
	
	<p:option name="block-transform" select="''"/>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
		<p:documentation>
			px:transform
		</p:documentation>
	</p:import>
	<p:import href="../format.xpl">
		<p:documentation>
			louis:format
		</p:documentation>
	</p:import>
	
	<px:assert message="'temp-dir' parameter is required">
		<p:with-option name="test" select="exists(//c:param[@name='temp-dir' and not(@namespace[not(.='')])])">
			<p:pipe step="main" port="parameters"/>
		</p:with-option>
	</px:assert>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="handle-list-item.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
	<px:transform>
		<p:with-option name="query" select="$block-transform"/>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</px:transform>
	
	<louis:format>
		<p:with-option name="temp-dir" select="//c:param[@name='temp-dir' and not(@namespace[not(.='')])][1]/@value">
			<p:pipe step="main" port="parameters"/>
		</p:with-option>
	</louis:format>
	
</p:pipeline>
