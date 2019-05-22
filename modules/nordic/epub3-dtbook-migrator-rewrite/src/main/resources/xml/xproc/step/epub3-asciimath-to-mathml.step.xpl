<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" type="px:nordic-epub3-asciimath-to-mathml.step"
    name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:mathml="http://www.w3.org/1998/Math/MathML">

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
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/asciimath-utils/library.xpl"/>

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
                <p:pipe port="fileset.in" step="main"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="epub3-asciimath-to-mathml.step.in-memory.opf"/>
                <p:pipe port="result" step="epub3-asciimath-to-mathml.step.in-memory.xhtml"/>
                <p:pipe port="result" step="epub3-asciimath-to-mathml.step.in-memory.other"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>



            <px:fileset-load media-types="application/xhtml+xml" name="epub3-asciimath-to-mathml.step.load-html-files">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <p:for-each name="epub3-asciimath-to-mathml.step.iterate-html-files">
                <px:message message="Checking for ASCIIMath in $1">
                    <p:with-option name="param1" select="base-uri(/*)"/>
                </px:message>
                <p:viewport match="//*[tokenize(@class,'\s+')='asciimath']" name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements">
                    <px:message message="Converting to MathML: '$1'">
                        <p:with-option name="param1" select="string-join(.//text(),'')"/>
                    </px:message>

                    <p:identity name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.asciimath"/>
                    <p:choose name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available">
                        <p:when test="p:step-available('px:asciimathml')">
                            <px:asciimathml name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.mathml">
                                <p:with-option name="asciimath" select="string-join(.//text(),'')"/>
                            </px:asciimathml>

                            <p:identity name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.epub-switch-wrapper">
                                <p:input port="source">
                                    <p:inline exclude-inline-prefixes="#all">
                                        <epub:switch>
                                            <epub:case required-namespace="http://www.w3.org/1998/Math/MathML"/>
                                            <epub:default/>
                                        </epub:switch>
                                    </p:inline>
                                </p:input>
                            </p:identity>
                            <p:insert match="/*/epub:case" position="first-child" name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.insert-mathml-into-epub-case">
                                <p:input port="insertion">
                                    <p:pipe port="result" step="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.mathml"/>
                                </p:input>
                            </p:insert>
                            <p:insert match="/*/epub:default" position="first-child" name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.insert-asciimath-into-epub-default">
                                <p:input port="insertion">
                                    <p:pipe port="result" step="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.asciimath"/>
                                </p:input>
                            </p:insert>

                        </p:when>
                        <p:otherwise>
                            <px:message severity="WARNING" message="px:asciimathml is not available; asciimath element will be replaced by placeholder"/>
                            <p:add-attribute match="/*" attribute-name="alt" name="epub3-asciimath-to-mathml.step.iterate-html-files.viewport-asciimath-elements.choose-step-available.step-not-available">
                                <p:input port="source">
                                    <p:inline>
                                        <math xmlns="http://www.w3.org/1998/Math/MathML"/>
                                    </p:inline>
                                </p:input>
                                <p:with-option name="attribute-value" select="string-join(.//text(),'')"/>
                            </p:add-attribute>
                        </p:otherwise>
                    </p:choose>

                </p:viewport>
            </p:for-each>
            <p:identity name="epub3-asciimath-to-mathml.step.in-memory.xhtml"/>
            <p:split-sequence test="//mathml:*" name="epub3-asciimath-to-mathml.step.split-sequence-mathml"/>
            <p:for-each name="epub3-asciimath-to-mathml.step.iterate-mathml">
                <p:delete match="/*/node()" name="epub3-asciimath-to-mathml.step.iterate-mathml.delete-content"/>
                <p:add-attribute match="/*" attribute-name="xml:base" name="epub3-asciimath-to-mathml.step.iterate-mathml.add-xml-base">
                    <p:with-option name="attribute-value" select="base-uri(/*)"/>
                </p:add-attribute>
            </p:for-each>
            <p:wrap-sequence wrapper="wrapper" name="epub3-asciimath-to-mathml.step.mathml-sequence-wrapper"/>
            <p:identity name="epub3-asciimath-to-mathml.step.xhtml-documents-with-mathml"/>
            <p:sink/>

            <px:fileset-load media-types="application/oebps-package+xml" method="xml" name="epub3-asciimath-to-mathml.step.load-opf">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one Package Document in the EPUB." error-code="NORDICDTBOOKEPUB011"/>
            <p:viewport match="/opf:package/opf:manifest/opf:item" name="epub3-asciimath-to-mathml.step.viewport-opf-items">
                <p:variable name="item-uri" select="resolve-uri(@href,base-uri(/*))"/>
                <p:choose name="epub3-asciimath-to-mathml.step.viewport-opf-items.choose-if-contains-mathml">
                    <p:xpath-context>
                        <p:pipe port="result" step="epub3-asciimath-to-mathml.step.xhtml-documents-with-mathml"/>
                    </p:xpath-context>
                    <p:when test="$item-uri = /*/*/@xml:base">
                        <!-- item contains MathML; add "mathml" to list of properties -->
                        <p:add-attribute match="/*" attribute-name="properties" attribute-value="string-join(distinct-values((/*/tokenize(@properties,'\s+'), 'mathml')),' ')" name="epub3-asciimath-to-mathml.step.viewport-opf-items.choose-if-contains-mathml.item-contains-mathml"/>
                    </p:when>
                    <p:otherwise>
                        <!-- MathML was not added to the item; don't do anything -->
                        <p:identity name="epub3-asciimath-to-mathml.step.viewport-opf-items.choose-if-contains-mathml.item-does-not-contain-mathml"/>
                    </p:otherwise>
                </p:choose>
            </p:viewport>
            <p:identity name="epub3-asciimath-to-mathml.step.in-memory.opf"/>
            <p:sink/>

            <p:for-each name="epub3-asciimath-to-mathml.step.iterate-in-memory.in">
                <p:iteration-source>
                    <p:pipe port="in-memory.in" step="main"/>
                </p:iteration-source>
                <p:choose name="epub3-asciimath-to-mathml.step.iterate-in-memory.in.choose-if-xhtml-or-opf">
                    <p:when test="ends-with(base-uri(/*),'.xhtml') or ends-with(base-uri(/*),'.opf')">
                        <p:identity name="epub3-asciimath-to-mathml.step.iterate-in-memory.in.choose-if-xhtml-or-opf.discard-xhtml-or-opf">
                            <p:input port="source">
                                <p:empty/>
                            </p:input>
                        </p:identity>
                    </p:when>
                    <p:otherwise>
                        <p:identity name="epub3-asciimath-to-mathml.step.iterate-in-memory.in.choose-if-xhtml-or-opf.retain-other-resources"/>
                    </p:otherwise>
                </p:choose>
            </p:for-each>
            <p:identity name="epub3-asciimath-to-mathml.step.in-memory.other"/>
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
