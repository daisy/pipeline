<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                type="px:epub3-to-daisy3" name="main">

    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The EPUB 3 fileset</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The DAISY 3 fileset</p>
        </p:documentation>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="daisy3" port="in-memory"/>
    </p:output>

    <p:option name="date" required="false" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Date of publication of the DTB</p>
        <p>Format must be YYYY[-MM[-DD]]</p>
        <p>Defaults to the current date.</p>
      </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Base directory of the DAISY 3.</p>
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
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-join
            px:fileset-filter
            px:fileset-add-entry
            px:fileset-copy
            px:fileset-compose
            px:fileset-diff
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-to-html/library.xpl">
        <p:documentation>
            px:opf-to-html-metadata
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:opf-spine-to-fileset
            px:epub3-label-pagebreaks-from-nav
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-merge
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-to-dtbook/library.xpl">
        <p:documentation>
            px:html-to-dtbook
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-audio-clips
            px:smil-to-audio-fileset
            px:smil-update-links
            px:audio-clips-to-fileset
            px:audio-clips-update-files
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
        <p:documentation>
            px:daisy3-prepare-dtbook
            px:daisy3-create-ncx
            px:daisy3-create-opf
            px:daisy3-create-res-file
            px:daisy3-create-smils
        </p:documentation>
    </p:import>
    <p:import href="page-list-update-links.xpl">
        <p:documentation>
            pxi:page-list-update-links
        </p:documentation>
    </p:import>

    <p:documentation>
        Normalize EPUB 3 fileset: use longest common URI as base directory
    </p:documentation>
    <p:group name="epub3">
        <p:output port="result"/>
        <p:filter select="/*/d:file"/>
        <p:for-each>
            <p:wrap match="/*" wrapper="d:fileset"/>
            <p:label-elements match="/*" attribute="xml:base" label="d:file/resolve-uri(@href,base-uri(.))"/>
            <p:label-elements match="/*/d:file" attribute="href" label="replace(@href,'^(.*/)?([^/]+)$','$2')"/>
        </p:for-each>
        <px:fileset-join/>
        <!-- delete mimetype and META-INF -->
        <p:delete match="d:file[@href='mimetype' or starts-with(@href,'META-INF/')]"/>
    </p:group>

    <p:documentation>
        Load package document
    </p:documentation>
    <p:group name="epub3-opf">
        <p:output port="result"/>
        <px:fileset-load media-types="application/oebps-package+xml">
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:assert test-count-min="1" test-count-max="1" error-code="XXXXX" message="The EPUB must contain exactly one OPF document"/>
        <px:assert error-code="XXXX" message="There must be at least one dc:identifier meta element in the OPF document">
            <p:with-option name="test" select="exists(/opf:package/opf:metadata/dc:identifier)"/>
        </px:assert>
    </p:group>
    <p:sink/>

    <p:documentation>
        Read the "page-list" navigation and label page break elements with epub:type="pagebreak".
    </p:documentation>
    <px:epub3-label-pagebreaks-from-nav name="pagebreaks-from-nav">
        <p:input port="source.fileset">
            <p:pipe step="epub3" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:epub3-label-pagebreaks-from-nav>

    <p:documentation>
        Get the spine
    </p:documentation>
    <p:group name="spine">
        <p:output port="result"/>
        <px:opf-spine-to-fileset>
            <p:input port="source.in-memory">
                <p:pipe step="pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
        </px:opf-spine-to-fileset>
        <!-- because spine could contain non-HTML files -->
        <px:fileset-filter media-types="application/xhtml+xml"/>
    </p:group>

    <p:documentation>
        Merge into a single HTML
    </p:documentation>
    <p:group name="merge-html" px:message="Merging HTML documents" px:progress="1/10">
        <p:output port="result" primary="true"/>
        <p:output port="mapping">
            <p:pipe step="merge" port="mapping"/>
        </p:output>
        <px:fileset-load>
            <p:input port="in-memory">
                <p:pipe step="pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:html-merge name="merge">
            <p:with-option name="output-base-uri" select="resolve-uri('content.html',base-uri(/*))">
                <p:pipe step="pagebreaks-from-nav" port="result.fileset"/>
            </p:with-option>
        </px:html-merge>
    </p:group>
    <p:sink/>

    <p:documentation>
        Create head element from OPF
    </p:documentation>
    <p:group name="html-with-head" px:message="Converting metadata">
        <p:output port="result"/>
        <px:opf-to-html-metadata name="head">
            <p:input port="source">
                <p:pipe step="epub3-opf" port="result"/>
            </p:input>
        </px:opf-to-html-metadata>
        <p:sink/>
        <p:delete match="/*/html:head">
            <p:input port="source">
                <p:pipe step="merge-html" port="result"/>
            </p:input>
        </p:delete>
        <p:insert match="/*" position="first-child">
            <p:input port="insertion">
                <p:pipe step="head" port="result"/>
            </p:input>
        </p:insert>
    </p:group>
    <p:sink/>

    <p:documentation>
        Combine the merged HTML with the resources (everything except HTML, SMIL, OPF and NCX)
    </p:documentation>
    <p:group name="html-and-resources">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="add-html" port="result.in-memory"/>
        </p:output>
        <px:fileset-filter not-media-types="application/oebps-package+xml
                                            application/xhtml+xml
                                            application/smil+xml
                                            application/x-dtbncx+xml"
                           name="resources">
            <p:input port="source">
                <p:pipe step="pagebreaks-from-nav" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
        </px:fileset-filter>
        <px:fileset-add-entry name="add-html" media-type="application/xhtml+xml">
            <p:input port="source.in-memory">
                <p:pipe step="resources" port="result.in-memory"/>
            </p:input>
            <p:input port="entry">
                <p:pipe step="html-with-head" port="result"/>
            </p:input>
            <p:with-param port="file-attributes" name="media-version" select="'5.0'"/>
        </px:fileset-add-entry>
    </p:group>

    <p:documentation>
        Copy to new location
    </p:documentation>
    <px:fileset-copy name="copy">
        <p:with-option name="target" select="$output-dir"/>
        <p:input port="source.in-memory">
            <p:pipe step="html-and-resources" port="in-memory"/>
        </p:input>
    </px:fileset-copy>

    <p:documentation>
        Convert HTML to DTBook
    </p:documentation>
    <px:html-to-dtbook name="dtbook-and-resources" imply-headings="true" px:message="Converting HTML to DTBook" px:progress="3/10">
        <p:input port="source.in-memory">
            <p:pipe step="copy" port="result.in-memory"/>
        </p:input>
    </px:html-to-dtbook>
    <p:sink/>

    <p:documentation>
        Total mapping of the HTML merge, the copy and the DTBook conversion. Assumes that IDs were
        preserved in the DTBook conversion.
    </p:documentation>
    <px:fileset-compose name="total-mapping">
        <p:input port="source">
            <p:pipe step="merge-html" port="mapping"/>
            <p:pipe step="copy" port="mapping"/>
            <p:pipe step="dtbook-and-resources" port="mapping"/>
        </p:input>
    </px:fileset-compose>
    <p:sink/>

    <p:documentation>
        Convert EPUB 3 SMILs to d:audio-clips document
    </p:documentation>
    <p:group name="audio" px:progress="1/10">
        <p:output port="fileset" primary="true"/>
        <p:output port="clips">
            <p:pipe step="clips" port="result"/>
        </p:output>
        <px:fileset-load media-types="application/smil+xml" name="smil">
            <p:input port="fileset">
                <p:pipe step="pagebreaks-from-nav" port="result.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:for-each>
            <px:smil-update-links>
                <p:input port="mapping">
                    <p:pipe step="total-mapping" port="result"/>
                </p:input>
            </px:smil-update-links>
        </p:for-each>
        <px:smil-to-audio-clips name="clips">
            <p:with-option name="output-base-uri" select="$output-dir">
                <p:empty/>
            </p:with-option>
        </px:smil-to-audio-clips>
        <px:audio-clips-to-fileset/>
    </p:group>
    <p:sink/>

    <p:documentation>
        List of pagebreak elements in the DTBook
    </p:documentation>
    <p:for-each name="dtbook-page-list">
        <p:iteration-source>
            <p:pipe step="pagebreaks-from-nav" port="page-list"/>
        </p:iteration-source>
        <p:output port="result"/>
        <pxi:page-list-update-links>
            <p:input port="mapping">
                <p:pipe step="total-mapping" port="result"/>
            </p:input>
        </pxi:page-list-update-links>
    </p:for-each>
    <p:sink/>

    <p:documentation>
        Create DAISY 3
    </p:documentation>
    <p:group name="daisy3" px:progress="5/10">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="daisy3.in-memory" port="result"/>
        </p:output>
        <p:variable name="uid" select="/opf:package/opf:metadata/dc:identifier">
            <p:pipe step="epub3-opf" port="result"/>
        </p:variable>

        <p:documentation>
            Prepare DTBook for DAISY 3
        </p:documentation>
        <px:fileset-filter media-types="application/x-dtbook+xml" name="dtbook">
            <p:input port="source">
                <p:pipe step="dtbook-and-resources" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="dtbook-and-resources" port="result.in-memory"/>
            </p:input>
        </px:fileset-filter>
        <p:sink/>
        <!--
            First unwrap all child elements within heading elements. This is workaround for a Voice
            Dream Reader bug.

            FIXME: if we'd do this after the NCX has been created, page numbers inside headings
            would still be in the page list (but the page list would link to the heading).
        -->
        <p:group name="voice-dream-workaround">
            <p:output port="dtbook" primary="true"/>
            <p:output port="audio-clips">
                <p:pipe step="audio-clips" port="result"/>
            </p:output>
            <p:output port="page-list" sequence="true">
                <p:pipe step="page-list" port="result"/>
            </p:output>
            <p:xslt name="xslt">
                <p:input port="source">
                    <p:pipe step="dtbook" port="result.in-memory"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="flatten-headings.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <p:sink/>
            <px:set-base-uri name="mapping">
                <p:input port="source">
                    <p:pipe step="xslt" port="secondary"/>
                </p:input>
                <p:with-option name="base-uri" select="base-uri(/*)">
                    <p:pipe step="audio" port="clips"/>
                </p:with-option>
            </px:set-base-uri>
            <p:sink/>
            <px:audio-clips-update-files name="audio-clips">
                <p:input port="source">
                    <p:pipe step="audio" port="clips"/>
                </p:input>
                <p:input port="mapping">
                    <p:pipe step="mapping" port="result"/>
                </p:input>
            </px:audio-clips-update-files>
            <p:sink/>
            <p:for-each name="page-list">
                <p:iteration-source>
                    <p:pipe step="dtbook-page-list" port="result"/>
                </p:iteration-source>
                <p:output port="result" sequence="true"/>
                <pxi:page-list-update-links>
                    <p:input port="mapping">
                        <p:pipe step="mapping" port="result"/>
                    </p:input>
                </pxi:page-list-update-links>
            </p:for-each>
            <p:sink/>
            <px:set-base-uri>
                <p:input port="source">
                    <p:pipe step="xslt" port="result"/>
                </p:input>
                <p:with-option name="base-uri" select="base-uri(/*)">
                    <p:pipe step="dtbook" port="result.in-memory"/>
                </p:with-option>
            </px:set-base-uri>
        </p:group>
        <px:daisy3-prepare-dtbook name="daisy3-dtbook">
            <p:with-option name="uid" select="$uid"/>
        </px:daisy3-prepare-dtbook>

        <p:documentation>
            Create DAISY 3 SMILs
        </p:documentation>
        <px:daisy3-create-smils audio-only="false" name="mo" px:message="Creating SMIL files" px:progress="3/5">
            <p:with-option name="uid" select="$uid"/>
            <p:with-option name="smil-dir" select="$output-dir"/>
            <p:input port="source.in-memory">
                <p:pipe step="daisy3-dtbook" port="result.in-memory"/>
            </p:input>
            <p:input port="audio-map">
                <p:pipe step="voice-dream-workaround" port="audio-clips"/>
            </p:input>
        </px:daisy3-create-smils>
        <p:sink/>

        <p:documentation>
            Create NCX document
        </p:documentation>
        <!--
            FIXME: Instead of failing when a heading or pagenum element has no smilref attribute,
            ensure that it has one when it leaves px:daisy3-create-smils. We're currently depending
            on the input EPUB to not have page break elements within narrated sentences.
        -->
        <px:daisy3-create-ncx name="ncx" fail-if-missing-smilref="true" px:message="Creating NCX file" px:progress="1/5">
            <p:with-option name="uid" select="$uid"/>
            <p:with-option name="ncx-dir" select="$output-dir"/>
            <p:input port="content">
                <p:pipe step="mo" port="dtbook.in-memory"/>
            </p:input>
            <p:input port="audio-map">
                <p:pipe step="voice-dream-workaround" port="audio-clips"/>
            </p:input>
            <p:input port="page-list">
                <p:pipe step="voice-dream-workaround" port="page-list"/>
            </p:input>
        </px:daisy3-create-ncx>
        <p:sink/>

        <p:documentation>
            Create resources file
        </p:documentation>
        <px:daisy3-create-res-file name="res">
            <p:with-option name="output-base-uri" select="concat($output-dir,'resources.res')"/>
        </px:daisy3-create-res-file>

        <p:documentation>
            Create package document
        </p:documentation>
        <!--
            Remove resources that were referenced by the HTML but not by the DTBook. (We're assuming
            that these files are not referenced elsewhere.)
        -->
        <px:dtbook-load name="dtbook-fileset">
            <p:input port="source.fileset">
                <p:pipe step="dtbook-and-resources" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="daisy3-dtbook" port="result.in-memory"/>
            </p:input>
        </px:dtbook-load>
        <p:sink/>
        <px:html-load name="html-fileset">
            <p:input port="source.fileset">
                <p:pipe step="copy" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="copy" port="result.in-memory"/>
            </p:input>
        </px:html-load>
        <px:fileset-diff name="unreferenced-resources">
            <p:input port="secondary">
                <p:pipe step="dtbook-fileset" port="result.fileset"/>
            </p:input>
        </px:fileset-diff>
        <p:sink/>
        <!--
            Remove audio files that were referenced by SMILs in the EPUB 3 but not in the DAISY 3,
            because the corresponding text was omitted. (We're assuming that these files are not
            referenced elsewhere.)
        -->
        <p:for-each>
            <p:iteration-source>
                <p:pipe step="mo" port="smil.in-memory"/>
            </p:iteration-source>
            <px:smil-to-audio-fileset/>
        </p:for-each>
        <px:fileset-join name="referenced-audio-files"/>
        <p:sink/>
        <px:fileset-diff name="unreferenced-audio-files">
            <p:input port="source">
                <p:pipe step="audio" port="fileset"/>
            </p:input>
            <p:input port="secondary">
                <p:pipe step="referenced-audio-files" port="result"/>
            </p:input>
        </px:fileset-diff>
        <p:sink/>
        <px:fileset-diff>
            <p:input port="source">
                <p:pipe step="dtbook" port="not-matched"/>
            </p:input>
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
        <px:fileset-join name="daisy3-without-opf">
            <p:input port="source">
                <p:pipe step="mo" port="result.fileset"/>
                <p:pipe step="ncx" port="result.fileset"/>
                <p:pipe step="res" port="result.fileset"/>
                <p:pipe step="referenced-resources" port="result"/>
            </p:input>
        </px:fileset-join>
        <px:daisy3-create-opf name="daisy3-opf" px:message="Creating OPF file" px:progress="1/5">
            <p:input port="source.in-memory">
                <p:pipe step="mo" port="result.in-memory"/>
            </p:input>
            <p:with-option name="output-base-uri" select="concat($output-dir, 'book.opf')"/>
            <p:with-option name="uid" select="$uid"/>
            <p:with-param port="dc-metadata" name="dc:Date" select="$date"/>
        </px:daisy3-create-opf>
        <p:sink/>
        <p:identity name="daisy3.in-memory">
            <p:input port="source">
                <p:pipe step="mo" port="result.in-memory"/>
                <p:pipe step="ncx" port="result"/>
                <p:pipe step="res" port="result.in-memory"/>
                <p:pipe step="daisy3-opf" port="result"/>
                <p:pipe step="dtbook" port="not-matched.in-memory"/>
            </p:input>
        </p:identity>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="daisy3-without-opf" port="result"/>
                <p:pipe step="daisy3-opf" port="result.fileset"/>
            </p:input>
        </px:fileset-join>
    </p:group>

</p:declare-step>
