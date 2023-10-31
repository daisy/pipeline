<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub3-label-pagebreaks-from-nav"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Identify page break elements based on the "page-list" navigation and label them with
		<code>epub:type="pagebreak"</code>, and return the page list as a
		<code>d:fileset</code>.</p>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input EPUB 3 fileset</p>
			<p>The navigation document, if present, should be marked with
			<code>role="nav"</code>.</p>
		</p:documentation>
	</p:input>

	<p:output port="result.fileset" primary="true">
		<p:pipe step="main" port="source.fileset"/>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Output EPUB 3 fileset</p>
			<p>The output manifest is identical to the input manifest. Content documents are
			modified so that elements referenced by the "page-list" navigation are labeled with
			<code>epub:type="pagebreak"</code>. A <code>aria-label</code> attribute is added if
			missing.</p>
		</p:documentation>
		<p:pipe step="in-memory" port="result"/>
	</p:output>

	<p:output port="page-list" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The "page-list" navigation converted into the <code>d:fileset</code> format, with
			every page reference represented by a <code>d:anchor</code> element.</p>
			<p>Empty sequence if there is no "page-list" navigation in the EPUB.</p>
		</p:documentation>
		<p:pipe step="page-list" port="result"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-update
		</p:documentation>
	</p:import>

	<p:group name="page-list">
		<p:output port="result" sequence="true"/>
		<p:identity>
			<p:input port="source">
				<p:pipe step="main" port="source.fileset"/>
			</p:input>
		</p:identity>
		<p:choose>
			<p:when test="exists(//d:file[@role='nav'])">
				<px:fileset-load>
					<p:input port="in-memory">
						<p:pipe step="main" port="source.in-memory"/>
					</p:input>
					<p:with-option name="href" select="//d:file[@role='nav'][1]/@href"/>
				</px:fileset-load>
				<p:add-xml-base>
					<!-- Not sure why this is needed. Omitted this could trigger a base URI error in
					     list-pagebreaks-from-nav.xsl. Bug? -->
				</p:add-xml-base>
				<p:filter select="//html:nav[tokenize(@epub:type,'\s+')='page-list'][1]" name="nav"/>
				<p:count/>
				<p:choose>
					<p:when test="/*=0">
						<p:sink/>
						<p:identity>
							<p:input port="source">
								<p:empty/>
							</p:input>
						</p:identity>
					</p:when>
					<p:otherwise>
						<p:sink/>
						<p:xslt template-name="main">
							<p:input port="source">
								<p:pipe step="nav" port="result"/>
							</p:input>
							<p:input port="stylesheet">
								<p:document href="list-pagebreaks-from-nav.xsl"/>
							</p:input>
							<p:input port="parameters">
								<p:empty/>
							</p:input>
						</p:xslt>
					</p:otherwise>
				</p:choose>
			</p:when>
			<p:otherwise>
				<p:sink/>
				<p:identity>
					<p:input port="source">
						<p:empty/>
					</p:input>
				</p:identity>
			</p:otherwise>
		</p:choose>
	</p:group>
	<p:count/>
	<p:choose>
		<p:when test="/*=0">
			<p:output port="result" sequence="true"/>
			<p:sink/>
			<p:identity>
				<p:input port="source">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</p:identity>
		</p:when>
		<p:otherwise>
			<p:output port="result" sequence="true">
				<p:pipe step="update" port="result.in-memory"/>
			</p:output>
			<p:sink/>
			<px:fileset-load media-types="application/xhtml+xml" name="content-docs">
				<p:input port="fileset">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
			</px:fileset-load>
			<p:for-each name="content-docs-with-pagebreak">
				<p:output port="result"/>
				<p:sink/>
				<p:xslt>
					<p:input port="source">
						<p:pipe step="content-docs-with-pagebreak" port="current"/>
						<p:pipe step="page-list" port="result"/>
					</p:input>
					<p:input port="stylesheet">
						<p:document href="label-pagebreaks-from-nav.xsl"/>
					</p:input>
					<p:input port="parameters">
						<p:empty/>
					</p:input>
				</p:xslt>
			</p:for-each>
			<p:sink/>
			<px:fileset-update name="update">
				<p:input port="source.fileset">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="main" port="source.in-memory"/>
				</p:input>
				<p:input port="update.fileset">
					<p:pipe step="content-docs" port="result.fileset"/>
				</p:input>
				<p:input port="update.in-memory">
					<p:pipe step="content-docs-with-pagebreak" port="result"/>
				</p:input>
			</px:fileset-update>
			<p:sink/>
		</p:otherwise>
	</p:choose>
	<p:identity name="in-memory"/>

</p:declare-step>
