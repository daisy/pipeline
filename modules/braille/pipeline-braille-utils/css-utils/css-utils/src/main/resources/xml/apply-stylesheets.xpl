<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="css:apply-stylesheets"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="px css"
                name="main">
	
	<p:documentation>
		Apply CSS/SASS stylesheets.
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation>
			Style sheets can be associated with the source in several ways: linked (using an
			'xml-stylesheet' processing instruction or a 'link' element), embedded (using a 'style'
			element) and/or inlined (using 'style' attributes).
		</p:documentation>
	</p:input>
	
	<p:input port="context" sequence="true">
		<p:documentation>
			Style sheets that are linked to from the source document, or included via the
			'stylesheets' option, must either exist on disk, or must be provided in memory via this
			port. Style sheets on this port must be wrapped in &lt;c:result
			content-type="text/plain"&gt; elements. Style sheet URIs are resolved by matching
			against the context documents's base URIs.
		</p:documentation>
		<p:empty/>
	</p:input>
	
	<p:output port="result">
		<p:documentation>
			Style sheets are applied by "inlining" them, i.e. performing the cascade and capturing
			the styles of individual elements in 'style' attributes. All CSS/SASS style sheets are
			applied at once, but the order in which they are specified (first the ones from the
			'stylesheets' option, then the ones associated with the source document) has an
			influence on the cascading order.
		</p:documentation>
	</p:output>
	
	<p:option name="stylesheets" required="false" select="''">
		<p:documentation>
			A space separated list of URIs, absolute or relative to source.
		</p:documentation>
	</p:option>
	
	<p:import href="library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
	
	<px:parse-xml-stylesheet-instructions name="xml-stylesheet-instructions"/>
	<p:sink/>
	
	<css:inline>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
		</p:input>
		<p:input port="context">
			<p:pipe step="main" port="context"/>
		</p:input>
		<p:with-option name="default-stylesheet"
		               select="string-join((
		                         $stylesheets,
		                         /d:xml-stylesheets/d:xml-stylesheet[@type='text/css' or (not(@type[not(.='text/css')]) and matches(@href,'\.s?css$'))]
		                         /@href),' ')">
			<p:pipe step="xml-stylesheet-instructions" port="result"/>
		</p:with-option>
	</css:inline>
	
</p:declare-step>
