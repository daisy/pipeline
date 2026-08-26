<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:word-to-dtbook"
                name="main">

	<p:option name="source" required="true">
		<p:documentation>
			The Word file
		</p:documentation>
	</p:option>
	<p:option name="output-dir" required="true">
		<p:documentation>
			The directory whether the DTBook should be stored
		</p:documentation>
	</p:option>
	<p:output port="result.fileset" primary="true">
		<p:documentation>
			The DTBook fileset
		</p:documentation>
	</p:output>
	<p:output port="result.in-memory" sequence="true">
		<p:empty/>
	</p:output>

	<p:option name="title" select="''" required="false"/>
	<p:option name="creator" select="''" required="false"/>
	<p:option name="publisher" select="''" required="false"/>
	<p:option name="uid" select="''"/>
	<p:option name="subject" select="''"/>
	<p:option name="accept-revisions" select="false()" cx:as="xs:boolean"/>
	<p:option name="version" select="'14'"/>
	<p:option name="MasterSub" select="false()" cx:as="xs:boolean"/>
	<p:option name="pagination" select="'custom'"/>
	<p:option name="image-size" select="'original'"/>
	<p:option name="dpi" select="96" cx:as="xs:integer"/>
	<p:option name="character-styles" select="false()" cx:as="xs:boolean"/>
	<p:option name="footnotes-position" select="'end'"/>
	<p:option name="footnotes-level" select="0" cx:as="xs:integer"/>
	<p:option name="footnotes-numbering" cx:as="xs:string" select="'none'"/>
	<p:option name="footnotes-start-value" cx:as="xs:integer" select="1"/>
	<p:option name="footnotes-numbering-prefix" select="''"/>
	<p:option name="footnotes-numbering-suffix" select="''"/>
	<p:option name="disableDateGeneration" cx:as="xs:boolean" select="false()"/>
	<p:option name="disableGeneratorGeneration" cx:as="xs:boolean" select="false()"/>
	<p:option name="extract-shapes" cx:as="xs:boolean" select="false()"/>
	<p:option name="repair" select="false()" cx:as="xs:boolean"/>
	<p:option name="tidy" select="false()" cx:as="xs:boolean"/>
	<p:option name="simplifyHeadingLayout" select="false()" cx:as="xs:boolean"/>
	<p:option name="externalizeWhitespace" select="false()" cx:as="xs:boolean"/>
	<p:option name="documentLanguage" select="''"/>
	<p:option name="narrator" select="false()" cx:as="xs:boolean"/>
	<p:option name="ApplySentenceDetection" select="false()" cx:as="xs:boolean"/>
	<p:option name="WithDoctype" select="true()" cx:as="xs:boolean"/>

	<p:option name="temp-dir" required="true"/>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
			px:log-error
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-add-entry
			px:fileset-copy
			px:fileset-filter
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
		<p:documentation>
			px:dtbook-break-detect
			px:dtbook-unwrap-words
			px:dtbook-upgrade
			px:dtbook-load
		</p:documentation>
	</p:import>
	<p:import href="fix-dtbook/fix-dtbook.xpl">
		<p:documentation>
			pxi:dtbook-fix
		</p:documentation>
	</p:import>
	<p:import href="fix-dtbook/doctyping.xpl">
		<p:documentation>
			pxi:dtbook-doctyping
		</p:documentation>
	</p:import>

	<p:xslt template-name="main" cx:serialize="true" px:message="Converting DOCX to DTBook" px:progress="1/2">
		<p:input port="source">
			<p:empty/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="oox2Daisy.xsl"/>
		</p:input>
		<p:with-param name="InputFile" select="$source"/>
		<p:with-param name="OutputDir" select="$temp-dir"/>
		<p:with-param name="title" select="$title"/>
		<p:with-param name="creator" select="$creator"/>
		<p:with-param name="publisher" select="$publisher"/>
		<p:with-param name="uid" select="$uid"/>
		<p:with-param name="subject" select="$subject"/>
		<p:with-param name="acceptRevisions" select="$accept-revisions"/>
		<p:with-param name="version" select="$version"/>
		<p:with-param name="pagination" select="$pagination"/>
		<p:with-param name="MasterSub" select="$MasterSub"/>
		<p:with-param name="ImageSizeOption" select="$image-size"/>
		<p:with-param name="DPI" select="$dpi"/>
		<p:with-param name="CharacterStyles" select="$character-styles"/>
		<p:with-param name="FootnotesPosition" select="$footnotes-position"/>
		<p:with-param name="FootnotesLevel" select="$footnotes-level"/>
		<p:with-param name="FootnotesNumbering" select="$footnotes-numbering"/>
		<p:with-param name="FootnotesStartValue" select="$footnotes-start-value"/>
		<p:with-param name="FootnotesNumberingPrefix" select="$footnotes-numbering-prefix"/>
		<p:with-param name="FootnotesNumberingSuffix" select="$footnotes-numbering-suffix"/>
		<p:with-param name="disableDateGeneration" select="$disableDateGeneration"/>
		<p:with-param name="disableGeneratorGeneration" select="$disableGeneratorGeneration"/>
		<p:with-param name="extractShapes" select="$extract-shapes"/>
	</p:xslt>
	<p:group>
		<p:documentation>Store plain text file and load as XML</p:documentation>
		<p:variable name="path" select="concat(
		                                  $temp-dir,
		                                  replace(replace($source,'^.*/([^/]*?)(\.[^/\.]*)?$','$1.xml'),',','_'))"/>
		<p:store name="store">
			<p:with-option name="href" select="$path"/>
		</p:store>
		<p:try>
			<p:group>
				<p:load cx:depends-on="store">
					<p:with-option name="href" select="$path"/>
				</p:load>
			</p:group>
			<p:catch name="catch">
				<p:choose>
					<p:xpath-context>
						<p:pipe step="catch" port="error"/>
					</p:xpath-context>
					<p:when test="/c:errors/c:error/@code='err:XD0011'">
						<px:log-error severity="DEBUG">
							<p:input port="source">
								<p:empty/>
							</p:input>
							<p:input port="error">
								<p:pipe step="catch" port="error"/>
							</p:input>
						</px:log-error>
						<px:error code="BUG" message="An unexpected error happened. Please contact maintainer."/>
					</p:when>
					<p:otherwise>
						<!-- re-throw error -->
						<px:error>
							<p:input port="error">
								<p:pipe step="catch" port="error"/>
							</p:input>
						</px:error>
					</p:otherwise>
				</p:choose>
			</p:catch>
		</p:try>
	</p:group>
	<p:identity name="dtbook"/>

	<!-- ******************************************************************* -->
	<!-- DTBOOK CLEANUP: apply cleanup routines and optionally tag sentences -->
	<!-- ******************************************************************* -->

	<p:for-each px:message="Cleaning DTBook(s)" px:progress="1/2">
		<p:variable name="output-name" select="concat(replace(replace(base-uri(.),'^.*/([^/]+)$','$1'),'\.[^\.]*$',''),'.xml')"/>
		<p:group name="cleaned" px:message="Cleaning '{$output-name}' ...">
			<p:output port="result"/>
			<!-- Update the DTBook -->
			<px:dtbook-upgrade/>
			<!-- Apply routines -->
			<pxi:dtbook-fix>
				<p:with-option name="repair" select="$repair"/>
				<p:with-option name="tidy" select="$tidy"/>
				<p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout"/>
				<p:with-option name="externalizeWhitespace" select="$externalizeWhitespace"/>
				<p:with-option name="documentLanguage" select="$documentLanguage"/>
				<p:with-option name="narrator" select="$narrator"/>
				<p:with-option name="publisher" select="$publisher"/>
			</pxi:dtbook-fix>
			<p:choose>
				<p:when test="$ApplySentenceDetection">
					<px:dtbook-break-detect/>
					<px:dtbook-unwrap-words/>
				</p:when>
				<p:otherwise>
					<p:identity/>
				</p:otherwise>
			</p:choose>
			<p:choose>
				<p:when test="$WithDoctype">
					<!-- DTBook with doctype (result is serialized) -->
					<!--
					    FIXME: this should be handled with px:fileset-store
					-->
					<pxi:dtbook-doctyping/>
				</p:when>
				<p:otherwise>
					<p:identity/>
				</p:otherwise>
			</p:choose>
		</p:group>
		<p:store px:message="Storing the cleaned DTBook and its resources ..." name="store-dtbook">
			<p:with-option name="href" select="concat(resolve-uri($output-dir),$output-name)"/>
		</p:store>
		<!-- Copy DTBook resources -->
		<px:fileset-add-entry media-type="application/x-dtbook+xml">
			<p:input port="entry">
				<p:pipe step="dtbook" port="result"/>
			</p:input>
		</px:fileset-add-entry>
		<px:dtbook-load name="load">
			<p:input port="source.in-memory">
				<p:pipe step="dtbook" port="result"/>
			</p:input>
		</px:dtbook-load>
		<px:fileset-filter not-media-types="application/x-dtbook+xml"/>
		<px:fileset-copy name="resources">
			<p:with-option name="target" select="resolve-uri($output-dir)"/>
		</px:fileset-copy>
		<!-- Add cleaned DTBook -->
		<px:fileset-add-entry media-type="application/x-dtbook+xml">
			<p:with-option name="href" select="string(/*)">
				<p:pipe step="store-dtbook" port="result"/>
			</p:with-option>
			<p:with-option name="original-href" select="string(/*)">
				<p:pipe step="store-dtbook" port="result"/>
			</p:with-option>
		</px:fileset-add-entry>
	</p:for-each>

</p:declare-step>
