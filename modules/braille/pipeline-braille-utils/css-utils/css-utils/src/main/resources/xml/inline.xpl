<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0"
                type="css:inline"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-inline-prefixes="#all">
	
	<p:documentation>
		CSS cascading and inlining according to
		http://snaekobbi.github.io/braille-css-spec/#h2_cascade and
		http://snaekobbi.github.io/braille-css-spec/#h2_style-attribute.
	</p:documentation>
	
	<p:input port="source" sequence="false" primary="true">
		<p:documentation>
			Style sheets can be attached to the source in several ways: linked (using the 'link'
			element), embedded (using the 'style' element) and/or inlined (using 'style'
			attributes).
		</p:documentation>
	</p:input>
	
	<p:input port="sass-variables" kind="parameter" primary="false"/>
	
	<p:output port="result" sequence="false" primary="true">
		<p:documentation>
			All styles are parsed, validated, normalized, cascaded and finally serialized into
			'style' attributes. 'page' rules are serialized as '@page { ... } @page:left { ... }
			@page:right { ... }'. 'before' and 'after' rules are serialized as '::before { ... }'
			and '::after { ... }'. Shorthand declarations such as 'margin: a b c d' are decomposed
			as 'margin-top: a; margin-right: b; margin-bottom: c; margin-left: d'.
		</p:documentation>
	</p:output>
	
	<p:option name="default-stylesheet" required="false">
		<p:documentation>
			Space separated list of URIs, absolute or relative to source. Applied prior to all other
			style sheets defined within the source.
		</p:documentation>
	</p:option>
	
</p:declare-step>
