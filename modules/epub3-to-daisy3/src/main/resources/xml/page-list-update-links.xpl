<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:page-list-update-links" name="main">

	<p:documentation>
		Update "page-list" d:fileset document according to provided mapping.
	</p:documentation>

	<p:input port="source" primary="true"/>
	<p:input port="mapping"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="main" port="mapping"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="page-list-update-links.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
