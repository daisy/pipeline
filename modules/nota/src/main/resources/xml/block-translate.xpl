<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="pxi:block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            exclude-inline-prefixes="#all">

	<p:option name="text-transform" required="true"/>

	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>

	<css:parse-properties properties="display"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="block-translate.xsl"/>
		</p:input>
		<p:with-param name="text-transform" select="$text-transform"/>
	</p:xslt>

</p:pipeline>
