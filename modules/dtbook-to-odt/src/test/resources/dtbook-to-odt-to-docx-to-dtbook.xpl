<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                type="pxi:dtbook-to-odt-to-docx-to-dtbook"
                name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="dtbook-2" port="result.in-memory"/>
	</p:output>

	<p:option name="template" required="true"/>
	<p:option name="output-dir" required="true"/>
	<p:option name="temp-dir" required="true"/>

	<p:import href="../../main/resources/xml/dtbook-to-odt.convert.xpl">
		<p:documentation>
			px:dtbook-to-odt
		</p:documentation>
	</p:import>
	<p:import href="odt-to-docx.xpl">
		<p:documentation>
			px:odt-to-docx
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/word-to-dtbook/library.xpl">
		<p:documentation>
			px:word-to-dtbook
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
		</p:documentation>
	</p:import>

	<p:variable name="file-name" select="//d:file[@media-type='application/x-dtbook+xml']
	                                     /replace(@href,'^.*/([^/]*)\.[^/\.]*$','$1')"/>

	<px:fileset-load media-types="application/x-dtbook+xml" name="dtbook-1">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>

	<px:dtbook-to-odt name="odt">
		<p:input port="fileset.in">
			<p:pipe step="dtbook-1" port="unfiltered.fileset"/>
		</p:input>
		<p:input port="in-memory.in">
			<p:pipe step="dtbook-1" port="unfiltered.in-memory"/>
		</p:input>
		<p:input port="content.xsl">
			<p:document href="../../main/resources/xml/content.xsl"/>
		</p:input>
		<p:input port="meta">
			<p:empty/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
		<p:with-option name="page-numbers" select="true()"/>
		<p:with-option name="daisy-styles" select="true()"/>
		<p:with-option name="template" select="$template"/>
		<p:with-option name="asciimath" select="'ASCIIMATH'"/>
		<p:with-option name="images" select="'EMBED'"/>
		<p:with-option name="temp-dir" select="concat($temp-dir,'dtbook-to-odt/')"/>
	</px:dtbook-to-odt>

	<px:odt-to-docx name="docx">
		<p:input port="source.in-memory">
			<p:pipe step="odt" port="in-memory.out"/>
		</p:input>
		<p:with-option name="href" select="concat($temp-dir,$file-name,'.docx')"/>
		<p:with-option name="temp-dir" select="concat($temp-dir,'odt-to-docx/')"/>
	</px:odt-to-docx>

	<px:word-to-dtbook name="dtbook-2">
		<p:with-option name="source" select="string(/*)">
			<p:pipe step="docx" port="result"/>
		</p:with-option>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="temp-dir" select="concat($temp-dir,'word-to-dtbook/')"/>
		<!-- preserve uid (don't generate new one) -->
		<p:with-option name="uid" select="//dtb:head/dtb:meta[@name='dtb:uid']/@content">
			<p:pipe step="dtbook-1" port="result"/>
		</p:with-option>
		<p:with-option name="tidy" select="true()"/>
		<p:with-option name="repair" select="true()"/>
	</px:word-to-dtbook>

</p:declare-step>
