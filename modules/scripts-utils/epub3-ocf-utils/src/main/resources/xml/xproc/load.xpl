<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:ocf="urn:oasis:names:tc:opendocument:xmlns:container"
                type="px:epub3-load" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Create a fileset from a zipped or unzipped EPUB 3.</p>
	</p:documentation>
	
	<p:option name="href" required="true" px:media-type="application/epub+zip application/oebps-package+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A zipped EPUB (.epub file), an EPUB package document (.opf file) or the mimetype file
			of an EPUB.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="store-to-disk" required="false" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Ensure that all files from the result fileset exist on disk. It is an error if this
			option is true and the value of the <code>temp-dir</code> option does not identify a
			directory.</p>
		</p:documentation>
		<!--
		    Note that setting this option to true is currently more memory efficient in case the
		    fileset is later stored, because the current implementation of px:fileset-store
		    loads all zipped files into memory before storing them to a location on disk or
		    zipping them up again, which can be slow when we're dealing with a lot of big files.
		-->
	</p:option>
	<p:option name="temp-dir" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Empty directory dedicated to this step. Mandatory when <code>store-to-disk</code>
			option is true.</p>
		</p:documentation>
	</p:option>
	
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The result fileset. If a .opf file was specified for the <code>href</code> option,
			the fileset does not contain the "mimetype" and "META-INF/container.xml" files.</p>
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="result" port="result.in-memory"/>
	</p:output>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:error
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:set-base-uri
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:fileset-create
			px:fileset-unzip
			px:fileset-from-dir
			px:fileset-add-entry
			px:fileset-join
			px:fileset-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			px:unzip
		</p:documentation>
	</p:import>
	<p:import href="opf-manifest-to-fileset.xpl"/>
	
	<px:assert message="When store-to-disk='true' then temp-dir must also be defined" error-code="PZU001">
		<p:with-option name="test" select="$store-to-disk='false' or p:value-available('temp-dir')"/>
	</px:assert>
	
	<px:assert message="Input must either be a .epub file, a .opf file or a file named 'mimetype', but got '$1'."
	           error-code="XXXXX">
		<p:with-option name="test" select="matches(lower-case($href),'.+\.(epub|opf)$|.*/mimetype$')"/>
		<p:with-option name="param1" select="$href"/>
	</px:assert>
	
	<p:choose name="result">
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
			<px:opf-manifest-to-fileset/>
			
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
							<p:output port="result">
								<p:pipe step="unzip" port="fileset"/>
							</p:output>
							<px:fileset-unzip store-to-disk="true" name="unzip">
								<p:with-option name="href" select="$href"/>
								<p:with-option name="unzipped-basedir" select="concat($temp-dir,'unzip/')"/>
							</px:fileset-unzip>
							<p:sink/>
						</p:when>
						<p:otherwise>
							<p:output port="result"/>
							<px:fileset-unzip name="unzip">
								<p:with-option name="href" select="$href"/>
							</px:fileset-unzip>
							<p:sink/>
							<px:set-base-uri>
								<p:input port="source">
									<p:pipe step="unzip" port="fileset"/>
								</p:input>
								<p:with-option name="base-uri" select="concat($href,'!/')"/>
							</px:set-base-uri>
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
			<p:sink/>
			<p:for-each>
				<p:iteration-source select="//d:file">
					<p:pipe step="fileset-from-zip-or-dir-without-mediatype" port="result"/>
				</p:iteration-source>
				<p:variable name="href" select="/*/@href"/>
				<p:identity>
					<p:input port="source">
						<p:pipe step="epub-base" port="result"/>
					</p:input>
				</p:identity>
				<p:choose>
					<p:xpath-context>
						<p:pipe step="container" port="result"/>
					</p:xpath-context>
					<p:when test="$href='META-INF/container.xml'">
						<px:fileset-add-entry media-type="application/xml">
							<p:with-option name="href" select="$href"/>
						</px:fileset-add-entry>
					</p:when>
					<p:when test="//ocf:rootfile[@full-path=$href]">
						<px:fileset-add-entry media-type="application/oebps-package+xml">
							<p:with-option name="href" select="$href"/>
						</px:fileset-add-entry>
					</p:when>
					<p:otherwise>
						<px:fileset-add-entry>
							<p:with-option name="href" select="$href"/>
						</px:fileset-add-entry>
					</p:otherwise>
				</p:choose>
			</p:for-each>
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
				<px:opf-manifest-to-fileset/>
				
			</p:for-each>
			<px:fileset-join name="filesets-from-package-documents"/>
			<p:sink/>
			
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="fileset-from-zip-or-dir" port="result"/>
					<p:pipe step="filesets-from-package-documents" port="result"/>
				</p:input>
			</px:fileset-join>
			
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
