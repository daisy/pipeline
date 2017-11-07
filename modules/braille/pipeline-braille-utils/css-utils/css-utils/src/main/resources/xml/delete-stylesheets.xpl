<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="css:delete-stylesheets"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-inline-prefixes="#all"
                name="main">
	
	<p:documentation>
		Delete CSS/SASS style sheets.
	</p:documentation>
	
	<p:input port="source">
		<p:documentation>
			CSS style sheets can be associated with the source in two ways: linked (using an
			'xml-stylesheet' processing instruction or a 'link' element) or embedded (using a
			'style' element).
		</p:documentation>
	</p:input>
	
	<p:output port="result">
		<p:documentation>
			All CSS style sheets associated with the source are deleted. Note that inlined styles
			(using a 'style' attribute) are not deleted.
		</p:documentation>
	</p:output>
	
	<!--
	    FIXME: This is needed in order to preserve the base-uri at the output. But why?
	-->
	<p:label-elements match="/*" attribute="xml:base" label="base-uri(/*)" replace="false"/>
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="delete-stylesheets.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	<p:delete match="/*/@xml:base"/>
	
</p:declare-step>
