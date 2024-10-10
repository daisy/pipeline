<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:load">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Like <a
		href="https://www.w3.org/XML/XProc/docs/langspec.html#c.load"><code>p:load</code></a>,
		but also handles certain non-XML input when the optional
		<code>content-type</code> is specified.</p>
	</p:documentation>

	<p:option name="href" required="true" cx:type="xs:anyURI"/>

	<p:option name="content-type" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A mime type.</p>
		</p:documentation>
	</p:option>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The parsed document. When <code>content-type</code> is
			specified, it is used to select a special-purpose
			parser. Falls back to a standard XML parser.</p>
		</p:documentation>
	</p:output>

	<!--
	    Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/LoadProvider.java
	-->

</p:declare-step>
