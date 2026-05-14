<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ocf="urn:oasis:names:tc:opendocument:xmlns:container"
                type="px:epub-load" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Create a fileset from a zipped or unzipped EPUB.</p>
	</p:documentation>
	
	<p:option name="href" required="true" px:media-type="application/epub+zip application/oebps-package+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A zipped EPUB (.epub file), an EPUB package document (.opf file) or the mimetype file
			of an EPUB.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="version" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>EPUB version: "2" or "3"</p>
			<p>If not specified, the version will be detected automatically.</p>
		</p:documentation>
	</p:option>
	<p:option name="store-to-disk" required="false" select="'false'" cx:as="xs:string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Ensure that all files from the result fileset exist on disk. It is an error if this
			option is true and the value of the <code>temp-dir</code> option does not identify a
			directory.</p>
		</p:documentation>
	</p:option>
	<p:option name="validation" cx:as="xs:boolean" select="false()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to perform validation of the input.</p>
		</p:documentation>
	</p:option>
	<p:option name="temp-dir" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Empty directory dedicated to this step. Mandatory when <code>store-to-disk</code>
			option is 'true' or when <code>validation</code> option is not 'off'.</p>
		</p:documentation>
	</p:option>
	
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result fileset. If a .opf file was specified for the <code>href</code> option,
			the fileset does not contain the "mimetype" and "META-INF/container.xml" files. The
			navigation document is marked with a <code>role="nav"</code> attribute.</p>
		</p:documentation>
		<p:pipe step="result" port="result.fileset"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="result" port="result.in-memory"/>
	</p:output>
	<p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The validation report</p>
			<p>The port is empty if the <code>validation</code> option is set to 'false' or if the
			input is a valid EPUB.</p>
		</p:documentation>
		<p:pipe step="validate" port="report"/>
	</p:output>
	<p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The <a href="http://daisy.github.io/pipeline/StatusXML">validation
			status</a> document</p>
			<p>'ok' if the input is a valid EPUB, 'error' otherwise.</p>
		</p:documentation>
		<p:pipe step="validate" port="status"/>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:message
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:fileset-create
			px:fileset-unzip
			px:fileset-from-dir
			px:fileset-add-entry
			px:fileset-add-entries
			px:fileset-join
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:unzip
		</p:documentation>
	</p:import>
	<p:import href="../validate/epub-validate.xpl">
		<p:documentation>
			px:epub-validate
		</p:documentation>
	</p:import>
	<p:import href="opf-manifest-to-fileset.xpl">
		<p:documentation>
			pxi:opf-manifest-to-fileset
		</p:documentation>
	</p:import>
	
	<px:assert message="When store-to-disk='true' then temp-dir must also be defined" error-code="PZU001" name="check-temp-dir">
		<p:with-option name="test" select="$store-to-disk='false' or p:value-available('temp-dir')"/>
	</px:assert>
	
	<px:assert message="Input must either be a .epub file, a .opf file or a file named 'mimetype', but got '$1'."
	           error-code="XXXXX" name="check-href">
		<p:with-option name="test" select="matches(lower-case($href),'.+\.(epub|opf)$|.*/mimetype$')"/>
		<p:with-option name="param1" select="$href"/>
	</px:assert>
	
	<p:choose name="result" cx:depends-on="check-href">
		<p:when test="ends-with(lower-case($href),'.opf')">
			<p:output port="result.fileset" primary="true"/>
			<p:output port="result.in-memory" sequence="true">
				<!--
				    other files are loaded lazily
				-->
				<p:pipe step="package-document" port="result"/>
			</p:output>
			
			<!-- get package file -->
			<p:load name="package-document">
				<p:with-option name="href" select="$href"/>
			</p:load>
			
			<!-- convert manifest -->
			<pxi:opf-manifest-to-fileset/>
			
		</p:when>
		<p:otherwise>
			<p:output port="result.fileset" primary="true"/>
			<p:output port="result.in-memory" sequence="true">
				<!--
				    other files are loaded lazily
				-->
				<p:pipe step="package-documents" port="result"/>
			</p:output>
			
			<!-- get container file -->
			<p:choose>
				<p:when test="ends-with(lower-case($href),'.epub')">
					<px:unzip file="META-INF/container.xml" content-type="application/xml">
						<p:with-option name="href" select="$href"/>
					</px:unzip>
				</p:when>
				<p:otherwise> <!-- mimetype -->
					<p:load>
						<p:with-option name="href" select="resolve-uri('META-INF/container.xml',$href)"/>
					</p:load>
				</p:otherwise>
			</p:choose>
			<p:identity name="container"/>
			<p:sink/>
			
			<!-- convert zip/dir manifest -->
			<p:choose>
				<p:when test="ends-with(lower-case($href),'.epub')">
					<p:choose>
						<p:when test="$store-to-disk='true'">
							<p:output port="result"/>
							<px:fileset-unzip store-to-disk="true" cx:depends-on="check-temp-dir">
								<p:with-option name="href" select="$href"/>
								<p:with-option name="unzipped-basedir" select="concat($temp-dir,'unzip/')"/>
							</px:fileset-unzip>
						</p:when>
						<p:otherwise>
							<p:output port="result"/>
							<px:fileset-unzip>
								<p:with-option name="href" select="$href"/>
							</px:fileset-unzip>
							<p:add-xml-base/>
						</p:otherwise>
					</p:choose>
				</p:when>
				<p:otherwise>
					<px:fileset-from-dir>
						<p:with-option name="path" select="resolve-uri('./',$href)"/>
					</px:fileset-from-dir>
				</p:otherwise>
			</p:choose>
			<p:identity name="fileset-from-zip-or-dir-without-mediatype"/>
			<p:delete match="//d:file" name="epub-base"/>
			<px:fileset-add-entries>
				<p:with-option name="href" select="//d:file/@href/string(.)">
					<p:pipe step="fileset-from-zip-or-dir-without-mediatype" port="result"/>
				</p:with-option>
			</px:fileset-add-entries>
			<px:fileset-add-entry href="META-INF/container.xml" media-type="application/xml" replace-attributes="true"/>
			<p:group>
				<px:fileset-add-entries media-type="application/oebps-package+xml" replace-attributes="true">
					<p:with-option name="href" select="//ocf:rootfile/@full-path/string(.)">
						<p:pipe step="container" port="result"/>
					</p:with-option>
				</px:fileset-add-entries>
			</p:group>
			<px:fileset-join name="fileset-from-zip-or-dir"/>
			<p:sink/>
			
			<!-- get package document(s) -->
			<p:for-each name="package-documents">
				<p:iteration-source select="//ocf:rootfile">
					<p:pipe step="container" port="result"/>
				</p:iteration-source>
				<p:output port="fileset" primary="true"/>
				<p:output port="result">
					<p:pipe step="package-document" port="result"/>
				</p:output>
				<p:variable name="full-path" select="/*/@full-path"/>
				<px:fileset-load name="package-document">
					<p:input port="fileset">
						<p:pipe step="fileset-from-zip-or-dir" port="result"/>
					</p:input>
					<p:input port="in-memory">
						<p:empty/>
					</p:input>
					<p:with-option name="href" select="resolve-uri($full-path,base-uri(/*))">
						<p:pipe step="epub-base" port="result"/>
					</p:with-option>
				</px:fileset-load>
				
				<!-- convert manifest -->
				<pxi:opf-manifest-to-fileset/>
				
			</p:for-each>
			<px:fileset-join name="filesets-from-package-documents"/>
			<p:sink/>
			
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="fileset-from-zip-or-dir" port="result"/>
					<p:pipe step="filesets-from-package-documents" port="result"/>
				</p:input>
			</px:fileset-join>
			<px:fileset-add-entry href="mimetype" first="true" replace="true">
				<p:with-param port="file-attributes" name="compression-method" select="'stored'"/>
			</px:fileset-add-entry>
			
		</p:otherwise>
	</p:choose>
	
	<p:group name="version">
		<p:output port="result"/>
		<p:variable name="opf-version" select="//d:file[@media-type='application/oebps-package+xml'][1]/@media-version"/>
		<p:choose>
			<p:when test="p:value-available('version')">
				<px:assert message="Version must be '2' or '3', but got '$1'" error-code="XXXXX">
					<p:with-option name="test" select="$version=('2','3')"/>
				</px:assert>
				<px:assert message="Specified version ($1) is not equal to detected version ($2)">
					<p:with-option name="test" select="$opf-version=concat($version,'.0')"/>
					<p:with-option name="param1" select="$version"/>
					<p:with-option name="param2" select="$opf-version"/>
				</px:assert>
				<p:template>
					<p:input port="template">
						<p:inline>
							<c:result>{$version}</c:result>
						</p:inline>
					</p:input>
					<p:with-param name="version" select="$version"/>
				</p:template>
			</p:when>
			<p:otherwise>
				<px:assert message="Could not detect version: unexpected version attribute found in package document ($1)"
				           error-code="XXXX">
					<p:with-option name="test" select="$opf-version=('2.0','3.0')"/>
					<p:with-option name="param1" select="$opf-version"/>
				</px:assert>
				<p:template>
					<p:input port="template">
						<p:inline>
							<c:result>{$version}</c:result>
						</p:inline>
					</p:input>
					<p:with-param name="version" select="substring($opf-version,1,1)"/>
				</p:template>
			</p:otherwise>
		</p:choose>
	</p:group>
	<p:sink/>

	<p:choose name="validate">
		<p:when test="not($validation)">
			<p:output port="report" sequence="true">
				<p:empty/>
			</p:output>
			<p:output port="status">
				<p:inline>
					<d:validation-status result="ok"/>
				</p:inline>
			</p:output>
			<p:sink>
				<p:input port="source">
					<p:empty/>
				</p:input>
			</p:sink>
		</p:when>
		<p:otherwise>
			<p:output port="report" sequence="true">
				<p:pipe step="status-and-report" port="report"/>
			</p:output>
			<p:output port="status">
				<p:pipe step="status-and-report" port="status"/>
			</p:output>
			<px:epub-validate name="epub3-validator">
				<!--
					epub option must point to a file that exists on disk (and may not be a file inside a ZIP)
				-->
				<p:with-option name="epub" select="$href">
					<p:pipe step="result" port="result.fileset"/>
				</p:with-option>
				<p:with-option name="version" select="string(/*)">
					<p:pipe step="version" port="result"/>
				</p:with-option>
				<p:with-option name="temp-dir" select="concat($temp-dir,'validate/')"/>
			</px:epub-validate>
			<p:identity>
				<p:input port="source">
					<p:pipe step="epub3-validator" port="validation-status"/>
				</p:input>
			</p:identity>
			<p:choose name="status-and-report">
				<p:when test="/d:validation-status[@result='ok']">
					<p:output port="status" primary="true"/>
					<p:output port="report" sequence="true">
						<p:empty/>
					</p:output>
					<p:identity/>
				</p:when>
				<p:otherwise>
					<p:output port="status" primary="true"/>
					<p:output port="report">
						<p:pipe step="epub3-validator" port="html-report"/>
					</p:output>
					<p:identity/>
				</p:otherwise>
			</p:choose>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
