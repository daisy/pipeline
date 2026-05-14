<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:sass-compile"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Compile Sass files to CSS.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input fileset containing the Sass files to be compiled (marked with
			<code>media-type="text/x-scss"</code>).</p>
			<p>Only the "SCSS" syntax is supported, and only Sass files not starting with "_" are
			compiled.</p>
			<p>The fileset will also be used for loading other resources. If files are present in memory, they
			are expected to be <code>c:data</code> documents. Only when files are not present in
			this fileset, it will be attempted to load them from disk.</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>

	<p:option name="parameters" select="map{}"> <!-- map(xs:string,item()) -->
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Global Sass variables.</p>
		</p:documentation>
	</p:option>

	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset containing all the CSS files that are a result of the compilation, together
			with all other files that were present in the input fileset, but except Sass files.</p>
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="result" port="result.in-memory"/>
	</p:output>

	<p:declare-step type="pxi:sass-compile">
		<p:input port="source" primary="true"/>
		<p:input port="context" sequence="true"/>
		<p:output port="result" sequence="true"/>
		<p:option name="parameters"/>
		<!--
		    Implemented in ../../java/org/daisy/pipeline/css/calabash/impl/SassCompileStep.java
		-->
	</p:declare-step>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-filter
			px:fileset-load
			px:fileset-add-entries
		</p:documentation>
	</p:import>

	<px:fileset-filter media-types="text/x-scss" name="sass">
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-filter>
	<p:add-attribute match="//d:file" attribute-name="method" attribute-value="binary">
		<p:documentation>Force base64 because CSS may not be encoded as UTF-8 (and fileset-load does
		not detect this)</p:documentation>
	</p:add-attribute>
	<px:fileset-load name="context">
		<p:input port="in-memory">
			<p:pipe step="sass" port="result.in-memory"/>
		</p:input>
	</px:fileset-load>

	<p:split-sequence test="not(starts-with(tokenize(base-uri(/),'/')[last()],'_'))"/>
	<p:for-each>
		<pxi:sass-compile>
			<p:input port="context">
				<p:pipe step="context" port="result"/>
			</p:input>
			<p:with-option name="parameters" select="$parameters"/>
		</pxi:sass-compile>
	</p:for-each>
	<p:identity name="css"/>
	<p:sink/>

	<px:fileset-add-entries media-type="text/css" name="result">
		<p:input port="source.fileset">
			<p:pipe step="sass" port="not-matched"/>
		</p:input>
		<p:input port="source.in-memory">
			<p:pipe step="sass" port="not-matched.in-memory"/>
		</p:input>
		<p:input port="entries">
			<p:pipe step="css" port="result"/>
		</p:input>
	</px:fileset-add-entries>

</p:declare-step>
