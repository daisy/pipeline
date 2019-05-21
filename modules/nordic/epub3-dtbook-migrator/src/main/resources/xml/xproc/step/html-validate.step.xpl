<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-html-validate.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:l="http://xproc.org/library">

    <p:serialization port="report.out" indent="true"/>

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
    <p:option name="check-images" required="false" select="'true'"/>
    <p:option name="organization-specific-validation" required="false" select="''"/>
    <p:option name="document-type" required="false" select="'Nordic HTML'"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="check-image-file-signatures.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

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
                <p:pipe port="in-memory.in" step="main"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:pipe port="result" step="html-validate.step.html.validate"/>
                <p:pipe port="result" step="html-validate.step.images.validate"/>
            </p:output>



            <!-- either load from memory or using p:load; avoid using px:html-load as it will remove the nordic namespace -->
            <p:identity>
                <p:input port="source">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </p:identity>
            <p:for-each>
                <px:normalize-document-base/>
            </p:for-each>
            <p:identity name="html-validate.step.in-memory-normalized-base"/>
            <px:fileset-load media-types="application/xhtml+xml" load-if-not-in-memory="false" name="html-validate.step.load-xhtml">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="result" step="html-validate.step.in-memory-normalized-base"/>
                </p:input>
            </px:fileset-load>
            <p:identity name="html-validate.step.html.from-memory"/>
            <p:count name="html-validate.step.count-xhtml"/>
            <p:choose name="html-validate.step.choose-already-in-memory">
                <p:when test="/*=0">
                    <px:fileset-filter media-types="application/xhtml+xml" name="html-validate.step.choose-already-in-memory.filter-fileset">
                        <p:input port="source">
                            <p:pipe port="fileset.in" step="main"/>
                        </p:input>
                    </px:fileset-filter>
                    <px:assert message="There must be exactly one HTML-file in the fileset." error-code="NORDICDTBOOKEPUB031">
                        <p:with-option name="test" select="count(/*/*) = 1"/>
                    </px:assert>
                    <p:load name="html-validate.step.choose-already-in-memory.load-from-disk">
                        <p:with-option name="href" select="/*/*[1]/resolve-uri(@href,base-uri(.))"/>
                    </p:load>
                </p:when>
                <p:otherwise>
                    <p:identity name="html-validate.step.choose-already-in-memory.pipe-from-memory">
                        <p:input port="source">
                            <p:pipe port="result" step="html-validate.step.html.from-memory"/>
                        </p:input>
                    </p:identity>
                    <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one HTML-file in the fileset." error-code="NORDICDTBOOKEPUB031"/>
                </p:otherwise>
            </p:choose>
            <p:delete match="/*/@xml:base" name="html-validate.step.delete-xml-base-from-input-html"/>
            <p:identity name="html-validate.step.html"/>
            <p:sink/>

            <l:relax-ng-report name="html-validate.step.validate.rng">
                <p:input port="source">
                    <p:pipe step="html-validate.step.html" port="result"/>
                </p:input>
                <p:input port="schema">
                    <p:document href="../../schema/nordic-html5.rng"/>
                </p:input>
                <p:with-option name="dtd-attribute-values" select="'false'"/>
                <p:with-option name="dtd-id-idref-warnings" select="'false'"/>
            </l:relax-ng-report>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <p:pipe step="html-validate.step.html" port="result"/>
                </p:input>
            </p:identity>
            <p:choose>
                <p:when test="lower-case($organization-specific-validation) = 'nota'">
                    <px:message severity="DEBUG" message="Validating against nordic2015-1.nota.sch: $1">
                        <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                    </px:message>
                    <p:validate-with-schematron name="html-validate.step.validate.sch.nota" assert-valid="false">
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="schema">
                            <p:document href="../../schema/nordic2015-1.nota.sch"/>
                        </p:input>
                    </p:validate-with-schematron>
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="report" step="html-validate.step.validate.sch.nota"/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <px:message severity="DEBUG" message="Validating against nordic2015-1.sch: $1">
                        <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                    </px:message>
                    <p:validate-with-schematron name="html-validate.step.validate.sch.generic" assert-valid="false">
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="schema">
                            <p:document href="../../schema/nordic2015-1.sch"/>
                        </p:input>
                    </p:validate-with-schematron>
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe port="report" step="html-validate.step.validate.sch.generic"/>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
            <p:identity name="html-validate.step.validate.sch"/>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <!-- not completely sure why, but a XD0005 error is thrown without this p:identity. -->
                    <p:empty/>
                </p:input>
            </p:identity>
            <px:combine-validation-reports name="html-validate.step.combine-validation-reports">
                <p:with-option name="document-type" select="$document-type"/>
                <p:input port="source">
                    <p:pipe port="report" step="html-validate.step.validate.rng"/>
                    <p:pipe port="result" step="html-validate.step.validate.sch"/>
                </p:input>
                <p:with-option name="document-name" select="replace(base-uri(/*),'.*/','')">
                    <p:pipe port="result" step="html-validate.step.html"/>
                </p:with-option>
                <p:with-option name="document-path" select="base-uri(/*)">
                    <p:pipe port="result" step="html-validate.step.html"/>
                </p:with-option>
            </px:combine-validation-reports>
            <p:identity name="html-validate.step.html.validate"/>
            <p:sink/>

            <p:choose name="html-validate.step.choose-if-check-images">
                <p:when test="$check-images = 'true'">
                    <px:nordic-check-image-file-signatures name="html-validate.step.choose-if-check-images.check-image-file-signatures">
                        <p:input port="source">
                            <p:pipe port="fileset.in" step="main"/>
                        </p:input>
                    </px:nordic-check-image-file-signatures>
                </p:when>
                <p:otherwise>
                    <p:identity name="html-validate.step.choose-if-check-images.not-checking-images">
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
            <p:identity name="html-validate.step.images.validate"/>
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
