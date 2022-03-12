<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:dtbook-to-html"
                name="main">
	
	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>One or more DTBook documents</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true"/>
	
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>HTML documents</p>
		</p:documentation>
		<p:pipe step="to-html" port="fileset.out"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="to-html" port="in-memory.out"/>
	</p:output>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>d:fileset</code> document that contains a mapping from input files (DTBook)
			to output files (HTML) and contained <code>id</code> attributes.</p>
		</p:documentation>
		<p:pipe step="mapping" port="result"/>
	</p:output>

	<p:option name="language" required="false" select="''"/>
	<p:option name="assert-valid" required="true"/>
	<p:option name="chunk" required="false" select="'false'"/>
	<p:option name="chunk-size" required="false" select="'-1'"/>
	<p:option name="filename" required="true"/>
	<p:option name="output-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-compose
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl">
		<p:documentation>
			px:dtbook-to-zedai
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl">
		<p:documentation>
			px:zedai-to-html
		</p:documentation>
	</p:import>
	
	<px:dtbook-to-zedai name="to-zedai" px:message="Converting DTBook to ZedAI" px:progress="1/2">
		<p:input port="in-memory.in">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
		<p:with-option name="opt-output-dir" select="concat($output-dir,'zedai/')"/>
		<p:with-option name="opt-zedai-filename" select="concat($filename,'.xml')"/>
		<p:with-option name="opt-lang" select="$language"/>
		<p:with-option name="opt-assert-valid" select="$assert-valid"/>
	</px:dtbook-to-zedai>

	<px:zedai-to-html name="to-html" px:message="Converting ZedAI to XHTML 5" px:progress="1/2">
		<p:input port="in-memory.in">
			<p:pipe step="to-zedai" port="in-memory.out"/>
		</p:input>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="chunk" select="$chunk"/>
		<p:with-option name="chunk-size" select="$chunk-size"/>
	</px:zedai-to-html>
	<p:sink/>

	<px:fileset-compose name="mapping">
		<p:input port="source">
			<p:pipe step="to-zedai" port="mapping"/>
			<p:pipe step="to-html" port="mapping"/>
		</p:input>
	</px:fileset-compose>
	<p:sink/>

</p:declare-step>
