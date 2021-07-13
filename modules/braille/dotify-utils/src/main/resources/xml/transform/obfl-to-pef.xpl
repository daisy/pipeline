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
	
	<p:option name="locale" required="true"/>
	<p:option name="mode" required="true"/>
	<p:input port="parameters" kind="parameter" primary="false"/>
	
	<p:import href="../obfl-normalize-space.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
	<p:import href="../library.xpl">
		<p:documentation>
			px:obfl-to-pef
		</p:documentation>
	</p:import>
	
	<!--
	    Follow the OBFL standard which says that "when volume-transition is present, the last page
	    or sheet in each volume may be modified so that the volume break occurs earlier than usual:
	    preferably between two blocks, or if that is not possible, between words"
	    (http://braillespecs.github.io/obfl/obfl-specification.html#L8701).  In other words, volumes
	    should by default not be allowed to end on a hyphen.
	-->
	<px:add-parameters name="allow-ending-volume-on-hyphen">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:with-param name="allow-ending-volume-on-hyphen"
		              select="if (/*/obfl:volume-transition) then 'false' else 'true'">
			<p:pipe step="main" port="source"/>
		</p:with-param>
	</px:add-parameters>
	<px:merge-parameters name="parameters">
		<p:input port="source">
			<p:pipe step="allow-ending-volume-on-hyphen" port="result"/>
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</px:merge-parameters>
	
	<pxi:obfl-normalize-space px:progress=".10">
		<p:input port="source">
			<p:pipe step="main" port="source"/>
		</p:input>
	</pxi:obfl-normalize-space>
	
	<px:obfl-to-pef px:progress=".90">
		<p:with-option name="locale" select="$locale"/>
		<p:with-option name="mode" select="$mode"/>
		<p:input port="parameters">
			<p:pipe step="parameters" port="result"/>
		</p:input>
	</px:obfl-to-pef>
	
</p:declare-step>
