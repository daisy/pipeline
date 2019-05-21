<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-html-to-epub3.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/">


    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>

    <p:input port="report.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="status.in">
        <p:inline>
            <d:validation-status result="ok"/>
        </p:inline>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="fileset.out" step="choose"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.out" step="choose"/>
    </p:output>

    <p:output port="report.out" sequence="true">
        <p:pipe port="report.in" step="main"/>
        <p:pipe port="report.out" step="choose"/>
    </p:output>
    <p:output port="status.out">
        <p:pipe port="result" step="status"/>
    </p:output>

    <p:option name="fail-on-error" required="true"/>
    <p:option name="temp-dir" required="true"/>
    <p:option name="compatibility-mode" select="'true'"/>

    <p:import href="html-split.xpl"/>
    <p:import href="validation-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-nav-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>

    <px:assert message="'fail-on-error' should be either 'true' or 'false'. was: '$1'. will default to 'true'.">
        <p:with-option name="param1" select="$fail-on-error"/>
        <p:with-option name="test" select="$fail-on-error = ('true','false')"/>
    </px:assert>

    <p:choose name="choose">
        <p:xpath-context>
            <p:pipe port="status.in" step="main"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' or $fail-on-error = 'false'">
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="html-to-epub3.step.result.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="html-to-epub3.step.result.in-memory"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>

            <p:variable name="epub-dir" select="concat($temp-dir,'epub/')"/>
            <p:variable name="publication-dir" select="concat($epub-dir,'EPUB/')"/>

            <px:fileset-load media-types="application/xhtml+xml" name="html-to-epub3.step.load-single-html">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one HTML file in the single-page HTML fileset." error-code="NORDICDTBOOKEPUB007"/>
            <p:identity name="html-to-epub3.step.single-html"/>
            <p:sink/>

            <px:nordic-html-split-perform name="html-to-epub3.step.html-split">
                <p:input port="fileset.in">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:nordic-html-split-perform>
            <px:fileset-move name="html-to-epub3.step.html-split.moved">
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.out" step="html-to-epub3.step.html-split"/>
                </p:input>
                <p:with-option name="new-base" select="$publication-dir"/>
            </px:fileset-move>

            <!-- Create spine -->
            <px:fileset-filter media-types="application/xhtml+xml" name="html-to-epub3.step.filter-html-split-fileset-xhtml"/>
            <p:identity name="html-to-epub3.step.spine"/>

            <px:fileset-load name="html-to-epub3.step.load-spine">
                <p:input port="in-memory">
                    <p:pipe port="in-memory.out" step="html-to-epub3.step.html-split.moved"/>
                </p:input>
            </px:fileset-load>
            <p:for-each name="html-to-epub3.step.iterate-spine">
                <p:viewport match="/html:html/html:head" name="html-to-epub3.step.iterate-spine.viewport-html-head">
                    <p:xslt name="html-to-epub3.step.iterate-spine.viewport-html-head.pretty-print">
                        <!-- TODO: remove as many pretty printing steps as possible to improve performance -->
                        <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/pretty-print.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:viewport>
            </p:for-each>
            <p:identity name="html-to-epub3.step.spine-html"/>
            <p:sink/>

            <!-- Create OPF metadata -->
            <p:xslt name="html-to-epub3.step.opf-metadata">
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/html-to-opf-metadata.xsl"/>
                </p:input>
            </p:xslt>
            <p:sink/>

            <!-- Create Navigation Document -->
            <p:group name="html-to-epub3.step.nav">
                <p:output port="html">
                    <p:pipe port="result" step="html-to-epub3.step.nav.html"/>
                </p:output>
                <p:output port="ncx">
                    <p:pipe port="result" step="html-to-epub3.step.nav.ncx"/>
                </p:output>

                <p:group name="html-to-epub3.step.nav.toc">
                    <p:output port="result"/>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                        </p:input>
                    </p:identity>
                    <p:xslt name="html-to-epub3.step.nav.toc.generate-missing-headlines">
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/generate-missing-headlines.xsl"/>
                        </p:input>
                    </p:xslt>
                    <p:delete match="html:a[tokenize(@epub:type,'\s+')='noteref']" name="html-to-epub3.step.nav.toc.delete-noterefs"/>
                    <px:epub3-nav-create-toc name="html-to-epub3.step.nav.toc.nav-create-toc">
                        <p:with-option name="output-base-uri" select="base-uri(/*)">
                            <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                        </p:with-option>
                    </px:epub3-nav-create-toc>
                    <p:add-attribute match="/*" attribute-name="xml:base" name="html-to-epub3.step.nav.toc.add-xml-base">
                        <p:with-option name="attribute-value" select="base-uri(/*)">
                            <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                        </p:with-option>
                    </p:add-attribute>
                    <p:delete match="/*/@xml:base" name="html-to-epub3.step.nav.toc.delete-xml-base"/>
                    <p:delete match="/html:nav/html:ol/html:li/html:a" name="html-to-epub3.step.nav.toc.delete-nav-ol-li-a"/>
                    <p:unwrap match="/html:nav/html:ol/html:li" name="html-to-epub3.step.nav.toc.unwrap-nav-ol-li"/>
                    <p:unwrap match="/html:nav/html:ol" name="html-to-epub3.step.nav.toc.unwrap-nav-ol"/>
                    <p:identity name="html-to-epub3.step.nav.toc.single-html-hrefs"/>
                    <p:xslt name="html-to-epub3.step.nav.toc.replace-single-html-hrefs-with-multi-html-hrefs">
                        <p:input port="source">
                            <p:pipe port="result" step="html-to-epub3.step.nav.toc.single-html-hrefs"/>
                            <p:pipe port="in-memory.out" step="html-to-epub3.step.html-split"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/replace-single-html-hrefs-with-multi-html-hrefs.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:group>
                <p:sink/>

                <px:epub3-nav-create-page-list name="html-to-epub3.step.nav.page-list">
                    <p:with-option name="output-base-uri" select="concat($publication-dir,'nav.xhtml')"/>
                    <p:input port="source">
                        <p:pipe port="in-memory.out" step="html-to-epub3.step.html-split.moved"/>
                    </p:input>
                </px:epub3-nav-create-page-list>
                <p:sink/>

                <px:epub3-nav-aggregate name="html-to-epub3.step.nav-aggregate">
                    <p:input port="source">
                        <p:pipe step="html-to-epub3.step.nav.toc" port="result"/>
                        <p:pipe step="html-to-epub3.step.nav.page-list" port="result"/>
                    </p:input>
                    <p:with-option name="language" select="/*/(@xml:lang,@lang)[1]">
                        <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                    </p:with-option>
                    <p:with-option name="output-base-uri" select="concat($publication-dir,'nav.xhtml')"/>
                </px:epub3-nav-aggregate>
                <p:xslt name="html-to-epub3.step.navdoc-nordic-normalization">
                    <p:with-param name="identifier" select="/*/html:head/html:meta[@name='dc:identifier']/@content">
                        <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                    </p:with-param>
                    <p:with-param name="title" select="/*/html:head/html:title/text()">
                        <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                    </p:with-param>
                    <p:with-param name="supplier" select="/*/html:head/html:meta[@name='nordic:supplier']/@content">
                        <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                    </p:with-param>
                    <p:with-param name="publisher" select="/*/html:head/html:meta[@name='dc:publisher']/@content">
                        <p:pipe port="result" step="html-to-epub3.step.single-html"/>
                    </p:with-param>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/navdoc-nordic-normalization.xsl"/>
                    </p:input>
                </p:xslt>
                <p:viewport match="/html:html/html:head" name="html-to-epub3.step.viewport-html-head">
                    <p:xslt name="html-to-epub3.step.viewport-html-head.pretty-print">
                        <!-- TODO: consider which pretty-print.xsl invocations can be removed to improve performance -->
                        <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/pretty-print.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:viewport>
                <p:identity name="html-to-epub3.step.nav.html"/>

                <p:xslt name="html-to-epub3.step.nav-to-ncx">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="http://www.daisy.org/pipeline/modules/epub3-nav-utils/nav-to-ncx.xsl"/>
                    </p:input>
                </p:xslt>
                <p:xslt name="html-to-epub3.step.ncx-pretty-print">
                    <!-- TODO: remove pretty printing to improve performance -->
                    <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/pretty-print.xsl"/>
                    </p:input>
                </p:xslt>
                <p:add-attribute match="/*" attribute-name="xml:base" name="html-to-epub3.step.ncx-add-xml-base">
                    <p:with-option name="attribute-value" select="concat($publication-dir,'nav.ncx')"/>
                </p:add-attribute>
                <p:identity name="html-to-epub3.step.nav.ncx"/>
            </p:group>

            <px:fileset-create name="html-to-epub3.step.create-ncx-fileset">
                <p:with-option name="base" select="replace(base-uri(/*),'[^/]+$','')">
                    <p:pipe port="ncx" step="html-to-epub3.step.nav"/>
                </p:with-option>
            </px:fileset-create>
            <px:message message="ncx base: $1">
                <p:with-option name="param1" select="replace(base-uri(/*),'[^/]+$','')">
                    <p:pipe port="ncx" step="html-to-epub3.step.nav"/>
                </p:with-option>
            </px:message>
            <px:fileset-add-entry media-type="application/x-dtbncx+xml" name="html-to-epub3.step.add-ncx-to-fileset">
                <p:with-option name="href" select="'nav.ncx'"/>
            </px:fileset-add-entry>
            <p:identity name="html-to-epub3.step.ncx-fileset"/>
            <px:fileset-join name="html-to-epub3.step.join-ncx-and-non-linear-content-filesets">
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.ncx-fileset"/>
                    <p:pipe port="result" step="html-to-epub3.step.non-linear-content"/>
                </p:input>
            </px:fileset-join>
            <px:mediatype-detect name="html-to-epub3.step.resource-fileset"/>
            <p:sink/>

            <px:epub3-pub-create-package-doc name="html-to-epub3.step.create-package-doc">
                <p:with-option name="result-uri" select="concat($publication-dir,'package.opf')"/>
                <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
                <p:with-option name="detect-properties" select="'true'"/>
                <p:input port="spine-filesets">
                    <p:pipe port="result" step="html-to-epub3.step.spine"/>
                </p:input>
                <p:input port="metadata">
                    <p:pipe port="result" step="html-to-epub3.step.opf-metadata"/>
                </p:input>
                <p:input port="content-docs">
                    <p:pipe port="html" step="html-to-epub3.step.nav"/>
                    <p:pipe port="result" step="html-to-epub3.step.spine-html"/>
                </p:input>
                <p:input port="publication-resources">
                    <p:pipe port="result" step="html-to-epub3.step.resource-fileset"/>
                </p:input>
            </px:epub3-pub-create-package-doc>
            <p:add-attribute match="/*" attribute-name="unique-identifier" attribute-value="pub-identifier" name="html-to-epub3.step.add-opf-attribute.unique-identifier"/>
            <p:add-attribute match="//dc:identifier[not(preceding::dc:identifier)]" attribute-name="id" attribute-value="pub-identifier" name="html-to-epub3.step.add-opf-attribute.dc-identifier-id"/>
            <p:add-attribute match="/*" attribute-name="prefix" attribute-value="nordic: http://www.mtm.se/epub/" name="html-to-epub3.step.add-opf-attribute.nordic-prefix"/>
            <p:add-attribute match="/*/opf:spine/opf:itemref[/*/opf:manifest/opf:item[matches(@href,'-(cover|rearnotes)(-\d+)?.xhtml')]/@id = @idref]" attribute-name="linear" attribute-value="no"
                name="html-to-epub3.step.add-opf-attribute.non-linear-spine-items"/>
            <p:add-attribute match="opf:item[matches(@href,'(^|/)cover.jpg')]" attribute-name="properties" attribute-value="cover-image"/>
            <p:delete match="/*//*/@prefix" name="html-to-epub3.step.delete-non-root-opf-prefix-attributes"/>
            <p:xslt name="html-to-epub3.step.add-dc-namespace">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                            <xsl:template match="/*">
                                <xsl:copy>
                                    <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
                                    <xsl:copy-of select="@*|node()"/>
                                </xsl:copy>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
            </p:xslt>
            <p:add-attribute match="/*" attribute-name="xml:base" name="html-to-epub3.step.set-opf-xml-base">
                <p:with-option name="attribute-value" select="concat($publication-dir,'package.opf')"/>
            </p:add-attribute>
            <p:xslt name="html-to-epub3.step.pretty-print-opf">
                <!-- TODO: consider removing this XSLT invocation to improve performance -->
                <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/pretty-print.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="html-to-epub3.step.package"/>

            <p:identity name="html-to-epub3.step.result.in-memory-without-ocf-files">
                <p:input port="source">
                    <p:pipe port="html" step="html-to-epub3.step.nav"/>
                    <p:pipe port="ncx" step="html-to-epub3.step.nav"/>
                    <p:pipe port="result" step="html-to-epub3.step.spine-html"/>
                    <p:pipe port="result" step="html-to-epub3.step.package"/>
                </p:input>
            </p:identity>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.resource-fileset"/>
                </p:input>
            </p:identity>
            <px:fileset-add-entry media-type="application/oebps-package+xml" name="html-to-epub3.step.add-opf-to-resource-fileset">
                <p:with-option name="href" select="base-uri(/*)">
                    <p:pipe port="result" step="html-to-epub3.step.package"/>
                </p:with-option>
            </px:fileset-add-entry>
            <px:fileset-add-entry media-type="application/xhtml+xml" name="html-to-epub3.step.add-nav-to-resource-fileset">
                <p:with-option name="href" select="concat($publication-dir,'nav.xhtml')"/>
            </px:fileset-add-entry>
            <px:mediatype-detect name="html-to-epub3.step.mediatype-detect">
                <p:input port="in-memory">
                    <p:pipe port="result" step="html-to-epub3.step.result.in-memory-without-ocf-files"/>
                </p:input>
            </px:mediatype-detect>
            <px:message message="epub-dir: $1">
                <p:with-option name="param1" select="$epub-dir"/>
            </px:message>
            <px:fileset-rebase name="html-to-epub3.step.rebase-fileset-to-epub-dir">
                <p:with-option name="new-base" select="$epub-dir"/>
            </px:fileset-rebase>
            <p:identity name="html-to-epub3.step.result.fileset-without-ocf-files"/>
            <p:sink/>

            <px:epub3-ocf-finalize name="html-to-epub3.step.finalize">
                <p:with-option name="epub-dir" select="$epub-dir"/>
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.result.fileset-without-ocf-files"/>
                </p:input>
            </px:epub3-ocf-finalize>
            <px:fileset-join name="html-to-epub3.step.join-result-fileset">
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.finalize"/>
                    <p:pipe port="result" step="html-to-epub3.step.spine"/>
                    <p:pipe port="result" step="html-to-epub3.step.result.fileset-without-ocf-files"/>
                </p:input>
            </px:fileset-join>
            <p:add-attribute match="//d:file[@href='META-INF/container.xml']" attribute-name="media-type" attribute-value="application/xml"
                name="html-to-epub3.step.result-fileset.add-container-media-type"/>
            <p:add-attribute match="//d:file[matches(@media-type,'[/+]xml$')]" attribute-name="omit-xml-declaration" attribute-value="false"
                name="html-to-epub3.step.result-fileset.dont-omit-xml-declarations"/>
            <p:add-attribute match="//d:file[matches(@media-type,'[/+]xml$')]" attribute-name="indent" attribute-value="true" name="html-to-epub3.step.result-fileset.indent"/>
            <p:add-attribute match="//d:file[matches(@media-type,'[/+]xml$') and not(@media-type='application/xhtml+xml')]" attribute-name="method" attribute-value="xml"
                name="html-to-epub3.step.result-fileset.method-xml"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="method" attribute-value="xhtml" name="html-to-epub3.step.result-fileset.method-xhtml"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="doctype" attribute-value="&lt;!DOCTYPE html&gt;"
                name="html-to-epub3.step.result-fileset.doctype-html"/>
            <p:identity name="html-to-epub3.step.result.fileset"/>
            <p:sink/>

            <p:identity name="html-to-epub3.step.result.in-memory">
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.step.result.in-memory-without-ocf-files"/>
                    <p:pipe port="in-memory.out" step="html-to-epub3.step.finalize"/>
                </p:input>
            </p:identity>
            <p:sink/>

            <!-- List auxiliary resources (i.e. all non-content files: images, CSS, NCX, etc. as well as content files that are non-primary) -->
            <px:fileset-filter name="html-to-epub3.step.filter-non-linear-content">
                <p:input port="source">
                    <p:pipe port="fileset.out" step="html-to-epub3.step.html-split.moved"/>
                </p:input>
            </px:fileset-filter>
            <p:delete match="//d:file[@media-type='application/xhtml+xml' and not(matches(@href,'-(cover|rearnotes)(-\d+)?.xhtml'))]"
                name="html-to-epub3.step.non-linear-content-fileset.delete-linear-content"/>
            <p:identity name="html-to-epub3.step.non-linear-content"/>










        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="fileset.in" step="main"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>

            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:choose name="status">
        <p:xpath-context>
            <p:pipe port="status.in" step="main"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' and $fail-on-error='true'">
            <p:output port="result"/>
            <px:nordic-validation-status>
                <p:input port="source">
                    <p:pipe port="report.out" step="choose"/>
                </p:input>
            </px:nordic-validation-status>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="status.in" step="main"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
