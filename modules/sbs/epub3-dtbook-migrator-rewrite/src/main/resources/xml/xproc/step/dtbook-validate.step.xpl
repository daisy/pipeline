<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-dtbook-validate.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:l="http://xproc.org/library">

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
    <p:option name="allow-legacy" required="false" select="'true'"/>

    <!-- option supporting convert to DTBook 1.1.0 -->
    <p:option name="dtbook2005" required="false" select="'true'"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="check-image-file-signatures.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-validator/dtbook-validator.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

    <px:assert message="'fail-on-error' should be either 'true' or 'false'. was: '$1'. will default to 'true'.">
        <p:with-option name="param1" select="$fail-on-error"/>
        <p:with-option name="test" select="$fail-on-error = ('true','false')"/>
    </px:assert>
    <px:assert message="'check-images' should be either 'true' or 'false'. was: '$1'. will default to 'false'.">
        <p:with-option name="param1" select="$check-images"/>
        <p:with-option name="test" select="$check-images = ('true','false')"/>
    </px:assert>
    <px:assert message="'allow-legacy' should be either 'true' or 'false'. was: '$1'. will default to 'true'.">
        <p:with-option name="param1" select="$allow-legacy"/>
        <p:with-option name="test" select="$allow-legacy = ('true','false')"/>
    </px:assert>

    <p:choose name="choose">
        <p:xpath-context>
            <p:pipe port="status.in" step="main"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' or $fail-on-error = 'false'">
            <p:output port="fileset.out" primary="true">
                <p:pipe port="fileset.out" step="choose.inner"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="in-memory.out" step="choose.inner"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:pipe port="report.out" step="choose.inner"/>
            </p:output>

            <px:message severity="DEBUG" message="Validating DTBook according to DTBook specification..."/>
            <!--
                FIXME: use px:dtbook-validator instead of px:dtbook-validator.script
            -->
            <px:dtbook-validator.script name="dtbook-validate.step.validate.input-dtbook.generic">
                <p:with-option name="input-dtbook" select="(/*/*[@media-type='application/x-dtbook+xml']/resolve-uri(@href,base-uri(.)))[1]"/>
                <p:with-option name="check-images" select="$check-images"/>
            </px:dtbook-validator.script>

            <p:choose name="choose.inner">
                <p:xpath-context>
                    <p:pipe port="validation-status" step="dtbook-validate.step.validate.input-dtbook.generic"/>
                </p:xpath-context>
                <p:when test="not(/*/@result='ok')">
                    <p:output port="fileset.out">
                        <p:pipe port="fileset.in" step="main"/>
                    </p:output>
                    <p:output port="in-memory.out" sequence="true">
                        <p:pipe port="in-memory.in" step="main"/>
                    </p:output>
                    <p:output port="report.out" sequence="true">
                        <p:pipe port="report" step="dtbook-validate.step.validate.input-dtbook.generic"/>
                    </p:output>

                    <p:sink>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:sink>
                </p:when>

                <p:otherwise>
                    <p:output port="fileset.out">
                        <p:pipe port="result" step="dtbook-validate.step.input-dtbook.fileset"/>
                    </p:output>
                    <p:output port="in-memory.out" sequence="true">
                        <p:pipe port="in-memory.out" step="dtbook-validate.step.input-dtbook.in-memory"/>
                    </p:output>
                    <p:output port="report.out" sequence="true">
                        <p:pipe port="report" step="dtbook-validate.step.validate.input-dtbook.generic"/>
                        <p:pipe port="result" step="dtbook-validate.step.validate.input-dtbook.nordic"/>
                        <p:pipe port="result" step="dtbook-validate.step.validate.images"/>
                    </p:output>

                    <px:fileset-filter media-types="application/x-dtbook+xml" name="dtbook-validate.step.filter-dtbook-from-fileset">
                        <p:input port="source">
                            <p:pipe port="fileset.in" step="main"/>
                        </p:input>
                    </px:fileset-filter>
                    <px:assert message="There should be exactly one DTBook (was: $1)">
                        <p:with-option name="test" select="count(/*/*) = 1"/>
                        <p:with-option name="param1" select="count(/*/*)"/>
                    </px:assert>
                    <p:delete match="/*/*[position() &gt; 1]" name="dtbook-validate.step.delete-except-first-dtbook-in-fileset"/>
                    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook-validate.step.load-dtbook">
                        <p:input port="in-memory">
                            <p:pipe port="in-memory.in" step="main"/>
                        </p:input>
                    </px:fileset-load>
                    <p:choose name="dtbook-validate.step.choose-if-legacy">
                        <p:when test="$allow-legacy='true' and $dtbook2005='true'">
                            <px:upgrade-dtbook name="dtbook-validate.step.choose-if-legacy.upgrade-to-2005-3">
                                <p:input port="parameters">
                                    <p:empty/>
                                </p:input>
                            </px:upgrade-dtbook>
                            <px:message severity="DEBUG" message="Cleaning up legacy markup"/>
                            <p:xslt name="dtbook-validate.step.dtbook-legacy-fix">
                                <p:input port="parameters">
                                    <p:empty/>
                                </p:input>
                                <p:input port="stylesheet">
                                    <p:document href="../../xslt/dtbook-legacy-fix.xsl"/>
                                </p:input>
                            </p:xslt>
                            <p:identity/>
                        </p:when>
                        <p:otherwise>
                            <p:identity name="dtbook-validate.step.no-legacy"/>
                        </p:otherwise>
                    </p:choose>
                    <p:identity name="dtbook-validate.step.input-dtbook"/>

                    <p:choose name="dtbook-validate.step.choose-if-not-dtbook110">
                        <p:when test="$dtbook2005='true'">
                            <p:output port="result" sequence="true"/>

                            <px:message message="Validating DTBook according to Nordic specification..."/>
                            <px:message severity="DEBUG" message="Validating against nordic-dtbook-2005-3.rng"/>
                            <l:relax-ng-report name="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.nordic.validation">
                                <p:input port="schema">
                                    <p:document href="../../schema/nordic-dtbook-2005-3.rng"/>
                                </p:input>
                                <p:with-option name="dtd-attribute-values" select="'false'"/>
                                <p:with-option name="dtd-id-idref-warnings" select="'false'"/>
                            </l:relax-ng-report>
                            <p:sink/>

                            <p:identity>
                                <p:input port="source">
                                    <p:pipe step="dtbook-validate.step.input-dtbook" port="result"/>
                                </p:input>
                            </p:identity>
                            <p:choose>
                                <p:when test="lower-case($organization-specific-validation) = 'nota'">
                                    <px:message severity="DEBUG" message="Validating against mtm2015-1.nota.sch: $1">
                                        <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                                    </px:message>
                                    <p:validate-with-schematron name="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation.nota" assert-valid="false">
                                        <p:input port="parameters">
                                            <p:empty/>
                                        </p:input>
                                        <p:input port="schema">
                                            <p:document href="../../schema/mtm2015-1.nota.sch"/>
                                        </p:input>
                                    </p:validate-with-schematron>
                                    <p:sink/>
                                    <p:identity>
                                        <p:input port="source">
                                            <p:pipe port="report" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation.nota"/>
                                        </p:input>
                                    </p:identity>
                                </p:when>
                                <p:otherwise>
                                    <px:message severity="DEBUG" message="Validating against mtm2015-1.sch: $1">
                                        <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
                                    </px:message>
                                    <p:validate-with-schematron name="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation.generic" assert-valid="false">
                                        <p:input port="parameters">
                                            <p:empty/>
                                        </p:input>
                                        <p:input port="schema">
                                            <p:document href="../../schema/mtm2015-1.sch"/>
                                        </p:input>
                                    </p:validate-with-schematron>
                                    <p:sink/>
                                    <p:identity>
                                        <p:input port="source">
                                            <p:pipe port="report" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation.generic"/>
                                        </p:input>
                                    </p:identity>
                                </p:otherwise>
                            </p:choose>
                            <p:identity name="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation"/>
                            <p:sink/>

                            <p:identity>
                                <p:input port="source">
                                    <p:pipe port="report" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.nordic.validation"/>
                                    <p:pipe port="result" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation"/>
                                </p:input>
                            </p:identity>
                            <px:combine-validation-reports document-type="Nordic DTBook" name="dtbook-validate.step.choose-if-not-dtbook110.combine-validation-reports">
                                <p:input port="source">
                                    <p:pipe port="report" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.nordic.validation"/>
                                    <p:pipe port="result" step="dtbook-validate.step.choose-if-not-dtbook110.validate.input-dtbook.mtm.validation"/>
                                </p:input>
                                <p:with-option name="document-name" select="replace((/*/*[@media-type='application/x-dtbook+xml']/@href)[1],'.*/','')">
                                    <p:pipe port="fileset.in" step="main"/>
                                </p:with-option>
                                <p:with-option name="document-path" select="(/*/*[@media-type='application/x-dtbook+xml']/resolve-uri(@href,base-uri()))[1]">
                                    <p:pipe port="fileset.in" step="main"/>
                                </p:with-option>
                            </px:combine-validation-reports>

                        </p:when>
                        <p:otherwise>
                            <p:output port="result" sequence="true"/>

                            <!-- DTBook 1.1.0 => no validation -->
                            <p:identity name="dtbook-validate.step.choose-if-not-dtbook110.dtbook110">
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:otherwise>
                    </p:choose>
                    <p:identity name="dtbook-validate.step.validate.input-dtbook.nordic"/>
                    <p:sink/>

                    <px:dtbook-load name="dtbook-validate.step.input-dtbook.in-memory">
                        <p:input port="source">
                            <p:pipe port="result" step="dtbook-validate.step.input-dtbook"/>
                        </p:input>
                    </px:dtbook-load>
                    <p:viewport match="/*/*" name="dtbook-validate.step.viewport-dtbook-fileset">
                        <px:message message="setting original-href for $1 to $2">
                            <p:with-option name="param1" select="/*/@href"/>
                            <p:with-option name="param2" select="if (/*/@original-href) then /*/@original-href else resolve-uri(/*/@href, base-uri(/*))"/>
                        </px:message>
                        <p:add-attribute match="/*" attribute-name="original-href" name="dtbook-validate.step.viewport-dtbook-fileset.add-original-href">
                            <p:with-option name="attribute-value" select="if (/*/@original-href) then /*/@original-href else resolve-uri(/*/@href, base-uri(/*))"/>
                        </p:add-attribute>
                    </p:viewport>
                    <px:mediatype-detect name="dtbook-validate.step.input-dtbook.fileset"/>
                    <p:sink/>

                    <p:choose name="dtbook-validate.step.choose-if-check-images">
                        <p:when test="$check-images = 'true'">
                            <px:nordic-check-image-file-signatures name="dtbook-validate.step.choose-if-check-images.checking-images">
                                <p:input port="source">
                                    <p:pipe port="result" step="dtbook-validate.step.input-dtbook.fileset"/>
                                </p:input>
                            </px:nordic-check-image-file-signatures>
                        </p:when>
                        <p:otherwise>
                            <p:identity name="dtbook-validate.step.choose-if-check-images.not-checking-image">
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:otherwise>
                    </p:choose>
                    <p:identity name="dtbook-validate.step.validate.images"/>
                    <p:sink/>

                </p:otherwise>
            </p:choose>

        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="in-memory.in" step="main"/>
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
