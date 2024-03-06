<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub2-to-epub3"
                name="main">

	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The EPUB 2 fileset</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true"/>

	<p:output port="result.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The EPUB 3 fileset</p>
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="ncx-to-nav" port="in-memory"/>
	</p:output>

	<p:option name="result-base" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Location of the result EPUB</p>
			<p>In case of a unzipped EPUB this should point to the base directory of the EPUB, in
			case of a zipped EPUB this should point to the EPUB file with '!/' added at the end.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-copy
			px:fileset-rebase
			px:fileset-load
			px:fileset-update
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
		<p:documentation>
			px:html-upgrade
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub3-ensure-core-media
			px:epub-upgrade-package-doc
			px:epub-guide-to-landmarks
			px:epub3-add-navigation-doc
			px:epub3-nav-from-ncx
			px:epub-rename-files
		</p:documentation>
	</p:import>

	<p:documentation>
		Normalize input fileset: make sure that the base is the directory containing the mimetype file
	</p:documentation>
	<p:choose>
		<p:when test="//d:file[matches(@href,'^(.+/)?mimetype$')]">
			<px:fileset-rebase>
				<p:with-option name="new-base"
				               select="//d:file[matches(@href,'^(.+/)?mimetype$')][1]
				                       /replace(resolve-uri(@href,base-uri(.)),'mimetype$','')"/>
			</px:fileset-rebase>
		</p:when>
		<p:otherwise>
			<px:error code="XXXXX" message="Fileset must contain a 'mimetype' file"/>
		</p:otherwise>
	</p:choose>

	<p:documentation>
		Move to new location
	</p:documentation>
	<px:fileset-copy name="move">
		<p:with-option name="target" select="$result-base"/>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-copy>

	<p:documentation>
		Filter out resources that are not EPUB 3 core media types:
		- application/x-dtbook+xml
		- application/xml
		- text/x-oeb1-css
		- text/x-oeb1-document
	</p:documentation>
	<px:epub3-ensure-core-media name="clean" px:progress="1/6">
		<p:input port="source.in-memory">
			<p:pipe step="move" port="result.in-memory"/>
		</p:input>
	</px:epub3-ensure-core-media>

	<p:documentation>
		Upgrade HTML
	</p:documentation>
	<p:group name="upgrade-html" px:progress="3/6">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="rename" port="result.in-memory"/>
		</p:output>
		<px:fileset-load media-types="application/xhtml+xml" name="load">
			<p:input port="in-memory">
				<p:pipe step="clean" port="result.in-memory"/>
			</p:input>
		</px:fileset-load>
		<p:for-each name="docs" px:progress="2/3">
			<p:output port="result"/>
			<px:html-upgrade/>
		</p:for-each>
		<p:sink/>
		<p:documentation>
			Update doctype
		</p:documentation>
		<p:add-attribute match="d:file" attribute-name="doctype" attribute-value="&lt;!DOCTYPE html&gt;" name="html5.fileset">
			<p:input port="source">
				<p:pipe step="load" port="result.fileset"/>
			</p:input>
		</p:add-attribute>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="clean" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="clean" port="result.in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="html5.fileset" port="result"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="docs" port="result"/>
			</p:input>
		</px:fileset-update>
		<p:documentation>
			Rename files to .xhtml.
		</p:documentation>
		<px:fileset-filter media-types="application/xhtml+xml"/>
		<p:label-elements match="d:file" attribute="original-href" replace="true"
		                  label="resolve-uri(@href,base-uri(.))"/>
		<p:label-elements match="d:file" attribute="href" replace="true"
		                  label="replace(@href,'^(.*)\.([^/\.]*)$','$1.xhtml')"/>
		<p:delete match="/*/*[not(self::d:file)]"/>
		<p:delete match="d:file/@*[not(name()=('href','original-href'))]" name="mapping"/>
		<p:sink/>
		<px:epub-rename-files name="rename" px:progress="1/3">
			<p:input port="source.fileset">
				<p:pipe step="update" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="update" port="result.in-memory"/>
			</p:input>
			<p:input port="mapping">
				<p:pipe step="mapping" port="result"/>
			</p:input>
		</px:epub-rename-files>
	</p:group>

	<p:documentation>
		Upgrade package document
	</p:documentation>
	<p:group name="upgrade-package-doc" px:progress="1/6">
		<p:output port="result.fileset" primary="true"/>
		<p:output port="result.in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<p:output port="opf">
			<p:pipe step="doc" port="result"/>
		</p:output>
		<px:epub-upgrade-package-doc name="doc">
			<p:input port="source.in-memory">
				<p:pipe step="upgrade-html" port="in-memory"/>
			</p:input>
		</px:epub-upgrade-package-doc>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="upgrade-html" port="fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="upgrade-html" port="in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="doc" port="result.fileset"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="doc" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

	<p:documentation>
		Create navigation document
	</p:documentation>
	<p:group name="ncx-to-nav" px:progress="1/6">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="create-nav" port="result.in-memory"/>
		</p:output>
		<px:fileset-load media-types="application/x-dtbncx+xml" name="ncx">
			<p:input port="in-memory">
				<p:pipe step="upgrade-package-doc" port="result.in-memory"/>
			</p:input>
		</px:fileset-load>

		<p:documentation>Convert pageList to page-list</p:documentation>
		<p:group name="page-list">
			<p:output port="result" sequence="true"/>
			<px:epub3-nav-from-ncx/>
			<p:filter select="//html:nav[@epub:type='page-list']"/>
		</p:group>
		<p:sink/>

		<p:documentation>Convert guide to landmarks</p:documentation>
		<px:epub-guide-to-landmarks name="landmarks">
			<p:input port="source">
				<p:pipe step="upgrade-package-doc" port="opf"/>
			</p:input>
			<p:with-option name="output-base-uri" select="resolve-uri('nav.xhtml',base-uri(/*))">
				<p:pipe step="ncx" port="result"/>
			</p:with-option>
		</px:epub-guide-to-landmarks>
		<p:sink/>

		<px:epub3-add-navigation-doc name="create-nav">
			<p:input port="source.fileset">
				<p:pipe step="upgrade-package-doc" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="upgrade-package-doc" port="result.in-memory"/>
			</p:input>
			<p:input port="page-list">
				<p:pipe step="page-list" port="result"/>
			</p:input>
			<p:input port="landmarks">
				<p:pipe step="landmarks" port="result"/>
			</p:input>
			<p:with-option name="output-base-uri" select="resolve-uri('nav.xhtml',base-uri(/*))">
				<p:pipe step="ncx" port="result"/>
			</p:with-option>
		</px:epub3-add-navigation-doc>
	</p:group>

</p:declare-step>
