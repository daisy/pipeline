<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:obfl-normalize-space"
                exclude-inline-prefixes="#all">

	<p:input port="source"/>
	<p:output port="result"/>

	<p:declare-step type="pxi:obfl-normalize-space-2">
		<p:input port="source"/>
		<p:output port="result"/>
		<!--
		    Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLNormalizeSpaceStep.java
		-->
	</p:declare-step>

	<p:xslt px:progress=".5">
		<p:input port="stylesheet">
			<p:document href="obfl-normalize-space.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

	<pxi:obfl-normalize-space-2 px:progress=".5"/>

</p:declare-step>
