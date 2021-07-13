<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                type="px:daisy202-to-daisy3"
                name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>DAISY 2.02 fileset</p>
			<p>Mediatype detection is assumed to applied and the SMIL files in the fileset are
			expected to be ordered according to reading order.</p>
		</p:documentation>
	</p:input>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>DAISY 3 fileset</p>
		</p:documentation>
		<p:pipe step="group" port="in-memory"/>
	</p:output>

	<p:option name="output-dir" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Base directory of the result DAISY 3 fileset.</p>
		</p:documentation>
	</p:option>
	<p:option name="identifier" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Identifier</p>
		</p:documentation>
	</p:option>
	<p:option name="dtbook-css" required="false" select="''">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>URI of CSS style sheet to apply to the DTBooks.</p>
			<p>If left empty, a default style sheet is used.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:set-base-uri
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
		<p:documentation>
			px:html-to-fileset
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-join
			px:fileset-load
			px:fileset-filter
			px:fileset-rebase
			px:fileset-copy
			px:fileset-diff
			px:fileset-add-entry
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
		<p:documentation>
			px:smil-update-links
			px:smil-to-audio-fileset
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
		<p:documentation>
			px:dtbook-load
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
		<p:documentation>
			px:daisy3-create-opf
			px:daisy3-create-res-file
			px:daisy3-smil-add-elapsed-time
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/html-to-dtbook/library.xpl">
		<p:documentation>
			px:html-to-dtbook
		</p:documentation>
	</p:import>
	<p:import href="ncc-to-oebps-metadata.xpl">
		<p:documentation>
			pxi:ncc-to-oebps-metadata
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:daisy202-identifier
			pf:daisy202-title
		</p:documentation>
	</cx:import>

	<px:fileset-load href="*/ncc.html">
		<p:input port="in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-load>
	<px:assert message="Input fileset must contain exactly one ncc.html"
	           test-count-min="1" test-count-max="1" name="ncc"/>

	<p:group name="group" px:progress="1">
		<p:output port="fileset" primary="true"/>
		<p:output port="in-memory" sequence="true">
			<p:pipe step="add-opf" port="in-memory"/>
		</p:output>
		<p:variable name="ncc" select="/"/>
		<p:variable name="daisy3-identifier" select="if ($identifier!='')
		                                             then $identifier
		                                             else pf:daisy202-identifier($ncc)"/>
		<p:variable name="daisy3-title" select="pf:daisy202-title($ncc)"/>
		<p:sink/>

		<p:documentation>
			Normalize fileset
		</p:documentation>
		<p:group>
			<p:output port="result"/>
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="main" port="source.fileset"/>
				</p:input>
			</px:fileset-join>
			<px:fileset-rebase>
				<p:with-option name="new-base" select="pf:normalize-uri(resolve-uri('.',base-uri($ncc/*)))"/>
			</px:fileset-rebase>
		</p:group>

		<p:documentation>
			Copy to new location
		</p:documentation>
		<px:fileset-copy name="copy">
			<p:with-option name="target" select="$output-dir"/>
			<p:input port="source.in-memory">
				<p:pipe step="main" port="source.in-memory"/>
			</p:input>
		</px:fileset-copy>

		<p:documentation>
			Filter content documents
		</p:documentation>
		<p:group name="html">
			<p:output port="fileset" primary="true">
				<p:pipe step="load" port="result.fileset"/>
			</p:output>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="load" port="result"/>
			</p:output>
			<px:fileset-filter media-types="application/xhtml+xml">
				<p:input port="source">
					<p:pipe step="copy" port="result.fileset"/>
				</p:input>
			</px:fileset-filter>
			<p:delete match="d:file[@href='ncc.html']"/>
			<px:fileset-load name="load">
				<p:input port="in-memory">
					<p:pipe step="copy" port="result.in-memory"/>
				</p:input>
			</px:fileset-load>
		</p:group>

		<p:documentation>
			Convert HTML to DTBook (possibly with SVG and CSS resources)
		</p:documentation>
		<p:group name="dtbook" px:progress="0.2" px:message="Converting HTML to DTBook">
			<p:output port="fileset" primary="true">
				<p:pipe step="dtbook-fileset" port="result"/>
			</p:output>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="for-each" port="in-memory"/>
			</p:output>
			<p:output port="mapping">
				<p:pipe step="mapping" port="result"/>
			</p:output>
			<p:for-each>
				<p:iteration-source select="/*/d:file"/>
				<p:variable name="href" select="resolve-uri(/*/@href,base-uri(/*))"/>
				<px:fileset-filter>
					<p:input port="source">
						<p:pipe step="html" port="fileset"/>
					</p:input>
					<p:with-option name="href" select="$href"/>
				</px:fileset-filter>
			</p:for-each>
			<p:for-each name="for-each">
				<p:output port="fileset" primary="true"/>
				<p:output port="in-memory" sequence="true">
					<p:pipe step="html-to-dtbook" port="result.in-memory"/>
				</p:output>
				<p:output port="mapping">
					<p:pipe step="html-to-dtbook" port="mapping"/>
				</p:output>
				<px:html-to-dtbook name="html-to-dtbook">
					<p:input port="source.in-memory">
						<p:pipe step="html" port="in-memory"/>
					</p:input>
					<p:with-option name="dtbook-css" select="$dtbook-css"/>
				</px:html-to-dtbook>
			</p:for-each>
			<px:fileset-join name="dtbook-fileset"/>
			<p:sink/>
			<px:fileset-join name="mapping">
				<p:input port="source">
					<p:pipe step="for-each" port="mapping"/>
				</p:input>
			</px:fileset-join>
			<p:sink/>
		</p:group>
		<p:sink/>

		<p:documentation>
			Upgrade SMILs
		</p:documentation>
		<p:group name="smil" px:progress="0.2" px:message="Converting SMIL">
			<p:output port="fileset" primary="true">
				<p:pipe step="load" port="result.fileset"/>
			</p:output>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="result" port="result"/>
			</p:output>
			<p:output port="daisy202-smils" sequence="true">
				<p:pipe step="load" port="result"/>
			</p:output>
			<p:documentation>Load SMIL files in reading order</p:documentation>
			<px:fileset-filter media-types="application/smil+xml" name="filter">
				<p:input port="source">
					<p:pipe step="copy" port="result.fileset"/>
				</p:input>
				<p:input port="source.in-memory">
					<p:pipe step="copy" port="result.in-memory"/>
				</p:input>
			</px:fileset-filter>
			<px:fileset-load name="load">
				<p:input port="in-memory">
					<p:pipe step="filter" port="result.in-memory"/>
				</p:input>
			</px:fileset-load>
			<p:for-each>
				<p:variable name="smil" select="/"/>
				<p:variable name="smil-base" select="base-uri($smil/*)"/>
				<p:xslt>
					<p:input port="stylesheet">
						<p:document href="d202smil_Z2005smil.xsl"/>
					</p:input>
					<p:with-param port="parameters" name="uid" select="$daisy3-identifier"/>
					<p:with-param port="parameters" name="title" select="daisy3-title"/>
					<p:with-param port="parameters" name="isNcxOnly"
					              select="not(//d:file[@media-type='application/xhtml+xml' and not(@href='ncc.html')])">
						<p:pipe step="copy" port="result.fileset"/>
					</p:with-param>
					<!-- Whether the NCC points to par or text in incoming smil file. If all targets
					     point to par, return true, else return false. -->
					<p:with-param port="parameters" name="NCCPointsToPars"
					              select="not(
					                        some $a in $ncc//html:a[substring-before(resolve-uri(@href,$output-dir),'#')=$smil-base] satisfies
					                        some $id in substring-after($a/@href,'#') satisfies
					                        $smil//*[@id=$id]/not(self::par))"/>
				</p:xslt>
				<px:smil-update-links>
					<p:input port="mapping">
						<p:pipe step="dtbook" port="mapping"/>
					</p:input>
				</px:smil-update-links>
			</p:for-each>
			<px:daisy3-smil-add-elapsed-time/>
			<p:identity name="result"/>
		</p:group>
		<p:sink/>

		<p:documentation>
			Create NCX
		</p:documentation>
		<p:group name="ncx" px:progress="0.2" px:message="Creating NCX">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="ncc-to-ncx" port="result"/>
			</p:output>
			<px:set-base-uri>
				<p:input port="source">
					<p:pipe step="ncc" port="result"/>
				</p:input>
				<p:with-option name="base-uri" select="resolve-uri('navigation.ncx',$output-dir)"/>
			</px:set-base-uri>
			<p:xslt name="ncc-to-ncx">
				<p:input port="stylesheet">
					<p:document href="d202ncc_Z2005ncx.xsl"/>
				</p:input>
				<p:with-param port="parameters" name="uid" select="$daisy3-identifier"/>
				<p:with-param port="parameters" name="smils" select="collection()">
					<p:pipe step="smil" port="daisy202-smils"/>
				</p:with-param>
				<p:with-param port="parameters" name="smilCustomTests"
				              select="distinct-values(collection()/s:smil/s:head/s:customAttributes/s:customTest/@id)">
					<p:pipe step="smil" port="in-memory"/>
				</p:with-param>
			</p:xslt>
			<p:sink/>
			<px:fileset-create>
				<p:with-option name="base" select="$output-dir"/>
			</px:fileset-create>
			<px:fileset-add-entry media-type="application/x-dtbncx+xml">
				<p:input port="entry">
					<p:pipe step="ncc-to-ncx" port="result"/>
				</p:input>
				<p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD ncx 2005-1//EN'"/>
				<p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/ncx-2005-1.dtd'"/>
			</px:fileset-add-entry>
		</p:group>
		<p:sink/>

		<p:documentation>
			Create RES file
		</p:documentation>
		<px:daisy3-create-res-file name="res" px:progress="0.1" px:message="Creating RES file">
			<p:input port="source">
				<p:document href="text.res"/>
			</p:input>
			<p:with-option name="output-base-uri" select="concat($output-dir,'text.res')"/>
		</px:daisy3-create-res-file>
		<p:sink/>

		<p:documentation>
			Filter resources
		</p:documentation>
		<!--
		    Remove resources that were referenced by the HTML but not by the DTBook. (We're assuming
		    that these files are not referenced elsewhere.)
		-->
		<p:group name="unreferenced-resources">
			<p:output port="result"/>
			<px:fileset-load media-types="application/x-dtbook+xml">
				<p:input port="fileset">
					<p:pipe step="dtbook" port="fileset"/>
				</p:input>
				<p:input port="in-memory">
					<p:pipe step="dtbook" port="in-memory"/>
				</p:input>
			</px:fileset-load>
			<p:for-each>
				<px:dtbook-load/>
			</p:for-each>
			<px:fileset-join name="dtbook-fileset"/>
			<p:sink/>
			<p:for-each>
				<p:iteration-source>
					<p:pipe step="html" port="in-memory"/>
				</p:iteration-source>
				<px:html-to-fileset>
					<p:input port="context.fileset">
						<p:pipe step="copy" port="result.fileset"/>
					</p:input>
					<p:input port="context.in-memory">
						<p:pipe step="copy" port="result.in-memory"/>
					</p:input>
				</px:html-to-fileset>
			</p:for-each>
			<px:fileset-join name="html-fileset"/>
			<px:fileset-diff>
				<p:input port="secondary">
					<p:pipe step="dtbook-fileset" port="result"/>
				</p:input>
			</px:fileset-diff>
		</p:group>
		<p:sink/>
		<!--
		    Remove audio files that were referenced by SMILs in the DAISY 2.02 but not in the DAISY
		    3, because the corresponding text was omitted. (We're assuming that these files are not
		    referenced elsewhere.)
		-->
		<p:group name="unreferenced-audio-files">
			<p:output port="result"/>
			<p:for-each>
				<p:iteration-source>
					<p:pipe step="smil" port="in-memory"/>
				</p:iteration-source>
				<px:smil-to-audio-fileset/>
			</p:for-each>
			<px:fileset-join name="daisy3-audio-fileset"/>
			<p:sink/>
			<p:for-each>
				<p:iteration-source>
					<p:pipe step="smil" port="daisy202-smils"/>
				</p:iteration-source>
				<px:smil-to-audio-fileset/>
			</p:for-each>
			<px:fileset-join name="daisy202-audio-fileset"/>
			<px:fileset-diff>
				<p:input port="secondary">
					<p:pipe step="daisy3-audio-fileset" port="result"/>
				</p:input>
			</px:fileset-diff>
		</p:group>
		<p:sink/>
		<px:fileset-filter name="resources"
		                   not-media-types="application/xhtml+xml
		                                    application/smil+xml">
			<p:input port="source">
				<p:pipe step="copy" port="result.fileset"/>
			</p:input>
			<p:input port="source.in-memory">
				<p:pipe step="copy" port="result.in-memory"/>
			</p:input>
		</px:fileset-filter>
		<px:fileset-diff>
			<p:input port="secondary">
				<p:pipe step="unreferenced-resources" port="result"/>
			</p:input>
		</px:fileset-diff>
		<px:fileset-diff name="referenced-resources">
			<p:input port="secondary">
				<p:pipe step="unreferenced-audio-files" port="result"/>
			</p:input>
		</px:fileset-diff>
		<p:sink/>

		<p:documentation>
			Create OPF
		</p:documentation>
		<pxi:ncc-to-oebps-metadata name="opf-metadata" px:progress="0.1">
			<p:input port="source">
				<p:pipe step="ncc" port="result"/>
			</p:input>
		</pxi:ncc-to-oebps-metadata>
		<p:sink/>
		<p:group name="daisy3-without-opf">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="resources" port="result.in-memory"/>
				<p:pipe step="dtbook" port="in-memory"/>
				<p:pipe step="smil" port="in-memory"/>
				<p:pipe step="ncx" port="in-memory"/>
				<p:pipe step="res" port="result.in-memory"/>
			</p:output>
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="referenced-resources" port="result"/>
					<p:pipe step="dtbook" port="fileset"/>
					<p:pipe step="smil" port="fileset"/>
					<p:pipe step="ncx" port="fileset"/>
					<p:pipe step="res" port="result.fileset"/>
				</p:input>
			</px:fileset-join>
		</p:group>
		<px:daisy3-create-opf name="opf" px:progress="0.2" px:message="Creating OPF">
			<p:input port="source.in-memory">
				<p:pipe step="daisy3-without-opf" port="in-memory"/>
			</p:input>
			<p:with-option name="uid" select="$daisy3-identifier"/>
			<p:with-option name="output-base-uri" select="resolve-uri('package.opf',$output-dir)"/>
			<p:input port="metadata">
				<p:pipe step="opf-metadata" port="result"/>
			</p:input>
			<p:input port="dc-metadata">
				<p:empty/>
			</p:input>
		</px:daisy3-create-opf>
		<p:sink/>
		<p:group name="add-opf">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="daisy3-without-opf" port="in-memory"/>
				<p:pipe step="opf" port="result"/>
			</p:output>
			<px:fileset-join>
				<p:input port="source">
					<p:pipe step="daisy3-without-opf" port="fileset"/>
					<p:pipe step="opf" port="result.fileset"/>
				</p:input>
			</px:fileset-join>
		</p:group>
	</p:group>

</p:declare-step>
