<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="px:odt-to-docx"
                name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>
	<p:option name="href" required="true"/>
	<p:output port="result"/>

	<p:option name="temp-dir" required="true"/>

	<p:import href="http://www.daisy.org/pipeline/modules/odf-utils/library.xpl">
		<p:documentation>
			px:odf-store
		</p:documentation>
	</p:import>

	<p:declare-step type="pxi:odt-to-docx">
		<p:option name="source"/>
		<p:option name="href"/>
		<p:option name="temp-dir"/>
		<p:output port="result"/>
		<!--
		    Implemented in ../java/OdtToDocxStep.java
		-->
	</p:declare-step>

	<px:odf-store name="odt-on-disk">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:with-option name="href" select="concat($temp-dir,'result.odt')"/>
	</px:odf-store>

	<pxi:odt-to-docx>
		<p:with-option name="source" select="string(/*)">
			<p:pipe step="odt-on-disk" port="result"/>
		</p:with-option>
		<p:with-option name="href" select="$href"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</pxi:odt-to-docx>

</p:declare-step>
