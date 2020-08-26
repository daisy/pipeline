<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:epub3-add-prefix">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Add prefix declarations to the <code>prefix</code> attribute</p>
	</p:documentation>

	<p:input port="source">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>OPF or HTML document that may have a <code>prefix</code> (<code>epub:prefix</code>)
			attribute on the root element.</p>
		</p:documentation>
	</p:input>

	<p:option name="prefixes" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The prefix declarations to add</p>
		</p:documentation>
	</p:option>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result document with the new prefix declarations added to the <code>prefix</code>
			(<code>epub:prefix</code>) attribute. If no attribute existed yet, one is created. The
			mappings are merged in such a way that every prefix is unique and no two prefixes are
			mapped to the same URI.</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="add-prefix.xsl"/>
		</p:input>
		<p:with-param name="prefixes" select="$prefixes"/>
	</p:xslt>

</p:declare-step>
