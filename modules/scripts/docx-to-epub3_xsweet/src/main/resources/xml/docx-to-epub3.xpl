<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:docx-to-epub3" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xsw="http://coko.foundation/xsweet"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all">
	
	<p:option name="docx" required="true" px:type="anyFileURI" px:sequence="false"
	          px:media-type="application/vnd.openxmlformats-officedocument.wordprocessingml.document"/>
	
	<p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">EPUB</h2>
			<p px:role="desc">The resulting EPUB 3 publication.</p>
		</p:documentation>
	</p:option>
	
	<p:import href="http://coko.foundation/xsweet/library.xpl">
		<p:documentation>
			xsw:docx-to-html5
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:normalize-uri
			px:set-base-uri
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
		<p:documentation>
			px:html-to-fileset
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/html-to-epub3/library.xpl">
		<p:documentation>
			px:html-to-epub3
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl">
		<p:documentation>
			px:epub3-store
		</p:documentation>
	</p:import>
	
	<px:normalize-uri name="output-dir-uri">
		<p:with-option name="href" select="concat($output-dir,'/')"/>
	</px:normalize-uri>
	<p:sink/>
	
	<p:group>
		<p:variable name="output-dir-uri" select="/c:result/string()">
			<p:pipe port="normalized" step="output-dir-uri"/>
		</p:variable>
		<p:variable name="epub-file-uri"
		            select="concat($output-dir-uri, replace($docx,'^.*/([^/]*)\.[^/\.]*$','$1.epub'))"/>
        <p:variable name="html-file-uri"
                    select="concat($output-dir-uri, replace($docx,'^.*/([^/]*)\.[^/\.]*$','$1.xhtml'))"/>
        <p:variable name="fileset1-file-uri"
                    select="concat($output-dir-uri, replace($docx,'^.*/([^/]*)\.[^/\.]*$','$1_1.xml'))"/>
         <p:variable name="fileset2-file-uri"
                    select="concat($output-dir-uri, replace($docx,'^.*/([^/]*)\.[^/\.]*$','$1_2.xml'))"/>
		
		<p:group name="html">
			<p:output port="result">
				<p:pipe port="result" step="rebase" />
			</p:output>
			<xsw:docx-to-html5>
				<p:with-option name="docx-file-uri" select="$docx"/>
			</xsw:docx-to-html5>
			<px:set-base-uri name="rebase">
				<p:documentation>
					xsw:docx-to-html5 may return "jar:file:" URIs which Pipeline does not support at the
					moment.
				</p:documentation>
				<p:with-option name="base-uri" select="replace(base-uri(/*),'^jar:file:','file:')"/>
			</px:set-base-uri>
			<p:store>
                <p:with-option name="href" select="$html-file-uri"/>
            </p:store>
		</p:group>
		
		
		<px:html-to-fileset name="html-fileset"/>
        <p:store>
            <p:with-option name="href" select="$fileset1-file-uri"/>
        </p:store>

		
		<px:html-to-epub3 name="epub3">
			<p:input port="input.fileset">
                <p:pipe port="fileset.out" step="html-fileset" />
            </p:input>
			<p:input port="input.in-memory">
				<p:pipe step="html" port="result"/>
			</p:input>
			<p:with-option name="output-dir" select="$output-dir-uri"/>
		</px:html-to-epub3>
		<p:store>
            <p:with-option name="href" select="$fileset2-file-uri"/>
        </p:store>

		<!-- <px:epub3-store>
			<p:with-option name="href" select="$epub-file-uri"/>
			<p:input port="in-memory.in">
				<p:pipe step="epub3" port="in-memory.out"/>
			</p:input>
		</px:epub3-store> -->
	</p:group>
	
</p:declare-step>
