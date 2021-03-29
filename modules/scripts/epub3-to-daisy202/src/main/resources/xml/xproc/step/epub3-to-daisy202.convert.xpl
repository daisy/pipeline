<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                type="px:epub3-to-daisy202" name="main">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result.fileset" primary="true">
        <p:pipe step="move" port="result.fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="move" port="result.in-memory"/>
    </p:output>

    <p:option name="bundle-dtds" select="'false'"/>
    <p:option name="output-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-filter
            px:fileset-load
            px:fileset-rebase
            px:fileset-copy
            px:fileset-update
            px:fileset-join
            px:fileset-add-entry
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
            px:html-upgrade
            px:html-downgrade
            px:html-outline
            px:html-merge
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl">
        <p:documentation>
            px:daisy202-rename-files
            px:daisy202-update-links
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-downgrade
        </p:documentation>
    </p:import>
    <p:import href="create-ncc.xpl">
        <p:documentation>
            pxi:create-ncc
        </p:documentation>
    </p:import>
    
    <p:documentation>
        Extract and verify the OPF.
    </p:documentation>
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert test-count-min="1" test-count-max="1" error-code="PED01" message="The EPUB must contain exactly one OPF document"/>
    <px:assert error-code="PED02" message="There must be at least one dc:identifier meta element in the OPF document">
        <p:with-option name="test" select="exists(/opf:package/opf:metadata/dc:identifier)"/>
    </px:assert>
    <p:identity name="opf"/>
    <p:sink/>

    <p:documentation>
        Read the "page-list" navigation and label page break elements with epub:type="pagebreak".
    </p:documentation>
    <px:epub3-label-pagebreaks-from-nav name="label-pagebreaks-from-nav">
        <p:input port="source.fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:epub3-label-pagebreaks-from-nav>

    <p:documentation>
        Sort HTML files by spine order and delete HTML files that are not in spine. Note that the
        navigation document may be part of the spine, in which case there will be the original
        navigation document as well as the generated NCC.
    </p:documentation>
    <p:group name="filter-spine">
        <p:output port="result"/>
        <px:opf-spine-to-fileset name="epub3.spine">
            <p:documentation>
                Get spine.
            </p:documentation>
            <p:input port="source.in-memory">
                <p:pipe step="opf" port="result"/>
            </p:input>
        </px:opf-spine-to-fileset>
        <px:fileset-filter media-types="application/xhtml+xml" name="epub3.spine.xhtml"/>
        <p:sink/>
        <px:fileset-filter media-types="application/xhtml+xml" name="epub3.xhtml">
            <p:input port="source">
                <p:pipe step="label-pagebreaks-from-nav" port="result.fileset"/>
            </p:input>
        </px:fileset-filter>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="epub3.spine.xhtml" port="result"/>
                <p:pipe step="epub3.xhtml" port="not-matched"/>
            </p:input>
        </px:fileset-join>
    </p:group>

    <p:documentation>
        Convert from EPUB 3 HTML to DAISY 2.02 HTML and identify page break elements.
    </p:documentation>
    <p:group name="convert-html" px:progress="1/5">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="update" port="result.in-memory"/>
        </p:output>
        <p:output port="page-list">
            <p:pipe step="identify-pagebreaks-and-noterefs" port="page-list"/>
        </p:output>
        <p:output port="noteref-list">
            <p:pipe step="identify-pagebreaks-and-noterefs" port="noteref-list"/>
        </p:output>
        <px:fileset-load media-types="application/xhtml+xml" name="epub3.xhtml">
            <p:documentation>
                Load content documents.
            </p:documentation>
            <p:input port="in-memory">
                <p:pipe step="label-pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:group name="identify-pagebreaks-and-noterefs">
            <p:documentation>Identify page break elements and noteref elements</p:documentation>
            <p:output port="content-docs" primary="true" sequence="true">
                <p:pipe step="content-docs" port="result"/>
            </p:output>
            <p:output port="page-list">
                <p:pipe step="page-list" port="result"/>
            </p:output>
            <p:output port="noteref-list">
                <p:pipe step="noteref-list" port="result"/>
            </p:output>
            <p:for-each name="content-docs">
                <p:output port="result" primary="true"/>
                <p:output port="page-and-noteref-lists" sequence="true">
                    <p:pipe step="xslt" port="secondary"/>
                </p:output>
                <p:label-elements match="*[@role='doc-pagebreak']" attribute="epub:type" replace="true"
                                  label="string-join(distinct-values((@epub:type/tokenize(.,'\s+')[not(.='')],'pagebreak')),' ')"
                                  name="handle-dpub-aria">
                    <!-- Convert DPUB-ARIA roles to epub:type -->
                </p:label-elements>
                <p:sink/>
                <p:xslt name="xslt">
                    <p:input port="source">
                        <p:pipe step="handle-dpub-aria" port="result"/>
                        <p:pipe step="label-pagebreaks-from-nav" port="page-list"/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/identify-pagebreaks-and-noterefs.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="base-uri(/*)">
                        <p:pipe step="content-docs" port="current"/>
                    </p:with-option>
                </px:set-base-uri>
            </p:for-each>
            <p:sink/>
            <p:count>
                <p:input port="source">
                    <p:pipe step="label-pagebreaks-from-nav" port="page-list"/>
                </p:input>
            </p:count>
            <p:choose name="page-list">
                <p:when test="/*=1">
                    <p:documentation>Get them from the navigation document.</p:documentation>
                    <p:output port="result"/>
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe step="label-pagebreaks-from-nav" port="page-list"/>
                        </p:input>
                    </p:identity>
                    <p:add-attribute match="d:anchor" attribute-name="class" attribute-value="page"/>
                </p:when>
                <p:otherwise>
                    <p:documentation>Get them from epub:type="pagebreak" markup.</p:documentation>
                    <p:output port="result"/>
                    <p:sink/>
                    <p:wrap-sequence wrapper="d:fileset">
                        <p:input port="source">
                            <p:pipe step="content-docs" port="page-and-noteref-lists"/>
                        </p:input>
                    </p:wrap-sequence>
                    <p:delete match="d:anchor[not(@class=('page-normal','page-front','page-special'))]"/>
                </p:otherwise>
            </p:choose>
            <p:sink/>
            <p:group name="noteref-list">
                <p:output port="result"/>
                <p:wrap-sequence wrapper="d:fileset">
                    <p:input port="source">
                        <p:pipe step="content-docs" port="page-and-noteref-lists"/>
                    </p:input>
                </p:wrap-sequence>
                <p:delete match="d:anchor[not(@class=('noteref'))]"/>
            </p:group>
            <p:sink/>
        </p:group>
        <p:for-each px:message="Converting HTML5 to HTML4" px:progress="1">
            <p:variable name="base" select="base-uri()"/>
            <p:variable name="href" select="//d:file[resolve-uri(@href,base-uri(.))=$base]/@href">
                <p:pipe step="epub3.xhtml" port="result.fileset"/>
            </p:variable>
            <p:identity px:message="Processing {$href}"/>
            <px:html-upgrade>
                <p:documentation>Normalize HTML5.</p:documentation>
                <!-- hopefully this preserves all IDs -->
            </px:html-upgrade>
            <px:html-downgrade>
                <p:documentation>Downgrade to HTML4. This preserves all ID.</p:documentation>
            </px:html-downgrade>
            <px:html-outline fix-heading-ranks="outline-depth">
                <p:documentation>Make sure heading hierarchy is correct in output</p:documentation>
                <!-- Note that this is already done once in px:html-downgrade but we do it a second time
                     after the sectioning elements have been converted, so that if the first heading is
                     a h2 everything shifts up one level. -->
            </px:html-outline>
        </p:for-each>
        <p:identity name="daisy202.xhtml.in-memory"/>
        <p:sink/>
        <px:fileset-update name="update">
            <p:input port="source.fileset">
                <p:pipe step="filter-spine" port="result"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="label-pagebreaks-from-nav" port="result.in-memory"/>
            </p:input>
            <p:input port="update.fileset">
                <p:pipe step="epub3.xhtml" port="result.fileset"/>
            </p:input>
            <p:input port="update.in-memory">
                <p:pipe port="result" step="daisy202.xhtml.in-memory"/>
            </p:input>
        </px:fileset-update>
    </p:group>

    <p:documentation>
        Convert from EPUB 3 SMIL to DAISY 2.02 SMIL.
    </p:documentation>
    <p:group name="convert-smil" px:progress="1/5">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="update" port="result.in-memory"/>
        </p:output>
        <px:fileset-load media-types="application/smil+xml" name="epub3.smil">
            <p:documentation>
                Load SMIL files.
            </p:documentation>
            <p:input port="in-memory">
                <p:pipe step="convert-html" port="in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:for-each px:message="Converting SMIL 3.0 to SMIL 1.0" px:progress="1">
            <p:variable name="smil-base" select="base-uri(/*)"/>
            <p:variable name="smil-href" select="//d:file[resolve-uri(@href,base-uri(.))=$smil-base]/@href">
                <p:pipe step="epub3.smil" port="result.fileset"/>
            </p:variable>
            <p:group px:progress="1" px:message="Processing {$smil-href}">
                <p:identity name="smil-without-system-required"/>
                <p:sink/>
                <p:xslt>
                    <p:documentation>
                        Add systemRequired attributes (which will be converted to system-required)
                    </p:documentation>
                    <p:input port="source">
                        <p:pipe step="smil-without-system-required" port="result"/>
                        <p:pipe step="convert-html" port="page-list"/>
                        <p:pipe step="convert-html" port="noteref-list"/>
                        <p:pipe step="label-pagebreaks-from-nav" port="result.in-memory"/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/make-skippables.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <px:smil-downgrade version="1.0" px:progress=".5"/>
            </p:group>
        </p:for-each>
        <p:identity name="daisy202.smil.in-memory"/>
        <p:sink/>
        <px:fileset-update name="update">
            <p:input port="source.fileset">
                <p:pipe step="convert-html" port="fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="convert-html" port="in-memory"/>
            </p:input>
            <p:input port="update.fileset">
                <p:pipe step="epub3.smil" port="result.fileset"/>
            </p:input>
            <p:input port="update.in-memory">
                <p:pipe port="result" step="daisy202.smil.in-memory"/>
            </p:input>
        </px:fileset-update>
    </p:group>

    <p:documentation>
        Create DAISY 2.02 fileset
    </p:documentation>
    <p:choose>
        <p:when test="//d:file[matches(@href,'^(.+/)?mimetype$')]">
            <p:documentation>
                Make the base URI the directory containing the mimetype file.
            </p:documentation>
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
    <p:delete match="//d:file[@media-type=('application/oebps-package+xml',
                                           'application/x-dtbncx+xml')
                              or starts-with(@href,'..')
                              or starts-with(@href,'META-INF/')
                              or @href='mimetype']">
        <p:documentation>
            - Delete package document (OPF).
            - Delete table of contents (NCX).
            - Delete mimetype and META-INF/.
            - Delete files outside of the directory that contains the mimetype.
        </p:documentation>
    </p:delete>
    <p:documentation>
        Create NCC file.
    </p:documentation>
    <pxi:create-ncc name="create-ncc" px:message="Creating NCC" px:progress="2/5">
        <p:input port="source.in-memory">
            <p:pipe step="convert-smil" port="in-memory"/>
        </p:input>
        <p:input port="opf">
            <p:pipe step="opf" port="result"/>
        </p:input>
        <p:input port="page-list">
            <p:pipe step="convert-html" port="page-list"/>
        </p:input>
        <p:input port="noteref-list">
            <p:pipe step="convert-html" port="noteref-list"/>
        </p:input>
    </pxi:create-ncc>

    <p:documentation>
        Move notes after their corresponding note refs in the media overlays.
    </p:documentation>
    <p:group name="rearrange-notes">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="update" port="result.in-memory"/>
        </p:output>
        <px:fileset-load media-types="application/smil+xml" name="smil">
            <p:input port="in-memory">
                <p:pipe step="create-ncc" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:for-each name="rearrange-smil">
            <p:output port="result"/>
            <p:sink/>
            <p:xslt>
                <p:input port="source">
                    <p:pipe step="rearrange-smil" port="current"/>
                    <p:pipe step="convert-html" port="noteref-list">
                        <!-- assumes convert-smil and create-ncc do not change base URIs of content
                             documents and IDs of noteref and note elements -->
                    </p:pipe>
                    <p:pipe step="create-ncc" port="result.in-memory"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/rearrange-notes.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:for-each>
        <p:sink/>
        <px:fileset-update name="update">
            <p:input port="source.fileset">
                <p:pipe step="create-ncc" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="create-ncc" port="result.in-memory"/>
            </p:input>
            <p:input port="update.fileset">
                <p:pipe step="smil" port="result.fileset"/>
            </p:input>
            <p:input port="update.in-memory">
                <p:pipe step="rearrange-smil" port="result"/>
            </p:input>
        </px:fileset-update>
    </p:group>

    <p:documentation>
        Merge into single HTML document (workaround for Voice Dream Reader)
    </p:documentation>
    <p:group name="voice-dream-workaround" px:message="Merging HTML documents" px:progress="1/5">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="choose" port="in-memory"/>
        </p:output>
        <px:fileset-filter href="*/ncc.html" name="ncc">
            <p:input port="source.in-memory">
                <p:pipe step="rearrange-notes" port="in-memory"/>
            </p:input>
        </px:fileset-filter>
        <p:sink/>
        <px:fileset-filter media-types="application/xhtml+xml" name="daisy202.html">
            <p:input port="source">
                <p:pipe step="ncc" port="not-matched"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="ncc" port="not-matched.in-memory"/>
            </p:input>
        </px:fileset-filter>
        <p:choose name="choose">
            <p:when test="count(//d:file)&gt;1">
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="add-entry" port="result.in-memory"/>
                </p:output>
                <px:fileset-load>
                    <p:input port="in-memory">
                        <p:pipe step="rearrange-notes" port="in-memory"/>
                    </p:input>
                </px:fileset-load>
                <px:html-merge name="merge">
                    <p:with-option name="output-base-uri" select="resolve-uri('content.html',base-uri(/*))">
                        <p:pipe step="opf" port="result"/>
                    </p:with-option>
                </px:html-merge>
                <p:unwrap match="html:section" name="html4">
                    <!-- added by px:html-merge -->
                </p:unwrap>
                <p:sink/>
                <px:fileset-join>
                    <p:input port="source">
                        <p:pipe step="ncc" port="result"/>
                        <p:pipe step="daisy202.html" port="not-matched"/>
                    </p:input>
                </px:fileset-join>
                <px:daisy202-update-links name="update-links">
                    <p:input port="source.in-memory">
                        <p:pipe step="ncc" port="result.in-memory"/>
                        <p:pipe step="daisy202.html" port="not-matched.in-memory"/>
                    </p:input>
                    <p:input port="mapping">
                        <p:pipe step="merge" port="mapping"/>
                    </p:input>
                </px:daisy202-update-links>
                <px:fileset-add-entry media-type="application/xhtml+xml" name="add-entry">
                    <p:input port="source.in-memory">
                        <p:pipe step="update-links" port="result.in-memory"/>
                    </p:input>
                    <p:input port="entry">
                        <p:pipe step="html4" port="result"/>
                    </p:input>
                </px:fileset-add-entry>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="rearrange-notes" port="in-memory"/>
                </p:output>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="rearrange-notes" port="fileset"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:group>

    <p:documentation>
        Rename content documents to .html.
    </p:documentation>
    <p:group name="rename-xhtml" px:message="Renaming content documents to .html" px:progress="1/5">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="rename" port="result.in-memory"/>
        </p:output>
        <px:fileset-filter media-types="application/xhtml+xml"/>
        <p:label-elements match="d:file" attribute="original-href" replace="true"
                          label="resolve-uri(@href,base-uri(.))"/>
        <p:label-elements match="d:file" attribute="href" replace="true"
                          label="replace(@href,'^(.*)\.([^/\.]*)$','$1.html')"/>
        <p:delete match="/*/*[not(self::d:file)]"/>
        <p:delete match="d:file/@*[not(name()=('href','original-href'))]" name="rename-xhtml-mapping"/>
        <p:sink/>
        <px:daisy202-rename-files name="rename">
            <p:input port="source.fileset">
                <p:pipe step="voice-dream-workaround" port="fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="voice-dream-workaround" port="in-memory"/>
            </p:input>
            <p:input port="mapping">
                <p:pipe step="rename-xhtml-mapping" port="result"/>
            </p:input>
        </px:daisy202-rename-files>
    </p:group>

    <p:documentation>
        Flatten DAISY 2.02 directory structure.
        Don't rename CSS, images and font files because that could break links in CSS.
    </p:documentation>
    <p:group name="flatten-daisy202">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="rename" port="result.in-memory"/>
        </p:output>
        <px:fileset-filter media-types="application/xhtml+xml
                                        application/smil+xml
                                        audio/mpeg
                                        audio/mp4"/>
        <px:fileset-copy flatten="true" dry-run="true" name="flatten">
            <p:with-option name="target" select="base-uri(/*)"/>
        </px:fileset-copy>
        <p:sink/>
        <px:daisy202-rename-files name="rename">
            <p:input port="source.fileset">
                <p:pipe step="rename-xhtml" port="fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="rename-xhtml" port="in-memory"/>
            </p:input>
            <p:input port="mapping">
                <p:pipe step="flatten" port="mapping"/>
            </p:input>
        </px:daisy202-rename-files>
    </p:group>

    <p:documentation>
        Finalize DAISY 2.02 fileset manifest: set DOCTYPE on XHTML and SMIL files
    </p:documentation>
    <p:add-attribute match="d:file[@media-type='application/xhtml+xml']"
                     attribute-name="doctype-public"
                     attribute-value="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
    <p:add-attribute match="d:file[@media-type='application/xhtml+xml']"
                     attribute-name="doctype-system"
                     attribute-value="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
    <p:add-attribute match="d:file[@media-type='application/smil+xml']"
                     attribute-name="doctype-public"
                     attribute-value="-//W3C//DTD SMIL 1.0//EN"/>
    <p:add-attribute match="d:file[@media-type='application/smil+xml']"
                     attribute-name="doctype-system"
                     attribute-value="http://www.w3.org/TR/REC-SMIL/SMIL10.dtd"/>

    <p:documentation>
        Move to final location
    </p:documentation>
    <px:fileset-copy name="move">
        <p:with-option name="target" select="concat($output-dir,replace(/*/@content,'[^a-zA-Z0-9]','_'),'/')">
            <p:pipe step="identifier" port="result"/>
        </p:with-option>
        <p:input port="source.in-memory">
            <p:pipe step="flatten-daisy202" port="in-memory"/>
        </p:input>
    </px:fileset-copy>
    <p:sink/>

    <p:group name="identifier">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:pipe step="create-ncc" port="ncc"/>
            </p:input>
        </p:identity>
        <!--
            these assertions should normally never fail
        -->
        <px:assert test-count-min="1" test-count-max="1" error-code="PED01"
                   message="There must be exactly one ncc.html in the resulting DAISY 2.02 fileset"/>
        <p:filter select="/*/*/*[@name='dc:identifier']"/>
        <px:assert test-count-min="1" error-code="PED02"
                   message="There must be at least one dc:identifier meta element in the resulting ncc.html"/>
        <p:split-sequence test="position()=1"/>
    </p:group>
    <p:sink/>

</p:declare-step>
