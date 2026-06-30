<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc-internal"
                type="pxi:epub3-ocf-container-update-links"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Update the links in a EPUB container file after resources have been relocated.</p>
	</p:documentation>
	
	<p:input port="source" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input container file</p>
		</p:documentation>
	</p:input>
	
	<p:input port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that defines the relocation of resources.</p>
		</p:documentation>
	</p:input>
	
	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The output container file with updated links</p>
		</p:documentation>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-join
		</p:documentation>
	</p:import>
	
	<!-- normalize mapping document -->
	<px:fileset-join name="mapping">
		<p:input port="source">
			<p:pipe step="main" port="mapping"/>
		</p:input>
	</px:fileset-join>
	<p:sink/>
	
	<p:xslt>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
			<p:pipe step="mapping" port="result"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="container-update-links.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
