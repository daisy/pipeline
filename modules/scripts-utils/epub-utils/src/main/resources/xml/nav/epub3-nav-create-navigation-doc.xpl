<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:epub3-add-navigation-doc" name="main">

	<p:input port="source.fileset" primary="true">
		<p:inline><d:fileset/></p:inline>
	</p:input>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input fileset</p>
			<p>May already include a (at most one) navigation document, in which case it should
			either be marked with a <code>role</code> attribute with value <code>nav</code>, or it
			should contain a <code>nav[@epub:type='toc']</code> element.</p>
			<p>If the input fileset does not include a navigation document it is generated from the
			content documents.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	<p:input port="content" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset from which to generate the navigation document.</p>
			<p>Defaults to all the "application/xhtml+xml" documents found in spine, or in the
			"source" fileset if the input does not contain a package document.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	<p:input port="toc" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><a
			href="http://idpf.org/epub/301/spec/epub-contentdocs.html#sec-xhtml-nav-def-types-toc">"<code>toc</code>"
			<code>nav</code> element</a> to include in the navigation document.</p>
			<p>At most one document is allowed and it is an error if a document is present when the
			input fileset already contains a navigation document.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	<p:input port="page-list" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><a
			href="http://idpf.org/epub/301/spec/epub-contentdocs.html#sec-xhtml-nav-def-types-pagelist">"<code>page-list</code>"
			<code>nav</code> element</a> to include in the navigation document.</p>
			<p>At most one document is allowed and it is an error if a document is present when the
			input fileset already contains a navigation document.</p>
		</p:documentation>
		<p:empty/>
	</p:input>
	<p:input port="landmarks" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><a
			href="http://idpf.org/epub/301/spec/epub-contentdocs.html#sec-xhtml-nav-def-types-landmarks">"<code>landmarks</code>"
			<code>nav</code> element</a> to include in the navigation document.</p>
			<p>At most one document is allowed and it is an error if a document is present when the
			input fileset already contains a navigation document.</p>
		</p:documentation>
		<p:empty/>
	</p:input>

	<p:output port="nav">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The EPUB navigation document</p>
		</p:documentation>
		<p:pipe step="add-nav-doc" port="nav.in-memory"/>
	</p:output>
	<p:output port="nav.fileset">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Fileset with as single file the navigation document</p>
		</p:documentation>
		<p:pipe step="add-nav-doc" port="nav.fileset"/>
	</p:output>
	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Copy of sourse fileset with navigation document added and package and HTML documents
			updated.</p>
			<p>If a navigation document was already present and the <code>output-base-uri</code>
			option is set, it will be moved to the new location. If a navigation document needs to
			be generated, the <code>output-base-uri</code> option is mandatory.</p>
			<p>If a package document is present in the input it will be updated.</p>
			<p>If the navigation document was generated from the content documents, the content
			documents will be changed so that all <code>body</code>, <code>article</code>,
			<code>aside</code>, <code>nav</code>, <code>section</code>, <code>h1</code>,
			<code>h2</code>, <code>h3</code>, <code>h4</code>, <code>h5</code>, <code>h6</code>,
			<code>hgroup</code> and <code>epub:type='pagebreak'</code> elements have an
			<code>id</code> attribute.</p>
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="update-package-doc" port="in-memory"/>
	</p:output>

	<p:option name="output-base-uri">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The base URI of the generated navigation document.</p>
			<p>Mandatory if the input fileset contains no navigation document yet, ignored
			otherwise.</p>
		</p:documentation>
	</p:option>
	<p:option name="page-list-hidden" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to add a <code>hidden</code> attribute to the
			<code>epub:type='page-list'</code> element.</p>
		</p:documentation>
	</p:option>
	<p:option name="title" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The title of the navigation document.</p>
			<p>If not specified, the title is "Table of contents", localized to the language of the
			content documents.</p>
		</p:documentation>
	</p:option>
	<p:option name="language" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The <code>xml:lang</code> and <code>lang</code> attributes of the navigation
			document.</p>
			<p>If not specified, it will be the language of the content documents.</p>
		</p:documentation>
	</p:option>
	<p:option name="css" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The CSS style sheet to attach to the navigation document.</p>
		</p:documentation>
	</p:option>

	<p:import href="epub3-nav-create-toc.xpl">
		<p:documentation>
			px:epub3-create-toc
		</p:documentation>
	</p:import>
	<p:import href="epub3-nav-create-page-list.xpl">
		<p:documentation>
			px:epub3-create-page-list
		</p:documentation>
	</p:import>
	<p:import href="epub3-nav-aggregate.xpl">
		<p:documentation>
			pxi:epub3-nav-aggregate
		</p:documentation>
	</p:import>
	<p:import href="../pub/opf-spine-to-fileset.xpl">
		<p:documentation>
			px:opf-spine-to-fileset
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-filter
			px:fileset-filter-in-memory
			px:fileset-update
			px:fileset-add-entry
			px:fileset-join
		</p:documentation>
	</p:import>

	<p:documentation>Get content documents</p:documentation>
	<px:fileset-load media-types="application/xhtml+xml" name="all-content-docs">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<p:sink/>

	<p:documentation>Check if there is already a navigation doc in the input</p:documentation>
	<p:group name="nav-doc-in-input">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory">
			<p:pipe step="choose" port="in-memory"/>
		</p:output>
		<p:choose name="choose">
			<p:xpath-context>
				<p:pipe step="all-content-docs" port="result.fileset"/>
			</p:xpath-context>
			<p:when test="//d:file[@role='nav']">
				<p:output port="fileset" primary="true">
					<p:pipe step="load" port="result.fileset"/>
				</p:output>
				<p:output port="in-memory">
					<p:pipe step="load" port="result"/>
				</p:output>
				<p:delete match="d:file[not(@role='nav')]">
					<p:input port="source">
						<p:pipe step="all-content-docs" port="result.fileset"/>
					</p:input>
				</p:delete>
				<px:fileset-load name="load">
					<p:input port="in-memory">
						<p:pipe step="all-content-docs" port="result"/>
					</p:input>
				</px:fileset-load>
			</p:when>
			<p:otherwise>
				<p:output port="fileset" primary="true">
					<p:pipe step="filter" port="result"/>
				</p:output>
				<p:output port="in-memory">
					<p:pipe step="filter" port="result.in-memory"/>
				</p:output>
				<p:split-sequence test="//html:nav[@epub:type='toc']" name="content-docs-with-toc">
					<p:input port="source">
						<p:pipe step="all-content-docs" port="result"/>
					</p:input>
				</p:split-sequence>
				<p:sink/>
				<px:fileset-filter-in-memory name="filter">
					<p:input port="source.fileset">
						<p:pipe step="all-content-docs" port="result.fileset"/>
					</p:input>
					<p:input port="source.in-memory">
						<p:pipe step="content-docs-with-toc" port="matched"/>
					</p:input>
				</px:fileset-filter-in-memory>
			</p:otherwise>
		</p:choose>
		<px:assert message="There can be at most one navigation document in the input" error-code="XXXXX">
			<p:with-option name="test" select="count(//d:file)&lt;=1"/>
		</px:assert>
		<px:assert message="'output-base-uri' option is mandatory when there is no navigation document in the input"
		           error-code="XXXXX">
			<p:with-option name="test" select="p:value-available('output-base-uri') or count(//d:file)=1"/>
		</px:assert>
	</p:group>

	<p:documentation>Generate navigation doc if not present in input</p:documentation>
	<p:choose name="add-nav-doc">
		<p:when test="//d:file">
			<p:output port="result.fileset" primary="true"/>
			<p:output port="result.in-memory" sequence="true">
				<p:pipe step="main" port="source.in-memory"/>
			</p:output>
			<p:output port="nav.fileset">
				<p:pipe step="nav-doc-in-input" port="fileset"/>
			</p:output>
			<p:output port="nav.in-memory">
				<p:pipe step="nav-doc-in-input" port="in-memory"/>
			</p:output>
			<p:sink/>
			<px:assert test-count-max="0" error-code="XXXXX" name="assert-no-toc"
			           message="No document may be present on 'toc' port if navigation document already present in the input">
				<p:input port="source">
					<p:pipe step="main" port="toc"/>
				</p:input>
			</px:assert>
			<p:sink/>
			<px:assert test-count-max="0" error-code="XXXXX" name="assert-no-page-list"
			           message="No document may be present on 'page-list' port if navigation document already present in the input">
				<p:input port="source">
					<p:pipe step="main" port="page-list"/>
				</p:input>
			</px:assert>
			<p:sink/>
			<px:assert test-count-max="0" error-code="XXXXX" name="assert-no-landmarks"
			           message="No document may be present on 'landmarks' port if navigation document already present in the input">
				<p:input port="source">
					<p:pipe step="main" port="landmarks"/>
				</p:input>
			</px:assert>
			<p:sink/>
			<p:identity>
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
			</p:identity>
			<p:identity cx:depends-on="assert-no-toc"/>
			<p:identity cx:depends-on="assert-no-page-list"/>
			<p:identity cx:depends-on="assert-no-landmarks"/>
		</p:when>
		<p:otherwise>
			<p:output port="result.fileset" primary="true">
				<p:pipe step="result" port="result"/>
			</p:output>
			<p:output port="result.in-memory" sequence="true">
				<p:pipe step="result" port="result.in-memory"/>
			</p:output>
			<p:output port="nav.fileset">
				<p:pipe step="nav" port="result"/>
			</p:output>
			<p:output port="nav.in-memory">
				<p:pipe step="nav" port="result.in-memory"/>
			</p:output>

			<p:documentation>Filter content documents</p:documentation>
			<px:assert test-count-max="1" error-code="XXXXX" message="At most one document allowed on 'content' port">
				<p:input port="source">
					<p:pipe step="main" port="content"/>
				</p:input>
			</px:assert>
			<p:count/>
			<p:choose name="content-docs">
				<p:when test="/.=1">
					<p:output port="in-memory" primary="true" sequence="true">
						<p:pipe step="filter" port="result.in-memory"/>
					</p:output>
					<p:output port="fileset">
						<p:pipe step="filter" port="result"/>
					</p:output>
					<p:sink/>
					<px:fileset-filter-in-memory name="filter">
						<p:input port="source.fileset">
							<p:pipe step="main" port="content"/>
						</p:input>
						<p:input port="source.in-memory">
							<p:pipe step="all-content-docs" port="result"/>
						</p:input>
					</px:fileset-filter-in-memory>
					<p:sink/>
				</p:when>
				<p:otherwise>
					<p:output port="in-memory" primary="true" sequence="true"/>
					<p:output port="fileset">
						<p:pipe step="spine-or-all-content-docs" port="fileset"/>
					</p:output>
					<p:sink/>
					<px:fileset-filter media-types="application/oebps-package+xml">
						<p:input port="source">
							<p:pipe step="main" port="source.fileset"/>
						</p:input>
					</px:fileset-filter>
					<p:choose name="spine-or-all-content-docs">
						<p:documentation>If there is a package document, select the spine only</p:documentation>
						<p:when test="//d:file">
							<p:output port="in-memory" primary="true" sequence="true"/>
							<p:output port="fileset">
								<p:pipe step="spine" port="result"/>
							</p:output>
							<p:sink/>
							<px:opf-spine-to-fileset name="spine">
								<p:input port="source.fileset">
									<p:pipe step="main" port="source.fileset"/>
								</p:input>
								<p:input port="source.in-memory">
									<p:pipe step="main" port="source.in-memory"/>
								</p:input>
							</px:opf-spine-to-fileset>
							<px:fileset-load>
								<p:input port="in-memory">
									<p:pipe step="all-content-docs" port="result"/>
								</p:input>
							</px:fileset-load>
						</p:when>
						<p:otherwise>
							<p:output port="in-memory" primary="true" sequence="true"/>
							<p:output port="fileset">
								<p:pipe step="all-content-docs" port="result.fileset"/>
							</p:output>
							<p:sink/>
							<p:identity>
								<p:input port="source">
									<p:pipe step="all-content-docs" port="result"/>
								</p:input>
							</p:identity>
						</p:otherwise>
					</p:choose>
				</p:otherwise>
			</p:choose>

			<p:documentation>Create toc</p:documentation>
			<p:group name="toc">
				<p:output port="result" primary="true"/>
				<p:output port="content-docs" sequence="true">
					<p:pipe step="skip-if-provided" port="content-docs"/>
				</p:output>
				<px:assert test-count-max="1" error-code="XXXXX" message="At most one document may be present on 'toc' port">
					<p:input port="source">
						<p:pipe step="main" port="toc"/>
					</p:input>
				</px:assert>
				<p:count/>
				<p:choose name="skip-if-provided">
					<p:when test=".=1">
						<p:output port="result" primary="true"/>
						<p:output port="content-docs" sequence="true">
							<p:pipe step="content-docs" port="in-memory"/>
						</p:output>
						<p:identity>
							<p:input port="source">
								<p:pipe step="main" port="toc"/>
							</p:input>
						</p:identity>
					</p:when>
					<p:otherwise>
						<p:output port="result" primary="true"/>
						<p:output port="content-docs" sequence="true">
							<p:pipe step="create" port="content-docs"/>
						</p:output>
						<px:epub3-create-toc name="create">
							<p:input port="source">
								<p:pipe step="content-docs" port="in-memory"/>
							</p:input>
							<p:with-option name="output-base-uri" select="$output-base-uri">
								<p:empty/>
							</p:with-option>
						</px:epub3-create-toc>
					</p:otherwise>
				</p:choose>
			</p:group>
			<p:sink/>
	
			<p:documentation>Create page list</p:documentation>
			<p:group name="page-list">
				<p:output port="result" primary="true" sequence="true"/>
				<p:output port="content-docs" sequence="true">
					<p:pipe step="skip-if-provided" port="content-docs"/>
				</p:output>
				<px:assert test-count-max="1" error-code="XXXXX" message="At most one document may be present on 'page-list' port">
					<p:input port="source">
						<p:pipe step="main" port="page-list"/>
					</p:input>
				</px:assert>
				<p:count/>
				<p:choose name="skip-if-provided">
					<p:when test=".=1">
						<p:output port="result" primary="true"/>
						<p:output port="content-docs" sequence="true">
							<p:pipe step="toc" port="content-docs"/>
						</p:output>
						<p:identity>
							<p:input port="source">
								<p:pipe step="main" port="page-list"/>
							</p:input>
						</p:identity>
					</p:when>
					<p:otherwise>
						<p:output port="result" primary="true"/>
						<p:output port="content-docs" sequence="true">
							<p:pipe step="create" port="content-docs"/>
						</p:output>
						<px:epub3-create-page-list name="create">
							<p:input port="source">
								<p:pipe step="toc" port="content-docs"/>
							</p:input>
							<p:with-option name="output-base-uri" select="$output-base-uri">
								<p:empty/>
							</p:with-option>
							<p:with-option name="hidden" select="$page-list-hidden">
								<p:empty/>
							</p:with-option>
						</px:epub3-create-page-list>
					</p:otherwise>
				</p:choose>
				<p:split-sequence test="/html:nav[html:ol/html:li]">
					<p:documentation>Omit page list if empty</p:documentation>
				</p:split-sequence>
			</p:group>
			<p:sink/>

			<px:assert test-count-max="1" error-code="XXXXX" message="At most one document may be present on 'landmarks' port"
			           name="landmarks">
				<p:input port="source">
					<p:pipe step="main" port="landmarks"/>
				</p:input>
			</px:assert>
			<p:sink/>

			<pxi:epub3-nav-aggregate name="aggregate">
				<p:input port="source">
					<p:pipe step="toc" port="result"/>
					<p:pipe step="page-list" port="result"/>
					<p:pipe step="landmarks" port="result"/>
				</p:input>
				<p:with-option name="output-base-uri" select="$output-base-uri"/>
				<p:with-option name="title" select="$title"/>
				<p:with-option name="language" select="$language"/>
				<p:with-option name="css" select="$css"/>
			</pxi:epub3-nav-aggregate>
			<p:sink/>

			<px:fileset-update name="update">
				<p:input port="source.fileset">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
				<p:input port="update.fileset">
					<p:pipe step="content-docs" port="fileset"/>
				</p:input>
				<p:input port="update.in-memory">
					<p:pipe step="page-list" port="content-docs"/>
				</p:input>
			</px:fileset-update>
			<px:fileset-add-entry media-type="application/xhtml+xml" name="result">
				<p:input port="source.in-memory">
					<p:pipe step="update" port="result.in-memory"/>
				</p:input>
				<p:input port="entry">
					<p:pipe step="aggregate" port="result"/>
				</p:input>
			</px:fileset-add-entry>
			<px:fileset-filter-in-memory name="nav">
				<p:input port="source.in-memory">
					<p:pipe step="aggregate" port="result"/>
				</p:input>
			</px:fileset-filter-in-memory>
		</p:otherwise>
	</p:choose>

	<p:documentation>Update package document</p:documentation>
	<p:group name="update-package-doc">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="choose" port="in-memory"/>
		</p:output>
		<px:fileset-filter media-types="application/oebps-package+xml"/>
		<p:choose name="choose">
			<p:when test="//d:file">
				<p:output port="fileset" primary="true"/>
				<p:output port="in-memory" sequence="true">
					<p:pipe step="result" port="result.in-memory"/>
				</p:output>
				<px:fileset-load name="input-package-doc">
					<p:input port="in-memory">
						<p:pipe step="add-nav-doc" port="result.in-memory"/>
					</p:input>
				</px:fileset-load>
				<p:for-each>
					<p:xslt>
						<p:input port="stylesheet">
							<p:document href="../pub/add-nav.xsl"/>
						</p:input>
						<p:with-param name="nav-doc-uri" select="base-uri(/*)">
							<p:pipe step="add-nav-doc" port="nav.in-memory"/>
						</p:with-param>
					</p:xslt>
				</p:for-each>
				<p:identity name="package-doc"/>
				<p:sink/>
				<px:fileset-update name="result">
					<p:input port="source.fileset">
						<p:pipe step="add-nav-doc" port="result.fileset"/>
					</p:input>
					<p:input port="source.in-memory">
						<p:pipe step="add-nav-doc" port="result.in-memory"/>
					</p:input>
					<p:input port="update.fileset">
						<p:pipe step="input-package-doc" port="result.fileset"/>
					</p:input>
					<p:input port="update.in-memory">
						<p:pipe step="package-doc" port="result"/>
					</p:input>
				</px:fileset-update>
			</p:when>
			<p:otherwise>
				<p:output port="fileset" primary="true"/>
				<p:output port="in-memory" sequence="true">
					<p:pipe step="add-nav-doc" port="result.in-memory"/>
				</p:output>
				<!--
				    if no package document present, add role attribute to navigation document in fileset
				-->
				<p:sink/>
				<p:add-attribute match="d:file" attribute-name="role" attribute-value="nav" name="nav-fileset-with-role">
					<p:input port="source">
						<p:pipe step="add-nav-doc" port="nav.fileset"/>
					</p:input>
				</p:add-attribute>
				<p:sink/>
				<px:fileset-join>
					<p:input port="source">
						<p:pipe step="add-nav-doc" port="result.fileset"/>
						<p:pipe step="nav-fileset-with-role" port="result"/>
					</p:input>
				</px:fileset-join>
			</p:otherwise>
		</p:choose>
	</p:group>

</p:declare-step>
