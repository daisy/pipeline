<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub3-create-package-doc" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Create a <a href="http://www.idpf.org/epub/301/spec/epub-publications.html">EPUB Package
        Document</a></p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Files to be included in the publication: <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub">content
            documents</a>, <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-media-overlay-document">media
            overlay documents</a> and other resources.</p>
            <p>If this fileset includes a navigation document, it should be marked with a
            <code>role</code> attribute with value <code>nav</code>, and this file should be a
            content document. At most one navigation document may be specified. If no navigation
            document is specified, the content document that contains a
            <code>nav[@epub:type='toc']</code> element is picked. It is an error if there is no such
            document.</p>
            <p>If the fileset includes a <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#cover-image"><code>cover-image</code></a>,
            it should be marked with a <code>role</code> attribute with value
            <code>cover-image</code>.</p>
        </p:documentation>
    </p:input>

    <p:input port="spine" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset that will make up the primary <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-spine-elem">spine</a>
            items.</p>
            <p>The order of the spine items is determined by the "source.fileset" input. Items that
            are not <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub">content
            documents</a> are omitted.</p>
            <!-- The latter is because the EPUB spec says: "Each referenced manifest item [from the
                 spine] must be either a) an EPUB Content Document or b) another type of Publication
                 Resource which, regardless of whether it is a Core Media Type Resource or a Foreign
                 Resource, must include an EPUB Content Document in its fallback chain." For now we
                 don't provide a way to specify fallbacks, so we have to disallow non-content
                 documents altogether. -->
            <p>If not specified, defaults to all content documents except the navigation
            document.</p>
            <p>The content documents in "source.fileset" that are not in "spine" and are not the
            navigation document become auxiliary spine items.</p>
            <!-- This is because the EPUB spec says: "All EPUB Content Documents that are linked to
                 from EPUB Content Documents in the spine must themselves be listed in the spine."
                 Rather than checking which documents are references, we simply include all content
                 documents in the spine. -->
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:input port="metadata" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Sequence of <code>metadata</code> elements in the OPF namespace from which the <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-elem"><code>metadata</code>
            element</a> for the package document will be constructed.</p>
            <p>Will be augmented with "duration" metadata that is extracted from the media overlay
            documents and other automatically generated metadata.</p>
            <p>If not specified, a metadata element with the minimal required metadata will be
            included.</p>
        </p:documentation>
        <p:inline>
            <opf:metadata/>
        </p:inline>
    </p:input>
    <p:input port="bindings" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset from which to contruct the <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-bindings-elem"><code>bindings</code></a>
            element of the package document.</p>
            <p>Ignored if compatibility-mode is not true.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:option name="compatibility-mode" required="false" select="'true'" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to be backward compatible with <a
            href="http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm">Open Package Format
            2.0.1</a>.</p>
        </p:documentation>
    </p:option>
    <p:option name="detect-properties" required="false" select="'true'" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to automatically detect <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-item-property-values">manifest
            item properties</a>:</p>
            <ul>
                <ol><a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#mathml"><code>mathml</code></a>:
                when a document contains instances of MathML markup</ol>
                <ol><a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#remote-resources"><code>remote-resources</code></a>:
                when a document contains references to other publication resources that are <a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-resource-locations">located
                outside of the EPUB container</a></ol>
                <ol><a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#scripted"><code>scripted</code></a>:
                when a document is a <a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub-scripted">scripted
                content document</a> (contains scripted content and/or elements from HTML5 forms)</ol>
                <ol><a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#svg"><code>svg</code></a>:
                when a document is a <a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#gloss-content-document-epub-svg">SVG
                content document</a> or contains instances of SVG markup</ol>
                <ol><a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#switch"><code>switch</code></a>:
                when a document contains <a
                href="http://www.idpf.org/epub/301/spec/epub-contentdocs.html#elemdef-switch"><code>epub:switch</code></a>
                elements</ol>
            </ul>
        </p:documentation>
    </p:option>
    <p:option name="reserved-prefixes" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>
                The <a
                href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-default-vocab">reserved
                prefix mappings</a> of the resulting package document. By default, prefixes are
                declared systematically.
            </p>
        </p:documentation>
    </p:option>
    <p:option name="output-base-uri" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI of the result document.</p>
        </p:documentation>
    </p:option>
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The package document.</p>
        </p:documentation>
    </p:output>
    <p:output port="result.fileset">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result fileset with the package document as single file.</p>
        </p:documentation>
        <p:pipe step="result" port="fileset"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
            px:add-xml-base
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-join
            px:fileset-intersect
            px:fileset-diff
            px:fileset-create
            px:fileset-add-entry
            px:fileset-filter-in-memory
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:message
        </p:documentation>
    </p:import>
    <p:import href="../nav/landmarks-to-guide.xpl">
        <p:documentation>
            px:epub-landmarks-to-guide
        </p:documentation>
    </p:import>
    <p:import href="../nav/epub3-nav-create-navigation-doc.xpl">
        <p:documentation>
            px:epub3-add-navigation-doc
        </p:documentation>
    </p:import>
    <p:import href="add-mediaoverlays.xpl">
        <p:documentation>
            px:epub3-add-mediaoverlays
        </p:documentation>
    </p:import>
    <p:import href="add-metadata.xpl">
        <p:documentation>
            px:epub3-add-metadata
        </p:documentation>
    </p:import>
    <p:import href="detect-properties.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            pxi:epub3-detect-properties
        </p:documentation>
    </p:import>

    <px:normalize-uri name="output-base-uri">
        <p:with-option name="href" select="$output-base-uri"/>
    </px:normalize-uri>

    <p:delete match="d:file/@linear"/>
    <px:mediatype-detect>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:mediatype-detect>

    <p:documentation>Filter out SMIL files, they are handled separately in px:epub3-add-mediaoverlays</p:documentation>
    <px:fileset-filter not-media-types="application/smil+xml" name="fileset-except-smil">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-filter>

    <p:documentation>Get content documents</p:documentation>
    <px:fileset-load media-types="application/xhtml+xml image/svg+xml" name="content-docs">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:sink/>

    <p:documentation>Get navigation document</p:documentation>
    <p:group name="nav-doc">
        <p:output port="result" primary="true"/>
        <p:output port="fileset">
            <p:pipe step="choose" port="fileset"/>
        </p:output>
        <p:choose name="choose">
            <p:xpath-context>
                <p:pipe step="content-docs" port="result.fileset"/>
            </p:xpath-context>
            <p:when test="//d:file[@role='nav']">
                <p:output port="result" primary="true"/>
                <p:output port="fileset">
                    <p:pipe step="load" port="result.fileset"/>
                </p:output>
                <p:delete match="d:file[not(@role='nav')]">
                    <p:input port="source">
                        <p:pipe step="content-docs" port="result.fileset"/>
                    </p:input>
                </p:delete>
                <px:fileset-load name="load">
                    <p:input port="in-memory">
                        <p:pipe step="content-docs" port="result"/>
                    </p:input>
                </px:fileset-load>
            </p:when>
            <p:otherwise>
                <p:output port="result" primary="true">
                    <p:pipe step="filter" port="result.in-memory"/>
                </p:output>
                <p:output port="fileset">
                    <p:pipe step="filter" port="result"/>
                </p:output>
                <p:split-sequence test="//html:nav[@epub:type='toc']" name="content-docs-with-toc">
                    <p:input port="source">
                        <p:pipe step="content-docs" port="result"/>
                    </p:input>
                </p:split-sequence>
                <p:sink/>
                <px:fileset-filter-in-memory name="filter">
                    <p:input port="source.fileset">
                        <p:pipe step="content-docs" port="result.fileset"/>
                    </p:input>
                    <p:input port="source.in-memory">
                        <p:pipe step="content-docs-with-toc" port="matched"/>
                    </p:input>
                </px:fileset-filter-in-memory>
            </p:otherwise>
        </p:choose>
        <px:assert message="There must be exactly one navigation document in the fileset"
                   test-count-min="1" test-count-max="1" error-code="PEPU14"/>
        <px:message severity="DEBUG" message="Navigation document extracted from fileset"/>
    </p:group>
    <p:sink/>

    <p:documentation>Get primary spine items</p:documentation>
    <p:group name="content-docs-except-nav" px:progress="1/10">
        <p:output port="result"/>
        <px:fileset-create/>
        <px:fileset-add-entry name="nav-doc.fileset">
            <p:input port="entry">
                <p:pipe step="nav-doc" port="result"/>
            </p:input>
        </px:fileset-add-entry>
        <p:sink/>
        <px:fileset-diff>
            <p:input port="source">
                <p:pipe step="content-docs" port="result.fileset"/>
            </p:input>
            <p:input port="secondary">
                <p:pipe step="nav-doc.fileset" port="result.fileset"/>
            </p:input>
        </px:fileset-diff>
    </p:group>
    <p:sink/>
    <p:group name="spine.primary">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:pipe step="main" port="spine"/>
            </p:input>
        </p:identity>
        <px:assert message="There can be at most one document on the 'spine' port" test-count-min="0" test-count-max="1" error-code="XXXX"/>
        <p:count/>
        <p:choose>
            <p:when test="/*=1">
                <px:fileset-intersect>
                    <p:input port="source">
                        <p:pipe step="content-docs" port="result.fileset"/>
                        <p:pipe step="main" port="spine"/>
                    </p:input>
                </px:fileset-intersect>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="content-docs-except-nav" port="result"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:add-attribute match="d:file" attribute-name="linear" attribute-value="yes"/>
    </p:group>
    <p:sink/>

    <p:documentation>Create manifest</p:documentation>
    <p:group name="manifest" px:progress="1/10"
             px:message="Creating package document manifest and fileset" px:message-severity="DEBUG">
        <p:output port="result" primary="true"/>
        <p:output port="as-fileset">
            <p:pipe step="manifest-with-ids" port="result"/>
        </p:output>

        <p:documentation>Give files in the "bindings" fileset media-type "application/xhtml+xml"</p:documentation>
        <p:group name="manifest-with-bindings">
            <p:output port="result" sequence="true"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="main" port="bindings"/>
                </p:input>
            </p:identity>
            <px:assert message="There can be at most one set of bindings" test-count-min="0" test-count-max="1" error-code="PEPUTODO"/>
            <p:count/>
            <p:choose>
                <p:when test="/*=0">
                    <p:identity>
                        <p:input port="source">
                            <p:pipe step="fileset-except-smil" port="result"/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <px:fileset-intersect>
                        <p:input port="source">
                            <p:pipe step="fileset-except-smil" port="result"/>
                            <p:pipe step="main" port="bindings"/>
                        </p:input>
                    </px:fileset-intersect>
                    <p:set-attributes match="d:file" name="bindings-with-media-type-xhtml">
                        <p:input port="attributes">
                            <p:inline>
                                <d:file media-type="application/xhtml+xml"/>
                            </p:inline>
                        </p:input>
                    </p:set-attributes>
                    <px:fileset-join>
                        <p:input port="source">
                            <p:pipe step="fileset-except-smil" port="result"/>
                            <p:pipe step="bindings-with-media-type-xhtml" port="result"/>
                        </p:input>
                    </px:fileset-join>
                </p:otherwise>
            </p:choose>
        </p:group>

        <p:documentation>Add id attributes</p:documentation>
        <p:label-elements name="manifest-with-ids"
                          match="d:file" attribute="id" label="concat('item_',1+count(preceding-sibling::*))"/>

        <p:documentation>Create manifest from fileset</p:documentation>
        <!-- FIXME: check which items are actually referenced from the publication -->
        <p:xslt name="fileset-to-manifest">
            <p:input port="stylesheet">
                <p:document href="create-package-doc.fileset-to-manifest.xsl"/>
            </p:input>
            <p:with-param name="output-base-uri" select="string(/*)">
                <p:pipe step="output-base-uri" port="normalized"/>
            </p:with-param>
            <p:with-option name="output-base-uri" select="string(/*)">
                <p:pipe step="output-base-uri" port="normalized"/>
            </p:with-option>
        </p:xslt>

        <px:message severity="DEBUG" message="Successfully created package document manifest"/>
    </p:group>
    <p:sink/>

    <p:documentation>Get spine</p:documentation>
    <p:group name="spine" px:progress="1/10">
        <p:output port="result"/>

        <p:documentation>Add secondary spine items and sort</p:documentation>
        <p:add-attribute match="d:file" attribute-name="linear" attribute-value="no" name="spine.secondary-if-not-primary">
            <p:input port="source">
                <p:pipe step="content-docs-except-nav" port="result"/>
            </p:input>
        </p:add-attribute>
        <p:sink/>
        <px:fileset-join>
            <!-- when file attributes are merged the last occurence wins -->
            <p:input port="source">
                <p:pipe step="fileset-except-smil" port="result"/>
                <p:pipe step="spine.secondary-if-not-primary" port="result"/> <!-- linear="no" -->
                <p:pipe step="spine.primary" port="result"/> <!-- linear="yes" -->
            </p:input>
        </px:fileset-join>
        <p:delete match="d:file[not(@linear)]"/>

        <p:documentation>Add idref attributes</p:documentation>
        <p:group>
            <p:viewport match="/*/d:file">
                <p:variable name="href" select="/*/resolve-uri(@href,base-uri(.))"/>
                <p:add-attribute match="/*" attribute-name="idref">
                    <p:with-option name="attribute-value" select="/*/d:file[resolve-uri(@href,base-uri(.))=$href]/@id">
                        <p:pipe step="manifest" port="as-fileset"/>
                    </p:with-option>
                </p:add-attribute>
            </p:viewport>
        </p:group>

        <p:documentation>Create spine from fileset</p:documentation>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="create-package-doc.idref-fileset-to-spine.xsl"/>
            </p:input>
        </p:xslt>

        <p:documentation>Add toc attribute</p:documentation>
        <p:choose>
            <p:xpath-context>
                <p:pipe step="manifest" port="as-fileset"/>
            </p:xpath-context>
            <p:when test="$compatibility-mode='true' and //@media-type='application/x-dtbncx+xml'">
                <p:add-attribute match="/*" attribute-name="toc">
                    <p:with-option name="attribute-value" select="//*[@media-type='application/x-dtbncx+xml']/@id">
                        <p:pipe step="manifest" port="as-fileset"/>
                    </p:with-option>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <px:message severity="DEBUG" message="Successfully created package document spine"/>
    </p:group>
    <p:sink/>

    <p:documentation>If the navigation document contains landmarks and compatibility-mode is
    enabled, generate the guide element based on the landmarks.</p:documentation>
    <p:group name="guide" px:progress="1/10">
        <p:output port="result" sequence="true"/>
        <p:filter select="//html:nav[@*[name()='epub:type']='landmarks']" name="guide.landmarks">
            <p:input port="source">
                <p:pipe step="nav-doc" port="result"/>
            </p:input>
        </p:filter>
        <p:count/>
        <p:choose px:progress="1">
            <p:when test="/*=0 or not($compatibility-mode='true')">
                <p:identity px:message="No landmarks in package document" px:message-severity="DEBUG">
                    <p:input port="source">
                        <p:pipe step="guide.landmarks" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <px:epub-landmarks-to-guide px:progress="1"
                                            px:message="Creating guide element for package document"
                                            px:message-severity="DEBUG">
                    <p:input port="source">
                        <p:pipe step="guide.landmarks" port="result"/>
                    </p:input>
                    <p:with-option name="output-base-uri" select="string(/*)">
                        <p:pipe step="output-base-uri" port="normalized"/>
                    </p:with-option>
                </px:epub-landmarks-to-guide>
                <px:message severity="DEBUG" message="guide element created successfully"/>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:sink/>

    <p:group name="bindings" px:progress="1/10">
        <p:output port="result" sequence="true"/>
        <p:count>
            <p:input port="source">
                <p:pipe step="main" port="bindings"/>
            </p:input>
        </p:count>
        <p:choose>
            <p:when test="/*=0 or not($compatibility-mode='true')">
                <px:message severity="DEBUG" message="No bindings in package document"/>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:xslt px:message="Creating bindings element for package document" px:message-severity="DEBUG">
                    <p:input port="source">
                        <p:pipe step="main" port="bindings"/>
                        <p:pipe step="manifest" port="as-fileset"/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="create-package-doc.handler-fileset-to-bindings.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <px:message severity="DEBUG" message="bindings element created successfully"/>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:sink/>

    <p:documentation>Create package document</p:documentation>
    <p:group name="create-package-doc" px:progress="1/10">
        <p:output port="result" primary="true">
            <p:pipe step="add-entry" port="result.in-memory"/>
        </p:output>
        <p:output port="fileset">
            <p:pipe step="add-entry" port="result.fileset"/>
        </p:output>
        <p:insert match="/*" position="last-child">
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <package xmlns="http://www.idpf.org/2007/opf" version="3.0"/>
                </p:inline>
            </p:input>
            <p:input port="insertion">
                <p:pipe step="default-metadata" port="result"/>
                <p:pipe step="manifest" port="result"/>
                <p:pipe step="spine" port="result"/>
                <p:pipe step="guide" port="result"/>
                <p:pipe step="bindings" port="result"/>
            </p:input>
        </p:insert>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="string(/*)">
                <p:pipe step="output-base-uri" port="normalized"/>
            </p:with-option>
        </px:set-base-uri>
        <px:add-xml-base root="false"/>
        <px:message severity="DEBUG" message="Finished assigning media overlays to content documents" name="in-memory"/>
        <p:sink/>
        <px:fileset-create/>
        <px:fileset-add-entry media-type="application/oebps-package+xml" name="add-entry">
            <p:input port="entry">
                <p:pipe step="in-memory" port="result"/>
            </p:input>
        </px:fileset-add-entry>
        <p:sink/>
        <p:group name="default-metadata">
            <p:output port="result"/>
            <p:documentation>Minimal required metadata</p:documentation>
            <p:uuid match="dc:identifier/text()">
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                        <metadata>
                            <dc:title>Unknown</dc:title>
                            <dc:identifier>generated-uuid</dc:identifier>
                        </metadata>
                    </p:inline>
                </p:input>
            </p:uuid>
            <p:documentation>Generate metadata</p:documentation>
            <p:insert match="//opf:metadata" position="last-child">
                <p:input port="insertion">
                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                        <meta property="schema:accessMode">textual</meta>
                    </p:inline>
                </p:input>
            </p:insert>
            <!--
                <meta property="schema:accessMode">auditory</meta> added in px:epub3-add-mediaoverlays

                FIXME: other access modes are not checked:
                - visual (requires determining whether images are decorative or not)
                - tactile
            -->
            <!--
                <meta property="schema:accessibilityFeature">synchronizedAudioText</meta> added in px:epub3-add-mediaoverlays
                <meta property="schema:accessibilityFeature">tableOfContents</meta> added in px:epub3-add-navigation-doc
                <meta property="schema:accessibilityFeature">pageNavigation</meta> added in px:epub3-add-navigation-doc

                FIXME: other accessibility features are not checked:
                - alternativeText
                - annotations
                - ARIA
                - audioDescription
                - bookmarks
                - braille
                - captions
                - ChemML
                - describedMath
                - displayTransformability
                - highContrastAudio
                - highContrastDisplay
                - index
                - largePrint
                - latex
                - longDescription
                - MathML
                - pageBreakMarkers
                - printPageNumbers
                - readingOrder
                - rubyAnnotations
                - signLanguage
                - structuralNavigation
                - taggedPDF
                - tactileGraphic
                - tactileObject
                - timingControl
                - transcript
                - ttsMarkup
                - unlocked
            -->
            <p:insert match="//opf:metadata" position="last-child">
                <p:input port="insertion">
                    <!--
                        FIXME: accessibility hazards are not checked:
                        - flashing
                        - noFlashingHazard
                        - motionSimulation
                        - noMotionSimulationHazard
                        - sound
                        - noSoundHazard
                    -->
                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                        <meta property="schema:accessibilityHazard">unknown</meta>
                    </p:inline>
                </p:input>
            </p:insert>
            <p:choose>
                <p:when test="$reserved-prefixes='#default'">
                    <p:add-attribute match="/*"
                                     attribute-name="prefix"
                                     attribute-value="schema: http://schema.org/">
                        <!--
                            note that if there was already "schema" metadata present in the input,
                            and the "schema" prefix was not declared, px:epub3-add-metadata will
                            make sure that the prefix will not be declared in the output either
                        -->
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:sink/>
    </p:group>

    <p:documentation>Add metadata</p:documentation>
    <p:group px:progress="1/10">
        <p:sink/>
        <px:epub3-add-metadata log-conflicts="false" name="add-metadata" px:progress="1">
            <p:input port="source.fileset">
                <p:pipe step="create-package-doc" port="fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="create-package-doc" port="result"/>
            </p:input>
            <p:input port="metadata">
                <p:pipe step="main" port="metadata"/>
            </p:input>
            <p:with-option name="reserved-prefixes" select="$reserved-prefixes"/>
            <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
        </px:epub3-add-metadata>
        <px:fileset-load media-types="application/oebps-package+xml">
            <p:input port="in-memory">
                <p:pipe step="add-metadata" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
    </p:group>

    <p:documentation>Set navigation document</p:documentation>
    <p:group px:progress="1/10">
        <p:identity name="package-doc"/>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="create-package-doc" port="fileset"/>
                <p:pipe step="nav-doc" port="fileset"/>
            </p:input>
        </px:fileset-join>
        <px:epub3-add-navigation-doc name="set-nav-doc" px:progress="1">
            <p:input port="source.in-memory">
                <p:pipe step="package-doc" port="result"/>
                <p:pipe step="nav-doc" port="result"/>
            </p:input>
        </px:epub3-add-navigation-doc>
        <px:fileset-load media-types="application/oebps-package+xml">
            <p:input port="in-memory">
                <p:pipe step="set-nav-doc" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
    </p:group>

    <p:documentation>Add properties of content documents</p:documentation>
    <p:choose px:progress="1/10">
        <p:when test="$detect-properties='true'">
            <pxi:epub3-detect-properties px:progress="1">
                <p:input port="content-docs">
                    <p:pipe step="content-docs" port="result"/>
                </p:input>
            </pxi:epub3-detect-properties>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:documentation>Add mediaoverlays</p:documentation>
    <p:group name="result" px:progress="1/10">
        <p:output port="result" primary="true"/>
        <p:output port="fileset">
            <p:pipe step="load" port="result.fileset"/>
        </p:output>
        <p:identity name="package-doc"/>
        <p:sink/>
        <px:fileset-join px:progress="1/5">
            <p:input port="source">
                <p:pipe step="create-package-doc" port="fileset"/>
                <p:pipe step="fileset-except-smil" port="result"/>
            </p:input>
        </px:fileset-join>
        <px:epub3-add-mediaoverlays name="add-mediaoverlays" px:progress="3/5">
            <p:input port="source.in-memory">
                <p:pipe step="main" port="source.in-memory"/>
                <p:pipe step="package-doc" port="result"/>
            </p:input>
            <p:input port="mo.fileset">
                <p:pipe step="fileset-except-smil" port="not-matched"/>
            </p:input>
            <p:input port="mo.in-memory">
                <p:pipe step="fileset-except-smil" port="not-matched.in-memory"/>
            </p:input>
            <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
            <p:with-option name="reserved-prefixes" select="$reserved-prefixes"/>
        </px:epub3-add-mediaoverlays>
        <px:fileset-load media-types="application/oebps-package+xml" name="load" px:progress="1/5">
            <p:input port="in-memory">
                <p:pipe step="add-mediaoverlays" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
    </p:group>

</p:declare-step>
