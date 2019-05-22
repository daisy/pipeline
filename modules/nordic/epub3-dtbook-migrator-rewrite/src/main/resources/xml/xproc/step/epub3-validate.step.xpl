<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-epub3-validate.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" xmlns:jhove="http://hul.harvard.edu/ois/xml/ns/jhove">

    <p:serialization port="report.out" indent="true"/>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="report.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="status.in">
        <p:inline exclude-inline-prefixes="#all">
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
    <p:option name="check-images" select="'true'"/>
    <p:option name="organization-specific-validation" required="false" select="''"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="html-validate.step.xpl"/>
    <p:import href="read-xml-declaration.xpl"/>
    <p:import href="read-doctype-declaration.xpl"/>
    <p:import href="check-image-file-signatures.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epubcheck-adapter/library.xpl"/>

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
                <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:pipe port="report.in" step="main"/>
                <p:pipe port="result" step="epub3-validate.step.epubcheck.validate"/>
                <p:pipe port="result" step="epub3-validate.step.epub-filename.validate"/>
                <p:pipe port="result" step="epub3-validate.step.xml-declaration.validate"/>
                <p:pipe port="result" step="epub3-validate.step.opf.validate"/>
                <p:pipe port="result" step="epub3-validate.step.html.validate"/>
                <p:pipe port="result" step="epub3-validate.step.nav-references.validate"/>
                <p:pipe port="result" step="epub3-validate.step.nav-ncx.validate"/>
                <p:pipe port="result" step="epub3-validate.step.opf-and-html.validate"/>
                <p:pipe port="result" step="epub3-validate.step.images.validate"/>
                <p:pipe port="result" step="epub3-validate.step.category.html-report"/>
            </p:output>











            <p:variable name="basedir" select="if (/*/d:file[@media-type='application/epub+zip']) then $temp-dir else base-uri(/*)"/>

            <p:choose name="epub3-validate.step.choose-zipped-or-not">
                <p:when test="/*/d:file[@media-type='application/epub+zip']">
                    <px:epubcheck mode="epub" version="3" name="epub3-validate.step.choose-zipped-or-not.epubcheck-zipped">
                        <p:with-option name="epub" select="(/*/d:file[@media-type='application/epub+zip'])[1]/resolve-uri(@href,base-uri(.))"/>
                    </px:epubcheck>
                </p:when>
                <p:otherwise>
                    <px:epubcheck mode="expanded" version="3" name="epub3-validate.step.choose-zipped-or-not.epubcheck-not-zipped">
                        <p:with-option name="epub" select="(/*/d:file[@media-type='application/oebps-package+xml'])[1]/resolve-uri(@href,base-uri(.))"/>
                    </px:epubcheck>
                </p:otherwise>
            </p:choose>
            <p:delete match="jhove:message[starts-with(.,'HTM-047')]" name="epub3-validate.step.delete-htm047">
                <!--
                    https://github.com/nlbdev/nordic-epub3-dtbook-migrator/issues/111
                    https://github.com/IDPF/epubcheck/issues/419
                -->
            </p:delete>
            <p:xslt name="epub3-validate.step.epubcheck-report-to-pipeline-report">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/epubcheck-report-to-pipeline-report.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="epub3-validate.step.epubcheck.validate"/>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
            </p:identity>
            <p:choose name="epub3-validate.step.unzip">
                <p:when test="/*/d:file[@media-type='application/epub+zip']">
                    <p:output port="fileset" primary="true">
                        <p:pipe port="result" step="epub3-validate.step.unzip.fileset"/>
                    </p:output>
                    <p:output port="in-memory" sequence="true">
                        <p:empty/>
                    </p:output>
                    <px:fileset-filter media-types="application/epub+zip" name="epub3-validate.step.unzip.filter-epub-from-fileset"/>
                    <px:assert message="There must be exactly one EPUB in the fileset (was: $1)." error-code="NORDICDTBOOKEPUB021">
                        <p:with-option name="test" select="count(/*/d:file) = 1"/>
                        <p:with-option name="param1" select="count(/*/d:file)"/>
                    </px:assert>
                    <px:fileset-unzip name="epub3-validate.step.unzip.unzip" load-to-memory="false" store-to-disk="true">
                        <p:with-option name="href" select="resolve-uri(/*/*/(@original-href,@href)[1],/*/*/base-uri(.))"/>
                        <p:with-option name="unzipped-basedir" select="$temp-dir"/>
                    </px:fileset-unzip>
                    <p:sink/>

                    <px:mediatype-detect name="epub3-validate.step.unzip.fileset">
                        <p:input port="source">
                            <p:pipe port="fileset" step="epub3-validate.step.unzip.unzip"/>
                        </p:input>
                    </px:mediatype-detect>

                </p:when>
                <p:otherwise>
                    <p:output port="fileset" primary="true">
                        <p:pipe port="fileset.in" step="main"/>
                    </p:output>
                    <p:output port="in-memory" sequence="true">
                        <p:pipe port="in-memory.in" step="main"/>
                    </p:output>
                    <p:sink name="epub3-validate.step.unzip.no-epub-to-unzip">
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:sink>
                </p:otherwise>
            </p:choose>

            <px:fileset-load media-types="application/oebps-package+xml" method="xml" name="epub3-validate.step.load-unzipped-fileset">
                <p:input port="in-memory">
                    <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one Package Document in the EPUB." error-code="NORDICDTBOOKEPUB011"/>
            <p:identity name="epub3-validate.step.opf"/>
            <p:sink/>

            <p:xslt name="epub3-validate.step.opf-to-spine-fileset">
                <!-- get fileset of HTML documents in spine order -->
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="source">
                    <p:pipe port="result" step="epub3-validate.step.opf"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/opf-to-spine-fileset.xsl"/>
                </p:input>
            </p:xslt>
            <px:fileset-load media-types="application/xhtml+xml" name="epub3-validate.step.load-unzipped-spine">
                <p:input port="in-memory">
                    <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" message="There must be a HTML file in the spine." error-code="NORDICDTBOOKEPUB005"/>
            <p:identity name="epub3-validate.step.html"/>
            <p:sink/>
            
            <p:identity>
                <p:input port="source">
                    <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                </p:input>
            </p:identity>
            <px:fileset-filter media-types="application/xhtml+xml" name="epub3-validate.step.filter-unzipped-html">
            </px:fileset-filter>
            <p:delete match="/*/*[not(ends-with(@href,'nav.xhtml'))]" name="epub3-validate.step.delete-nav-from-fileset"/>
            <px:fileset-load name="epub3-validate.step.load-unzipped-content-except-nav">
                <p:input port="in-memory">
                    <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There is no navigation document with the filename 'nav.xhtml' in the EPUB" error-code="NORDICDTBOOKEPUB013"/>
            <p:identity name="epub3-validate.step.nav"/>
            <p:sink/>
            
            <p:identity>
                <p:input port="source">
                    <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                </p:input>
            </p:identity>
            <px:fileset-filter media-types="application/x-dtbncx+xml" name="epub3-validate.step.filter-unzipped-ncx">
            </px:fileset-filter>
            <p:delete match="/*/*[not(ends-with(@href,'nav.ncx'))]" name="epub3-validate.step.delete-non-ncx"/>
            <px:fileset-load name="epub3-validate.step.load-ncx">
                <p:input port="in-memory">
                    <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There is no NCX with the filename 'nav.ncx' in the EPUB" error-code="NORDICDTBOOKEPUB014"/>
            <p:identity name="epub3-validate.step.ncx"/>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <p:pipe step="epub3-validate.step.opf" port="result"/>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG" message="Validating against nordic2015-1.opf.sch: $1">
                <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
            </px:message>
            <p:validate-with-schematron name="epub3-validate.step.opf.validate.schematron" assert-valid="false">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="schema">
                    <p:document href="../../schema/nordic2015-1.opf.sch"/>
                </p:input>
            </p:validate-with-schematron>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="report" step="epub3-validate.step.opf.validate.schematron"/>
                </p:input>
            </p:identity>
            <px:combine-validation-reports document-type="Nordic EPUB3 Package Document" name="epub3-validate.step.combine-validation-reports">
                <p:with-option name="document-name" select="replace(base-uri(/*),'.*/','')">
                    <p:pipe port="result" step="epub3-validate.step.opf"/>
                </p:with-option>
                <p:with-option name="document-path" select="base-uri(/*)">
                    <p:pipe port="result" step="epub3-validate.step.opf"/>
                </p:with-option>
            </px:combine-validation-reports>
            <p:identity name="epub3-validate.step.opf.validate"/>
            <p:sink/>

            <p:choose name="epub3-validate.step.choose-epub-filename">
                <p:xpath-context>
                    <p:pipe port="fileset.in" step="main"/>
                </p:xpath-context>
                <p:when test="/*/d:file[@media-type='application/epub+zip']">
                    <p:variable name="opf-identifier" select="/opf:package/opf:metadata/dc:identifier[not(@refines)][1]/text()">
                        <p:pipe port="result" step="epub3-validate.step.opf"/>
                    </p:variable>
                    <p:variable name="epub-filename" select="(/*/d:file[@media-type='application/epub+zip'])[1]/@href">
                        <p:pipe port="fileset.in" step="main"/>
                    </p:variable>
                    <p:variable name="error-count" select="if ($epub-filename = concat($opf-identifier,'.epub')) then 0 else 1"/>
                    <p:in-scope-names name="epub3-validate.step.choose-epub-filename.vars"/>
                    <p:template name="epub3-validate.step.choose-epub-filename.template">
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                        <p:input port="parameters">
                            <p:pipe port="result" step="epub3-validate.step.choose-epub-filename.vars"/>
                        </p:input>
                        <p:input port="template">
                            <p:inline exclude-inline-prefixes="#all">
                                <d:document-validation-report>
                                    <d:document-info>
                                        <d:document-name>{$epub-filename}</d:document-name>
                                        <d:document-type>Nordic EPUB3 Filename</d:document-type>
                                        <d:error-count>{$error-count}</d:error-count>
                                    </d:document-info>
                                    <d:reports>
                                        <d:report type="filecheck">
                                            <d:message severity="{if ($error-count=0) then 'info' else 'error'}" type="file-not-wellformed">
                                                <d:desc xml:space="preserve">{concat('The EPUB filename (',replace($epub-filename,'.epub$',''),') ',if ($error-count=0) then 'matches' else 'does not match',' the identifier in the package document (',$opf-identifier,')')}</d:desc>
                                                <d:location>{$epub-filename}</d:location>
                                                <d:expected>{$opf-identifier}.epub</d:expected>
                                                <d:was>{$epub-filename}</d:was>
                                            </d:message>
                                        </d:report>
                                    </d:reports>
                                </d:document-validation-report>
                            </p:inline>
                        </p:input>
                    </p:template>
                    <px:message severity="DEBUG" message="Input EPUB is zipped; filename is $1">
                        <p:with-option name="param1" select="if ($error-count=1) then 'valid' else 'invalid'"/>
                    </px:message>
                </p:when>
                <p:otherwise>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                    <px:message severity="DEBUG" message="Input EPUB is not zipped; unable to perform filename validation"/>
                </p:otherwise>
            </p:choose>
            <p:identity name="epub3-validate.step.epub-filename.validate"/>
            <p:sink/>

            <p:for-each name="epub3-validate.step.for-each-html-validate">
                <p:iteration-source select="/*/d:file[@media-type='application/xhtml+xml']">
                    <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                </p:iteration-source>
                <p:delete name="epub3-validate.step.for-each-html-validate.delete-everything-except-this-html-file-from-the-fileset">
                    <p:with-option name="match" select="concat('//d:file[not(@href=&quot;',/*/@href,'&quot;) or preceding-sibling::d:file/@href=&quot;',/*/@href,'&quot;]')"/>
                    <p:input port="source">
                        <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                    </p:input>
                </p:delete>
                <px:nordic-html-validate.step name="epub3-validate.step.for-each-html-validate.validate.html" document-type="Nordic HTML (EPUB3 Content Document)" check-images="false">
                    <p:with-option name="fail-on-error" select="$fail-on-error"/>
                    <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
                    <p:input port="in-memory.in">
                        <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                    </p:input>
                </px:nordic-html-validate.step>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="report.out" step="epub3-validate.step.for-each-html-validate.validate.html"/>
                    </p:input>
                </p:identity>
            </p:for-each>
            <p:identity name="epub3-validate.step.html.validate" cx:depends-on="epub3-validate.step.xml-declaration.validate"/>
            <p:sink/>

            <p:group name="epub3-validate.step.group-xml-declaration">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                    </p:input>
                </p:identity>
                <px:fileset-filter media-types="application/xml application/*+xml" name="epub3-validate.step.group-xml-declaration.filter-xml-files"/>
                <p:for-each name="epub3-validate.step.group-xml-declaration.iterate-files">
                    <p:iteration-source select="/*/d:file"/>
                    <p:output port="result" sequence="true">
                        <p:pipe port="result" step="epub3-validate.step.group-xml-declaration.iterate-files.xml"/>
                        <p:pipe port="result" step="epub3-validate.step.group-xml-declaration.iterate-files.doctype"/>
                    </p:output>
                    <p:variable name="href" select="resolve-uri(/*/@href,base-uri(/*))"/>

                    <!-- XML declaration -->
                    <p:try name="epub3-validate.step.group-xml-declaration.iterate-files.try">
                        <p:group>
                            <px:message message="trying to read xml declaration from $1">
                                <p:with-option name="param1" select="replace($href,'.*/','')"/>
                            </px:message>
                            <px:read-xml-declaration name="epub3-validate.step.group-xml-declaration.iterate-files.try.group.read-xml-declaration">
                                <p:with-option name="href" select="$href"/>
                            </px:read-xml-declaration>
                            <px:message message="xml declaration from $1 is $2">
                                <p:with-option name="param1" select="replace($href,'.*/','')"/>
                                <p:with-option name="param2" select="/*/replace(@xml-declaration,'[&lt;&gt;]','')"/>
                            </px:message>
                        </p:group>
                        <p:catch>
                            <px:message message="inferring xml declaration from d:file instead: $1">
                                <p:with-option name="param1" select="$href"/>
                            </px:message>
                            <p:rename match="/*" new-name="c:result" name="epub3-validate.step.group-xml-declaration.iterate-files.try.catch.rename-c-result"/>
                            <p:delete match="/*/@*[not(local-name()=('version','encoding','standalone'))]"
                                name="epub3-validate.step.group-xml-declaration.iterate-files.try.catch.delete-irrelevant-attributes"/>
                            <px:message message="xml declaration for $1 is: ?xml $2 $3 $4 ?">
                                <p:with-option name="param1" select="replace($href,'.*/','')"/>
                                <p:with-option name="param2" select="if (/*/@version) then concat('version=&quot;',/*/@version,'&quot;') else ''"/>
                                <p:with-option name="param3" select="if (/*/@encoding) then concat('encoding=&quot;',/*/@encoding,'&quot;') else ''"/>
                                <p:with-option name="param4" select="if (/*/@standalone) then concat('standalone=&quot;',/*/@standalone,'&quot;') else ''"/>
                            </px:message>
                        </p:catch>
                    </p:try>
                    <p:choose name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration">
                        <p:when test="/*/@version='1.0' and /*/@encoding=('utf-8','UTF-8') and not(/*/@standalone)">
                            <p:identity name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.correct-xml-declaration">
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <p:wrap-sequence wrapper="d:was" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.wrap-d-was"/>
                            <p:string-replace match="/*/c:result" replace="/*/c:result/@xml-declaration"
                                name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.replace-result-with-xml-declaration"/>
                            <p:wrap-sequence wrapper="d:error" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.wrap-d-error"/>
                            <p:insert match="/*" position="first-child"
                                name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.insert-desc-file-expected-placeholders">
                                <p:input port="insertion">
                                    <p:inline exclude-inline-prefixes="#all">
                                        <d:desc>PLACEHOLDER</d:desc>
                                    </p:inline>
                                    <p:inline exclude-inline-prefixes="#all">
                                        <d:file>PLACEHOLDER</d:file>
                                    </p:inline>
                                    <p:inline exclude-inline-prefixes="#all">
                                        <d:expected>&lt;?xml version="1.0" encoding="utf-8"?&gt;</d:expected>
                                    </p:inline>
                                </p:input>
                            </p:insert>
                            <p:string-replace match="/*/d:desc/text()" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.string-replace-desc">
                                <p:with-option name="replace" select="concat('&quot;Bad or missing XML declaration in: ',replace($href,'^.*/([^/]*)$','$1'),'&quot;')"/>
                            </p:string-replace>
                            <p:string-replace match="/*/d:file/text()" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-correct-xml-declaration.string-replace-file">
                                <p:with-option name="replace" select="concat('&quot;',$href,'&quot;')"/>
                            </p:string-replace>
                        </p:otherwise>
                    </p:choose>
                    <p:identity name="epub3-validate.step.group-xml-declaration.iterate-files.xml"/>

                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="current" step="epub3-validate.step.group-xml-declaration.iterate-files"/>
                        </p:input>
                    </p:identity>

                    <!-- DOCTYPE declaration -->
                    <p:choose name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html">
                        <p:when test="not(/*/@media-type='application/xhtml+xml')">
                            <px:message message="skipping doctype check for non-HTML document: $1">
                                <p:with-option name="param1" select="$href"/>
                            </px:message>
                            <p:identity name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.not-html">
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <p:try name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.try">
                                <p:group>
                                    <px:message message="trying to read doctype declaration from $1">
                                        <p:with-option name="param1" select="replace($href,'.*/','')"/>
                                    </px:message>
                                    <px:read-doctype-declaration name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.try.group.read-doctype-declaration">
                                        <p:with-option name="href" select="$href"/>
                                    </px:read-doctype-declaration>
                                    <px:message message="doctype declaration from $1 is: $2">
                                        <p:with-option name="param1" select="replace($href,'.*/','')"/>
                                        <p:with-option name="param2" select="/*/replace(string(@doctype-declaration),'[&lt;&gt;]','')"/>
                                    </px:message>
                                </p:group>
                                <p:catch>
                                    <px:message message="inferring doctype declaration from d:file instead: $1">
                                        <p:with-option name="param1" select="$href"/>
                                    </px:message>
                                    <p:rename match="/*" new-name="c:result" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.try.catch.rename-c-result"/>
                                    <p:delete match="/*/@*[not(local-name()=('doctype-public','doctype-system'))]"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.try.catch.delete-doctype"/>
                                    <px:message message="doctype declaration for $1 is: !DOCTYPE $2 $3 $4">
                                        <p:with-option name="param1" select="replace($href,'.*/','')"/>
                                        <p:with-option name="param2"
                                            select="if (@media-type='application/xhtml+xml') then 'html' else if (@media-type='application/x-dtbook+xml') then 'dtbook' else if (ends-with(@href,'opf')) then 'opf' else if (ends-with(@href,'ncx')) then 'ncx' else '(name?)'"/>
                                        <p:with-option name="param3" select="if (/*/@doctype-public) then concat('PUBLIC &quot;',/*/@doctype-public,'&quot;') else ''"/>
                                        <p:with-option name="param4"
                                            select="if (/*/@doctype-system) then concat(if (/*/@doctype-public) then '' else 'SYSTEM ', concat('&quot;',/*/@doctype-system,'&quot;')) else ''"/>
                                    </px:message>
                                </p:catch>
                            </p:try>
                            <p:choose name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype">
                                <p:when test="/*/@has-doctype-declaration='true' and /*/@name='html' and not(/*/@doctype-public) and not(/*/@doctype-system)">
                                    <p:identity name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.correct-doctype">
                                        <p:input port="source">
                                            <p:empty/>
                                        </p:input>
                                    </p:identity>
                                </p:when>
                                <p:otherwise>
                                    <p:wrap-sequence wrapper="d:was" name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.wrap-d-was"/>
                                    <p:string-replace match="/*/c:result" replace="/*/c:result/@doctype-declaration"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.string-replace-result-with-actual-doctype"/>
                                    <p:wrap-sequence wrapper="d:error"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.wrap-in-d-error"/>
                                    <p:insert match="/*" position="first-child"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.insert-desc-file-expected-placeholders">
                                        <p:input port="insertion">
                                            <p:inline exclude-inline-prefixes="#all">
                                                <d:desc>PLACEHOLDER</d:desc>
                                            </p:inline>
                                            <p:inline exclude-inline-prefixes="#all">
                                                <d:file>PLACEHOLDER</d:file>
                                            </p:inline>
                                            <p:inline exclude-inline-prefixes="#all">
                                                <d:expected>&lt;!DOCTYPE html&gt;</d:expected>
                                            </p:inline>
                                        </p:input>
                                    </p:insert>
                                    <p:string-replace match="/*/d:desc/text()"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.replace-string-desc">
                                        <p:with-option name="replace" select="concat('&quot;Bad or missing DOCTYPE declaration in: ',replace($href,'^.*/([^/]*)$','$1'),'&quot;')"/>
                                    </p:string-replace>
                                    <p:string-replace match="/*/d:file/text()"
                                        name="epub3-validate.step.group-xml-declaration.iterate-files.choose-if-html.choose-if-correct-doctype.wrong-doctype.replace-string-file">
                                        <p:with-option name="replace" select="concat('&quot;',$href,'&quot;')"/>
                                    </p:string-replace>
                                </p:otherwise>
                            </p:choose>
                        </p:otherwise>
                    </p:choose>

                    <p:identity name="epub3-validate.step.group-xml-declaration.iterate-files.doctype"/>

                </p:for-each>
                <p:wrap-sequence wrapper="d:errors" name="epub3-validate.step.group-xml-declaration.wrap-d-errors"/>
            </p:group>
            <p:identity name="epub3-validate.step.xml-declaration.validate"/>
            <p:sink/>

            <p:group name="epub3-validate.step.group-opf-and-html">
                <p:for-each name="epub3-validate.step.group-opf-and-html.iterate-opf-and-html">
                    <p:iteration-source>
                        <p:pipe step="epub3-validate.step.opf" port="result"/>
                        <p:pipe step="epub3-validate.step.html" port="result"/>
                    </p:iteration-source>
                    <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-validate.step.group-opf-and-html.iterate-opf-and-html.add-xml-base">
                        <p:with-option name="attribute-value" select="base-uri(/*)"/>
                    </p:add-attribute>
                </p:for-each>
                <p:wrap-sequence wrapper="c:result" name="epub3-validate.step.group-opf-and-html.wrap-c-result"/>
                <px:message severity="DEBUG" message="Validating against nordic2015-1.opf-and-html.sch: $1">
                    <p:with-option name="param1" select="replace(base-uri(/*/*[1]),'.*/','')"/>
                </px:message>
                <p:validate-with-schematron name="epub3-validate.step.group-opf-and-html.validate.schematron" assert-valid="false">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="schema">
                        <p:document href="../../schema/nordic2015-1.opf-and-html.sch"/>
                    </p:input>
                </p:validate-with-schematron>
                <p:sink/>
                <px:combine-validation-reports document-type="Nordic EPUB3 OPF+HTML" document-path="/" name="epub3-validate.step.group-opf-and-html.combine-validation-reports">
                    <p:input port="source">
                        <p:pipe port="report" step="epub3-validate.step.group-opf-and-html.validate.schematron"/>
                    </p:input>
                    <p:with-option name="document-name" select="'Cross-document references and metadata'"/>
                </px:combine-validation-reports>
            </p:group>
            <p:identity name="epub3-validate.step.opf-and-html.validate"/>
            <p:sink/>

            <p:choose name="epub3-validate.step.choose-if-check-images">
                <p:when test="$check-images = 'true'">
                    <px:nordic-check-image-file-signatures name="epub3-validate.step.choose-if-check-images.check-image-file-signatures">
                        <p:input port="source">
                            <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                        </p:input>
                    </px:nordic-check-image-file-signatures>
                </p:when>
                <p:otherwise>
                    <p:identity name="epub3-validate.step.choose-if-check-images.dont-check-images">
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
            <p:identity name="epub3-validate.step.images.validate"/>
            <p:sink/>

            <p:group name="epub3-validate.step.group-nav-references">
                <p:for-each name="epub3-validate.step.group-nav-references.iterate-html">
                    <p:iteration-source>
                        <p:pipe step="epub3-validate.step.html" port="result"/>
                    </p:iteration-source>
                    <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-validate.step.group-nav-references.iterate-html.add-xml-base">
                        <p:with-option name="attribute-value" select="base-uri(/*)"/>
                    </p:add-attribute>
                    <p:xslt name="epub3-validate.step.group-nav-references.iterate-html.list-heading-and-pagebreak-references">
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/list-heading-and-pagebreak-references.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:for-each>
                <p:wrap-sequence wrapper="c:result" name="epub3-validate.step.group-nav-references.iterate-html.wrap-c-result"/>
                <p:unwrap match="/*/*" name="epub3-validate.step.group-nav-references.iterate-html.unwrap-children"/>
                <p:identity name="epub3-validate.step.group-nav-references.iterate-html.heading-references"/>

                <p:insert match="/*" position="first-child" name="epub3-validate.step.group-nav-references.iterate-html.insert-heading-references-into-nav">
                    <p:input port="source">
                        <p:pipe port="result" step="epub3-validate.step.nav"/>
                    </p:input>
                    <p:input port="insertion">
                        <p:pipe port="result" step="epub3-validate.step.group-nav-references.iterate-html.heading-references"/>
                    </p:input>
                </p:insert>
                <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-validate.step.group-nav-references.iterate-html.add-xml-base-to-nav">
                    <p:with-option name="attribute-value" select="base-uri(/*)"/>
                </p:add-attribute>

                <px:message severity="DEBUG" message="Validating against nordic2015-1.nav-references.sch: $1">
                    <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                </px:message>
                <p:validate-with-schematron name="epub3-validate.step.group-nav-references.iterate-html.nav-references.validate.schematron" assert-valid="false">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="schema">
                        <p:document href="../../schema/nordic2015-1.nav-references.sch"/>
                    </p:input>
                </p:validate-with-schematron>
                <p:sink/>
                <px:combine-validation-reports document-type="Nordic EPUB3 Navigation Document References" name="epub3-validate.step.group-nav-references.iterate-html.combine-validation-reports">
                    <p:input port="source">
                        <p:pipe port="report" step="epub3-validate.step.group-nav-references.iterate-html.nav-references.validate.schematron"/>
                    </p:input>
                    <p:with-option name="document-name" select="'References from the navigation document to the content documents'"/>
                    <p:with-option name="document-path" select="base-uri(/*)">
                        <p:pipe port="result" step="epub3-validate.step.nav"/>
                    </p:with-option>
                </px:combine-validation-reports>
            </p:group>
            <p:identity name="epub3-validate.step.nav-references.validate"/>
            <p:sink/>

            <p:group name="epub3-validate.step.group-nav-ncx">
                <p:wrap-sequence wrapper="wrapper" name="epub3-validate.step.group-nav-ncx.wrap-wrapper">
                    <p:input port="source">
                        <p:pipe port="result" step="epub3-validate.step.nav"/>
                        <p:pipe port="result" step="epub3-validate.step.ncx"/>
                    </p:input>
                </p:wrap-sequence>
                <px:message severity="DEBUG" message="Validating against nordic2015-1.nav-ncx.sch: $1">
                    <p:with-option name="param1" select="replace(base-uri(/*/*[last()]),'.*/','')"/>
                </px:message>
                <p:validate-with-schematron name="epub3-validate.step.group-nav-ncx.nav-ncx.validate.schematron" assert-valid="false">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="schema">
                        <p:document href="../../schema/nordic2015-1.nav-ncx.sch"/>
                    </p:input>
                </p:validate-with-schematron>
                <p:sink/>
                <px:combine-validation-reports document-type="Nordic EPUB3 NCX and Navigation Document" name="epub3-validate.step.group-nav-ncx.combine-validation-reports">
                    <p:input port="source">
                        <p:pipe port="report" step="epub3-validate.step.group-nav-ncx.nav-ncx.validate.schematron"/>
                    </p:input>
                    <p:with-option name="document-name" select="replace(base-uri(/*),'.*/','')">
                        <p:pipe port="result" step="epub3-validate.step.ncx"/>
                    </p:with-option>
                    <p:with-option name="document-path" select="base-uri(/*)">
                        <p:pipe port="result" step="epub3-validate.step.ncx"/>
                    </p:with-option>
                </px:combine-validation-reports>
            </p:group>
            <p:identity name="epub3-validate.step.nav-ncx.validate"/>
            <p:sink/>

            <p:group name="epub3-validate.step.category-report">
                <px:fileset-filter media-types="application/xhtml+xml" name="epub3-validate.step.category-report.filter-html">
                    <p:input port="source">
                        <p:pipe port="fileset" step="epub3-validate.step.unzip"/>
                    </p:input>
                </px:fileset-filter>
                <p:delete match="/*/*[ends-with(@href,'nav.xhtml')]" name="epub3-validate.step.category-report.delete-nav-from-fileset"/>
                <px:fileset-load name="epub3-validate.step.category-report.load-html">
                    <p:input port="in-memory">
                        <p:pipe port="in-memory" step="epub3-validate.step.unzip"/>
                    </p:input>
                </px:fileset-load>
                <p:wrap-sequence wrapper="wrapper" name="epub3-validate.step.category-report.wrap-wrapper"/>
                <p:xslt name="epub3-validate.step.category-report.determine-complexity">
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/determine-complexity.xsl"/>
                    </p:input>
                </p:xslt>
            </p:group>
            <p:identity name="epub3-validate.step.category.html-report"/>
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
