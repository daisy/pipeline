<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-epub3-to-html.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf">
    
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

    <p:import href="validation-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
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
                <p:pipe port="result" step="epub3-to-html.step.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="epub3-to-html.step.in-memory"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>

            <px:fileset-load media-types="application/oebps-package+xml" name="epub3-to-html.step.load-epub-opf">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one Package Document in the EPUB." error-code="NORDICDTBOOKEPUB011"/>
            <p:identity name="epub3-to-html.step.package-doc"/>
            <p:sink/>

            <p:xslt name="epub3-to-html.step.opf-to-spine-fileset">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="source">
                    <p:pipe port="result" step="epub3-to-html.step.package-doc"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/opf-to-spine-fileset.xsl"/>
                </p:input>
            </p:xslt>
            <px:message message="Loading spine..."/>
            <px:fileset-load name="epub3-to-html.step.load-spine-xhtml">
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <p:for-each name="epub3-to-html.step.iterate-spine-xhtml">
                <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-to-html.step.iterate-spine-xhtml.add-xml-base">
                    <p:with-option name="attribute-value" select="base-uri(/*)"/>
                </p:add-attribute>
            </p:for-each>
            <p:identity name="epub3-to-html.step.spine-xhtml"/>
            <p:sink/>

            <p:filter select="/*/opf:manifest/opf:item[matches(@properties,'(^|\s)nav(\s|$)')]" name="epub3-to-html.step.filter-nav-item">
                <p:input port="source">
                    <p:pipe port="result" step="epub3-to-html.step.package-doc"/>
                </p:input>
            </p:filter>
            <p:group name="epub3-to-html.step.nav-with-spine-bodies">
                <p:output port="result">
                    <p:pipe port="result" step="epub3-to-html.step.nav-with-spine-bodies.nav-with-spine-bodies"/>
                </p:output>
                <p:variable name="nav-href" select="resolve-uri(/*/@href,base-uri(/*))"/>
                <px:message message="Loading Navigation Document: $1">
                    <p:with-option name="param1" select="$nav-href"/>
                </px:message>
                <px:fileset-load name="epub3-to-html.step.nav-with-spine-bodies.load-nav">
                    <p:input port="fileset">
                        <p:pipe port="fileset.in" step="main"/>
                    </p:input>
                    <p:input port="in-memory">
                        <p:pipe port="in-memory.in" step="main"/>
                    </p:input>
                    <p:with-option name="href" select="$nav-href"/>
                </px:fileset-load>
                <px:assert test-count-min="1" test-count-max="1" message="The Navigation Document must exist: $1" error-code="NORDICDTBOOKEPUB013">
                    <p:with-option name="param1" select="$nav-href"/>
                </px:assert>
                <p:insert match="/*" position="first-child" name="epub3-to-html.step.nav-with-spine-bodies.insert-spine-bodies-into-nav">
                    <p:input port="insertion">
                        <p:pipe port="result" step="epub3-to-html.step.nav-with-spine-bodies.spine-bodies"/>
                    </p:input>
                </p:insert>
                <p:identity name="epub3-to-html.step.nav-with-spine-bodies.nav-with-spine-bodies"/>
                <p:sink/>

                <p:for-each name="epub3-to-html.step.nav-with-spine-bodies.iterate-spine-bodies">
                    <p:iteration-source select="/*/html:body">
                        <p:pipe port="result" step="epub3-to-html.step.spine-xhtml"/>
                    </p:iteration-source>
                    <p:delete match="/*/node()" name="epub3-to-html.step.nav-with-spine-bodies.iterate-spine-bodies.delete-body-content"/>
                    <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-to-html.step.nav-with-spine-bodies.iterate-spine-bodies.add-xml-base">
                        <p:with-option name="attribute-value" select="base-uri(/*)"/>
                    </p:add-attribute>
                </p:for-each>
                <p:wrap-sequence wrapper="opf:spine" name="epub3-to-html.step.nav-with-spine-bodies.wrap-spine"/>
                <p:identity name="epub3-to-html.step.nav-with-spine-bodies.spine-bodies"/>
            </p:group>
            <px:message message="Creating outline of single-document HTML representation based on navigation document and $1 documents from spine">
                <p:with-option name="param1" select="count(/*/opf:spine/*)"/>
            </px:message>
            <p:xslt name="epub3-to-html.step.navdoc-to-outline">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/navdoc-to-outline.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt name="epub3-to-html.step.replace-sections-with-documents">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="source">
                    <p:pipe port="result" step="epub3-to-html.step.navdoc-to-outline"/>
                    <p:pipe port="result" step="epub3-to-html.step.spine-xhtml"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/replace-sections-with-documents.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt name="epub3-to-html.step.fix-section-hierarchy">
                <p:with-param name="body-is-section" select="'false'"/>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/fix-section-hierarchy.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt name="epub3-to-html.step.make-uris-relative-to-document">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/make-uris-relative-to-document.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="epub3-to-html.step.single-html.body"/>
            <p:sink/>

            <p:xslt name="epub3-to-html.step.opf-to-html-metadata">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="source">
                    <p:pipe step="epub3-to-html.step.package-doc" port="result"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/opf-to-html-metadata.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="epub3-to-html.step.single-html.metadata"/>
            <p:sink/>

            <p:group name="epub3-to-html.step.header-element">
                <p:string-replace match="//html:h1/text()" name="epub3-to-html.step.header-element.create">
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <header xmlns="http://www.w3.org/1999/xhtml">
                                <h1 epub:type="fulltitle" class="title">FULLTITLE</h1>
                            </header>
                        </p:inline>
                    </p:input>
                    <p:with-option name="replace" select="concat('&quot;',replace(//html:title/text(),'&quot;','&amp;quot;'),'&quot;')">
                        <p:pipe port="result" step="epub3-to-html.step.single-html.metadata"/>
                    </p:with-option>
                </p:string-replace>
                <p:insert match="/*" position="last-child" name="epub3-to-html.step.header-element.insert-author-meta">
                    <p:input port="insertion" select="//html:meta[@name='dc:creator']">
                        <p:pipe port="result" step="epub3-to-html.step.single-html.metadata"/>
                    </p:input>
                </p:insert>
                <p:insert match="/*" position="last-child" name="epub3-to-html.step.header-element.insert-covertitle-meta">
                    <p:input port="insertion" select="//html:meta[@name='covertitle']">
                        <p:pipe port="result" step="epub3-to-html.step.single-html.metadata"/>
                    </p:input>
                </p:insert>
                <p:rename match="/*/html:meta" new-name="p" new-namespace="http://www.w3.org/1999/xhtml" name="epub3-to-html.step.header-element.rename-meta-to-p"/>
                <p:insert match="//html:p" position="first-child" name="epub3-to-html.step.header-element.insert-span-into-meta">
                    <p:input port="insertion">
                        <p:inline exclude-inline-prefixes="#all">
                            <html:span>TEXT</html:span>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:unwrap match="//html:p/html:span" name="epub3-to-html.step.header-element.unwrap-span-in-meta"/>
                <p:viewport match="//html:p" name="epub3-to-html.step.header-element.viewport-p">
                    <p:string-replace match="/*/text()" replace="/*/@content" name="epub3-to-html.step.header-element.set-text-to-content-attribute-value"/>
                </p:viewport>
                <p:add-attribute match="//html:p[@name='dc:creator']" attribute-name="epub:type" attribute-value="z3998:author" name="epub3-to-html.step.header-element.set-author-type"/>
                <p:add-attribute match="//html:p[@name='dc:creator']" attribute-name="class" attribute-value="docauthor" name="epub3-to-html.step.header-element.set-author-class"/>
                <p:add-attribute match="//html:p[@name='covertitle']" attribute-name="epub:type" attribute-value="z3998:covertitle" name="epub3-to-html.step.header-element.set-covertitle-type"/>
                <p:delete match="//html:p/@*[not(name()=('epub:type','class'))]" name="epub3-to-html.step.header-element.delete-non-type-non-class-attributes"/>
            </p:group>
            <p:identity name="epub3-to-html.step.single-html.header-element"/>
            <p:sink/>

            <p:replace match="//html:head" name="epub3-to-html.step.replace-single-html-head-with-head-from-opf">
                <p:input port="source">
                    <p:pipe port="result" step="epub3-to-html.step.single-html.body"/>
                </p:input>
                <p:input port="replacement" select="//html:head">
                    <p:pipe port="result" step="epub3-to-html.step.single-html.metadata"/>
                </p:input>
            </p:replace>
            <p:add-attribute match="/html:html" attribute-name="epub:prefix">
                <p:with-option name="attribute-value" select="string-join(//@epub:prefix,' ')"/>
            </p:add-attribute>
            <p:insert match="/*/html:body" position="first-child" name="epub3-to-html.step.insert-header-element-into-single-html">
                <p:input port="insertion">
                    <p:pipe port="result" step="epub3-to-html.step.single-html.header-element"/>
                </p:input>
            </p:insert>
            <p:add-attribute match="/*" attribute-name="xml:lang" name="epub3-to-html.step.add-xml-lang-to-single-html">
                <p:with-option name="attribute-value" select="/*/html:head/html:meta[@name='dc:language']/@content"/>
            </p:add-attribute>
            <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-to-html.step.add-xml-base-to-single-html">
                <p:with-option name="attribute-value"
                    select="replace(base-uri(/*),'[^/]+$',concat((/*/html:head/html:meta[lower-case(@name)=('dc:identifier','dct:identifier','dtb:uid','dc:title')]/string(@content), /*/html:head/html:title/normalize-space(.))[1],'.xhtml'))"
                />
            </p:add-attribute>
            <p:viewport match="//*[@xml:lang]" name="epub3-to-html.step.viewport-add-lang-to-xml-lang">
                <p:add-attribute match="/*" attribute-name="lang" name="epub3-to-html.step.viewport-add-lang-to-xml-lang.add-attribute">
                    <p:with-option name="attribute-value" select="/*/@xml:lang"/>
                </p:add-attribute>
            </p:viewport>
            <p:delete match="//*[@xml:lang = ancestor::*[@xml:lang][1]/@xml:lang]/@xml:lang | //*[@lang = ancestor::*[@lang][1]/@lang]/@lang"
                name="epub3-to-html.step.delete-unneccessary-xml-lang-and-lang"/>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/update-epub-prefixes.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="epub3-to-html.step.in-memory"/>

            <px:html-to-fileset name="epub3-to-html.step.single-html-to-fileset">
                <p:input port="source">
                    <p:pipe port="result" step="epub3-to-html.step.in-memory"/>
                </p:input>
            </px:html-to-fileset>
            <p:delete match="//d:file[preceding-sibling::d:file/resolve-uri(@href,base-uri(.))=resolve-uri(@href,base-uri(.))]" name="epub3-to-html.step.remove-duplicate-fileset-entries"/>
            <px:fileset-add-entry media-type="application/xhtml+xml" name="epub3-to-html.step.add-single-html-to-fileset">
                <p:with-option name="href" select="base-uri(/*)">
                    <p:pipe port="result" step="epub3-to-html.step.in-memory"/>
                </p:with-option>
            </px:fileset-add-entry>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="omit-xml-declaration" attribute-value="false"
                name="epub3-to-html.step.single-html-omit-xml-declaration"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="version" attribute-value="1.0" name="epub3-to-html.step.single-html-xml-version"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="encoding" attribute-value="utf-8" name="epub3-to-html.step.single-html-xml-encoding"/>
            <px:mediatype-detect name="epub3-to-html.step.single-html-fileset-mediatype-detect"/>
            <p:identity name="epub3-to-html.step.fileset"/>
            <p:sink/>


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
