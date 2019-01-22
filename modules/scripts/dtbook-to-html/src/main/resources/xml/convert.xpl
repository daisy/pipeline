<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:dtbook-to-html"
                name="main">
	
	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true"/>
	
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="to-html" port="in-memory.out"/>
	</p:output>
	
	<p:option name="language" required="true"/>
	<p:option name="assert-valid" required="true"/>
	<p:option name="chunk-size" required="false" select="'-1'"/>
	<p:option name="filename" required="true"/>
	<p:option name="output-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl"/>
	
	<px:message message="Converting to ZedAI..."/>
	<px:dtbook-to-zedai name="to-zedai">
		<p:input port="in-memory.in">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:with-option name="opt-output-dir" select="concat($output-dir,'zedai/')"/>
		<p:with-option name="opt-zedai-filename" select="concat($filename,'.xml')"/>
		<p:with-option name="opt-lang" select="$language"/>
		<p:with-option name="opt-assert-valid" select="$assert-valid"/>
	</px:dtbook-to-zedai>

	<px:message message="Converting to XHTML5..."/>
	<px:zedai-to-html name="to-html">
		<p:input port="in-memory.in">
			<p:pipe step="to-zedai" port="in-memory.out"/>
		</p:input>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="chunk-size" select="$chunk-size"/>
	</px:zedai-to-html>

</p:declare-step>
