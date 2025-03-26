<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                type="px:dtbook-to-ebraille"
                name="main">

	<p:input port="source.fileset" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The DTBook and any referenced resources.</p>
		</p:documentation>
	</p:input>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	<p:input port="css.fileset">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>CSS style sheets to be attached to the HTML documents, together with any referenced
			resources (fonts, images, imported style sheets).</p>
			<p>The top-level files must be marked with a <code>role</code> attribute with value
			<code>stylesheet</code>. <code>link</code> elements are inserted in the HTML in the
			order in which they appear in the fileset.</p>
			<p>The <code>xml:base</code> of the fileset manifest is required, and determines at
			which locations relative to the eBraille publication's "ebraille" directory the files
			will be stored. Files that fall outside the base directory will result in an error.</p>
		</p:documentation>
	</p:input>
	<p:input port="css.in-memory" sequence="true">
		<p:empty/>
	</p:input>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="update-package-doc" port="in-memory"/>
	</p:output>
	<p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
		<p:pipe step="epub3" port="status"/>
	</p:output>

	<p:option name="braille-translator" select="''"/>
	<p:option name="braille-translator-stylesheet" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>CSS style sheets as space separated list of absolute URIs.</p>
		</p:documentation>
	</p:option>
	<p:option name="braille-translator-stylesheet-parameters" cx:as="xs:string" select="'()'"/>
	<p:option name="dtbook-is-valid" cx:as="xs:boolean" select="true()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the input is a valid DTBook.</p>
		</p:documentation>
	</p:option>
	<p:option name="nimas" cx:as="xs:boolean" select="false()">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether the input is NIMAS.</p>
		</p:documentation>
	</p:option>

	<p:option name="output-dir" required="true"/>
	<p:option name="temp-dir" required="true"/>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:add-ids
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-load
			px:fileset-filter
			px:fileset-update
			px:fileset-copy
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:set-base-uri
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
		<p:documentation>
			px:css-cascade
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
		<p:documentation>
			px:transform
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-epub3/library.xpl">
		<p:documentation>
			px:dtbook-to-epub3
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
		<p:documentation>
			px:epub3-create-package-doc
			px:opf-spine-to-fileset
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:css-parse-param-set
		</p:documentation>
	</cx:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:get-braille-code-info
		</p:documentation>
	</cx:import>

	<px:fileset-load media-types="application/x-dtbook+xml" name="load-dtbook">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>

	<p:group name="process-dtbook">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<p:output port="original-text" sequence="true">
			<p:pipe step="dtbook-with-pagenum-ids" port="result"/>
		</p:output>
		<p:output port="braille-codes">
			<p:pipe step="get-used-braille-codes" port="codes"/>
		</p:output>
		<p:documentation>Mark pagenum</p:documentation>
		<!-- We will use this information later (px:dtbook-to-epub preserves the IDs) to get the
		     original text of page numbers. -->
		<px:add-ids match="dtb:pagenum" name="dtbook-with-pagenum-ids"/>
		<p:documentation>Add frontmatter</p:documentation>
		<!-- make sure there is a frontmatter so that dtbook-to-zedai doesn't generate one
		     (which would result in untranslated content) -->
		<p:insert match="dtb:book[not(dtb:frontmatter)]" position="first-child">
			<p:input port="insertion">
				<p:inline exclude-inline-prefixes="#all" xmlns="http://www.daisy.org/z3986/2005/dtbook/"
				          ><frontmatter><doctitle> </doctitle></frontmatter></p:inline>
			</p:input>
		</p:insert>
		<p:string-replace match="dtb:doctitle[normalize-space(.)='']/text()"
		                  replace="//dtb:head/dtb:meta[@name='dc:Title']/@content"/>
		<p:documentation>The dc:date, dc:publisher and dc:rights of the DTBook should not be used as
		dc:date, dc:publisher and dc:rights of the eBraille publication.</p:documentation>
		<!-- Later, the dc:date of the DTBook will be used as a dcterms:date refinement on dc:source
		     of the eBraille, if dtb:uid is used as the dc:source of the eBraille (in absence of a
		     dc:source of the DTBook) -->
		<p:delete match="dtb:head/dtb:meta[lower-case(@name)=('dc:date','dc:publisher','dc:rights')]"/>
		<p:documentation>Translate all text content to Unicode braille</p:documentation>
		<p:group>
			<p:variable name="parameter-map"
			            select="pf:css-parse-param-set(($braille-translator-stylesheet-parameters,()))"/>
			<p:for-each>
				<p:variable name="lang" select="(/*/@xml:lang,'und')[1]"/>
				<px:css-cascade media="braille" content-type="application/x-dtbook+xml"
				                include-user-agent-stylesheet="true" name="dtbook-with-css">
					<p:with-option name="user-stylesheet" select="$braille-translator-stylesheet"/>
					<p:with-option name="parameters" select="$parameter-map"/>
					<p:input port="parameters">
						<p:empty/>
					</p:input>
				</px:css-cascade>
				<px:transform>
					<!-- note that this step also translates text inside head (such as style
					     elements), but this text should normally not end up in the result eBraille -->
					<p:with-option name="query" select="concat('(input:css)(output:css)(output:braille)',
					                                           '(include-braille-code-in-language)',
					                                           $braille-translator,
					                                           '(document-locale:',$lang,')')"/>
					<p:input port="parameters">
						<p:pipe step="dtbook-with-css" port="result.parameters"/>
					</p:input>
					<p:with-param port="parameters" name="translate-attributes" select="'@alt|@abbr|@title'"/>
				</px:transform>
				<p:delete match="@style"/>
			</p:for-each>
		</p:group>
		<p:documentation>Get list of used braille codes, sorted by use, and remove -t- extension
		from xml:lang attributes.</p:documentation>
		<p:group name="get-used-braille-codes">
			<p:output port="result" primary="true" sequence="true">
				<p:pipe step="each-dtbook" port="result"/>
			</p:output>
			<p:output port="codes">
				<p:pipe step="codes" port="result"/>
			</p:output>
			<p:for-each name="each-dtbook">
				<p:output port="result" primary="true"/>
				<p:output port="codes">
					<p:pipe step="xslt" port="secondary"/>
				</p:output>
				<p:xslt name="xslt">
					<p:input port="stylesheet">
						<p:document href="get-used-braille-codes.xsl"/>
					</p:input>
					<p:input port="parameters">
						<p:empty/>
					</p:input>
				</p:xslt>
				<px:set-base-uri>
					<p:with-option name="base-uri" select="base-uri(/*)">
						<p:pipe step="each-dtbook" port="current"/>
					</p:with-option>
				</px:set-base-uri>
			</p:for-each>
			<p:sink/>
			<p:xslt name="codes" template-name="main">
				<p:input port="source">
					<p:empty/>
				</p:input>
				<p:with-param name="codes" select="distinct-values(collection()//d:code/string(.))">
					<p:pipe step="each-dtbook" port="codes"/>
				</p:with-param>
				<p:with-param name="weights"
				              select="map:merge(
				                        for $code in distinct-values(collection()//d:code/string(.))
				                        return map:entry(
				                                 $code,
				                                 sum(collection()//d:code[string(.)=$code]/@weight
				                                                  /xs:integer(number(.)))))">
					<p:pipe step="each-dtbook" port="codes"/>
				</p:with-param>
				<p:input port="stylesheet">
					<p:inline>
						<xsl:stylesheet version="2.0">
							<xsl:param name="codes"/>
							<xsl:param name="weights"/>
							<xsl:template name="main">
								<d:codes>
									<xsl:for-each select="$codes">
										<xsl:sort order="descending" select="$weights(.)"/>
										<d:code><xsl:value-of select="."/></d:code>
									</xsl:for-each>
								</d:codes>
							</xsl:template>
						</xsl:stylesheet>
					</p:inline>
				</p:input>
			</p:xslt>
			<p:sink/>
		</p:group>
		<p:identity name="dtbook-with-braille-text"/>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="load-dtbook" port="unfiltered.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="load-dtbook" port="unfiltered.in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="load-dtbook" port="result.fileset"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="dtbook-with-braille-text" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

	<px:dtbook-to-epub3 name="epub3" px:progress="1"
	                    package-doc-path="package.opf"
	                    navigation-doc-path="index.html"
	                    content-path="ebraille/"
	                    xhtml-file-extension=".html">
		<p:input port="source.in-memory">
			<p:pipe step="process-dtbook" port="in-memory"/>
		</p:input>
		<p:with-option name="validation" select="'off'"/>
		<p:with-option name="output-validation" select="'off'"/>
		<p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
		<p:with-option name="nimas" select="$nimas"/>
		<!-- for some reason base URIs are lost when using collection(), so we assume there is only
		     one DTBook -->
		<p:with-option name="output-name"
		               select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
			<p:pipe step="load-dtbook" port="result"/>
		</p:with-option>
		<p:with-option name="output-dir" select="$output-dir"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
	</px:dtbook-to-epub3>
	<p:sink/>

	<p:identity>
		<p:input port="source">
			<p:pipe step="main" port="css.fileset"/>
		</p:input>
	</p:identity>
	<p:choose name="add-css">
		<p:documentation>
			Copy CSS files into eBraille publication
		</p:documentation>
		<p:when test="//d:file">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="epub3" port="result.in-memory"/>
				<p:pipe step="copy-css" port="result.in-memory"/>
			</p:output>
			<px:fileset-copy name="copy-css">
				<p:input port="source.in-memory">
					<p:pipe step="main" port="css.in-memory"/>
				</p:input>
				<p:with-option name="target" select="concat($output-dir,'ebraille/css/')"/>
			</px:fileset-copy>
			<p:sink/>
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="epub3" port="result.fileset"/>
					<p:pipe step="copy-css" port="result.fileset"/>
				</p:input>
			</px:fileset-join>
		</p:when>
		<p:otherwise>
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="epub3" port="result.in-memory"/>
			</p:output>
			<p:sink/>
			<p:identity>
				<p:input port="source">
					<p:pipe step="epub3" port="result.fileset"/>
				</p:input>
			</p:identity>
		</p:otherwise>
	</p:choose>

	<p:group name="process-html">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<px:fileset-load media-types="application/xhtml+xml" name="load-html">
			<p:input port="in-memory">
				<p:pipe step="add-css" port="in-memory"/>
			</p:input>
		</px:fileset-load>
		<p:sink/>
		<px:fileset-filter href="index.html" name="filter-primary-entry-page">
			<p:input port="source">
				<p:pipe step="load-html" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="load-html" port="result"/>
			</p:input>
		</px:fileset-filter>
		<p:documentation>
			Process primary entry page:
			- Add original text of page numbers
			- Drop any periods at the end of page numbers (added by zedai-to-html to help AT)
			- Translate generated headings "Table of contents" and "List of pages" to braille
			- Insert link to package.opf
		</p:documentation>
		<p:xslt>
			<p:input port="source">
				<p:pipe step="filter-primary-entry-page" port="result.in-memory"/>
			</p:input>
			<p:input port="stylesheet">
				<p:document href="process-primary-entry-page.xsl"/>
			</p:input>
			<p:with-param name="original-text" select="collection()">
				<p:pipe step="process-dtbook" port="original-text"/>
			</p:with-param>
			<!-- Note that $braille-translator-stylesheet and
			     $braille-translator-stylesheet-parameters are not taken into account, as well as
			     any CSS defined in the DTBook itself, as they apply only on the DTBook. This means
			     that any @text-transform rules will not have any effect for the primary entry
			     page. -->
			<p:with-param name="braille-translator" select="$braille-translator"/>
		</p:xslt>
		<p:identity name="primary-entry-page-with-link"/>
		<p:sink/>
		<p:documentation>
			Process other HTML documents:
			- Fix aria-label of page breaks
			- Add links to CSS
		</p:documentation>
		<p:for-each>
			<p:iteration-source>
				<p:pipe step="filter-primary-entry-page" port="not-matched.in-memory"/>
			</p:iteration-source>
			<p:xslt>
				<p:input port="stylesheet">
					<p:document href="process-html.xsl"/>
				</p:input>
				<p:with-param name="stylesheet-links" select="//d:file[role='stylesheet']/resolve-uri(@href,base-uri(.))">
					<p:pipe step="add-css" port="fileset"/>
				</p:with-param>
			</p:xslt>
		</p:for-each>
		<p:identity name="html-with-link-to-css"/>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="load-html" port="unfiltered.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="load-html" port="unfiltered.in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="load-html" port="result.fileset"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="primary-entry-page-with-link" port="result"/>
				<p:pipe step="html-with-link-to-css" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

	<p:documentation>Generate new package document</p:documentation>
	<p:group name="update-package-doc">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="update" port="result.in-memory"/>
		</p:output>
		<!--
		    We can drop zedai-mods.xml because (1) the EPUB specification says:
		    
		    > Due to the variety of metadata record formats and serializations that an EPUB creator
		    > can link to an EPUB publication, and the complexity of comparing metadata properties
		    > between them, this specification does not require reading systems to process linked
		    > records.
		    
		    and (2) we know it contains mostly redundant information. Only the information below
		    might not be covered in the package metadata:
		    
		    - identifier (type="uid") (from dc:Identifier)
		    - relatedItem/titleInfo/title (from dtb:sourceTitle)
		    - relatedItem/originInfo/edition (from dtb:sourceEdition)
		    - originInfo/publisher (from dtb:producer)
		    - originInfo/dateCreated (from dtb:producedDate)
		    
		    See:
		    
		    - px:dtbook-to-mods-meta
		    - px:dtbook-to-zedai-meta
		    - px:zedai-to-opf-metadata
		    - ebraille-metadata.xsl
		    
		    FIXME: Relying on this assumption is brittle. Better would be to actually check that the
		    metadata contained in MODS is actually redundant before discarding it.
		    
		    - improve px:dtbook-to-zedai-meta so that it handles what px:dtbook-to-mods-meta handles
		    - improve px:zedai-to-opf-metadata so that it handles everything that
		      px:dtbook-to-zedai-meta produces
		    - create a px:mods-to-opf-metadata that can handle what px:dtbook-to-mods-meta produces
		    - add option in dtbook-to-zedai to omit MODS
		    - add option in dtbook-to-zedai to omit MODS when redundant
		    - add option in dtbook-to-epub3 to omit MODS when redundant
		    - add option in zedai-to-epub3 to discard MODS when redundant
		-->
		<px:fileset-filter href="ebraille/zedai-mods.xml" name="filter-mods">
			<p:input port="source.in-memory">
				<p:pipe step="process-html" port="in-memory"/>
			</p:input>
		</px:fileset-filter>
		<p:sink/>
		<px:fileset-filter media-types="application/oebps-package+xml" name="filter-package-doc">
			<p:input port="source">
				<p:pipe step="filter-mods" port="not-matched"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="filter-mods" port="not-matched.in-memory"/>
			</p:input>
		</px:fileset-filter>
		<px:fileset-load name="load-package-doc">
			<p:input port="in-memory">
				<p:pipe step="process-html" port="in-memory"/>
			</p:input>
		</px:fileset-load>
		<p:group name="metadata">
			<p:output port="result"/>
			<p:filter select="//opf:metadata"/>
			<p:delete match="opf:link[@href='ebraille/zedai-mods.xml']"/>
			<p:xslt>
				<p:input port="stylesheet">
					<p:document href="ebraille-metadata.xsl"/>
				</p:input>
				<p:with-param name="dtbook-metadata" select="collection()//dtb:head">
					<p:pipe step="load-dtbook" port="result"/>
				</p:with-param>
				<p:with-param name="brailleCellType"
				              select="string-join(
				                        distinct-values(
				                          //d:code/pf:get-braille-code-info(string(.))('dots')),
				                          ', ')">
					<p:pipe step="process-dtbook" port="braille-codes"/>
				</p:with-param>
				<p:with-param name="brailleSystem" select="//d:code/string(.)">
					<p:pipe step="process-dtbook" port="braille-codes"/>
				</p:with-param>
			</p:xslt>
		</p:group>
		<p:sink/>
		<px:opf-spine-to-fileset name="spine">
			<p:input port="source.fileset">
				<p:pipe step="process-html" port="fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="process-html" port="in-memory"/>
			</p:input>
		</px:opf-spine-to-fileset>
		<p:sink/>
		<!-- container.xml should not be in package doc  -->
		<px:fileset-filter href="META-INF/container.xml" name="filter-container">
			<p:input port="source">
				<p:pipe step="filter-package-doc" port="not-matched"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="filter-package-doc" port="not-matched.in-memory"/>
			</p:input>
		</px:fileset-filter>
		<p:sink/>
		<px:epub3-create-package-doc compatibility-mode="false" name="new-package-doc">
			<p:input port="source.fileset">
				<p:pipe step="filter-container" port="not-matched"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="filter-container" port="not-matched.in-memory"/>
			</p:input>
			<p:input port="spine">
				<p:pipe step="spine" port="result"/>
			</p:input>
			<p:input port="metadata">
				<p:pipe step="metadata" port="result"/>
			</p:input>
			<p:with-option name="output-base-uri" select="base-uri(/)">
				<p:pipe step="load-package-doc" port="result"/>
			</p:with-option>
		</px:epub3-create-package-doc>
		<p:sink/>
		<px:fileset-update name="update">
			<p:input port="source.fileset">
				<p:pipe step="filter-mods" port="not-matched"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="filter-mods" port="not-matched.in-memory"/>
			</p:input>
			<p:input port="update.fileset">
				<p:pipe step="filter-package-doc" port="result"/>
			</p:input>
			<p:input port="update.in-memory">
				<p:pipe step="new-package-doc" port="result"/>
			</p:input>
		</px:fileset-update>
	</p:group>

</p:declare-step>
