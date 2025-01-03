<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dotify-obfl-to-pef" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
            xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
            exclude-inline-prefixes="#all"
            name="main">

	<p:input port="source"/>
	<p:output port="result"/>

	<p:input port="parameters" kind="parameter" primary="false"/>

	<p:import href="../obfl-normalize-space.xpl">
		<p:documentation>
			pxi:obfl-normalize-space
		</p:documentation>
	</p:import>
	<p:import href="../library.xpl">
		<p:documentation>
			px:obfl-to-pef
		</p:documentation>
	</p:import>

	<pxi:obfl-normalize-space px:progress=".10">
		<!-- This has already been done (in dotify-transform.xpl), but do it again just in case the
		     OBFL has been edited. -->
	</pxi:obfl-normalize-space>

	<px:obfl-to-pef px:progress=".90">
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</px:obfl-to-pef>

</p:declare-step>
