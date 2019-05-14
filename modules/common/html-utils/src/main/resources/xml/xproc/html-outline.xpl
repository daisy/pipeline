<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:html-outline"
                version="1.0">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p px:role="desc">Return an outline (ol element) from an (x)html page</p>
	</p:documentation>
	
	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Input (X)HTML stream</h2>
			<p px:role="desc">The (X)HTML document from which the outline must be extracted</p>
		</p:documentation>
	</p:input>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Outline</h2>
			<p px:role="desc">This steps output the outline of the html as a hierarchy of ordered lists</p>
		</p:documentation>
	</p:output>

	<p:option name="output-base-uri" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI of the resulting outline.</p>
		</p:documentation>
	</p:option>

	<p:xslt>
		<p:documentation>Creating the outline</p:documentation>
		<p:input port="stylesheet">
			<p:document href="../xslt/html5-outliner.xsl"/>
		</p:input>
		<p:with-param name="output-base-uri" select="$output-base-uri"/>
		<p:with-option name="output-base-uri" select="$output-base-uri"/>
	</p:xslt>

</p:declare-step>