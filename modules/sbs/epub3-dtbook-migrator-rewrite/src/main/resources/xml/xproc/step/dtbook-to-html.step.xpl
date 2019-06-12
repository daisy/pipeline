<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-dtbook-to-html.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:cx="http://xmlcalabash.com/ns/extensions">

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

    <p:input port="xslt">
        <p:document href="../../xslt/dtbook-to-epub3.xsl"/>
    </p:input>

    <p:option name="fail-on-error" required="true"/>
    <p:option name="temp-dir" required="true"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

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
                <p:pipe port="result" step="dtbook-to-html.step.result.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="dtbook-to-html.step.result.in-memory"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>










            <p:variable name="href" select="resolve-uri((//d:file[@media-type='application/x-dtbook+xml'])[1]/@href,base-uri(/))"/>

            <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook-to-html.step.load-dtbook">
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-max="1" message="There are multiple DTBooks in the fileset; only the first one will be converted."/>
            <px:assert test-count-min="1" message="There must be a DTBook file in the fileset." error-code="NORDICDTBOOKEPUB004"/>
            <p:split-sequence initial-only="true" test="position()=1" name="dtbook-to-html.step.only-use-first-dtbook"/>
            <p:identity name="dtbook-to-html.step.dtbook"/>

            <p:xslt name="dtbook-to-html.step.dtbook-to-epub3">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:pipe step="main" port="xslt"/>
                </p:input>
            </p:xslt>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/update-epub-prefixes.xsl"/>
                </p:input>
            </p:xslt>

            <p:viewport match="/html:html/html:head" name="dtbook-to-html.step.viewport-html-head">
                <p:xslt name="dtbook-to-html.step.viewport-html-head.pretty-print">
                    <!-- TODO: consider dropping this if it causes performance issues -->
                    <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/pretty-print.xsl"/>
                    </p:input>
                </p:xslt>
            </p:viewport>
            <!-- TODO: add ASCIIMathML.js if there are asciimath elements -->

            <p:add-attribute match="/*" attribute-name="xml:base" name="dtbook-to-html.step.set-xml-base">
                <p:with-option name="attribute-value" select="concat($temp-dir,(//dtbook:meta[@name='dtb:uid']/@content,'missing-uid')[1],'.xhtml')">
                    <p:pipe port="result" step="dtbook-to-html.step.dtbook"/>
                </p:with-option>
            </p:add-attribute>
            <p:identity name="dtbook-to-html.step.result.in-memory"/>
            <p:sink/>

            <px:fileset-filter not-media-types="application/x-dtbook+xml text/css" name="dtbook-to-html.step.filter-resources">
                <p:input port="source">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
            </px:fileset-filter>
            <px:fileset-move name="dtbook-to-html.step.move-resources">
                <p:with-option name="new-base" select="$temp-dir"/>
            </px:fileset-move>
            <p:viewport match="/*/*[starts-with(@media-type,'image/')]" name="dtbook-to-html.step.viewport-images">
                <p:add-attribute match="/*" attribute-name="href" name="dtbook-to-html.step.viewport-images.change-href">
                    <p:with-option name="attribute-value" select="concat('images/',/*/@href)"/>
                </p:add-attribute>
            </p:viewport>
            <p:identity name="dtbook-to-html.step.fileset.existing-resources"/>

            <px:fileset-create name="dtbook-to-html.step.create-temp-dir-fileset">
                <p:with-option name="base" select="$temp-dir"/>
            </px:fileset-create>
            <p:viewport match="/*/*" name="dtbook-to-html.step.viewport-fileset">
                <p:add-attribute match="/*" attribute-name="original-href" name="dtbook-to-html.step.viewport-fileset.add-original-href">
                    <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri(/*))"/>
                </p:add-attribute>
            </p:viewport>
            <px:fileset-add-entry media-type="application/xhtml+xml" name="dtbook-to-html.step.add-html-to-fileset">
                <p:with-option name="href" select="base-uri(/*)">
                    <p:pipe port="result" step="dtbook-to-html.step.result.in-memory"/>
                </p:with-option>
            </px:fileset-add-entry>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="omit-xml-declaration" attribute-value="false" name="dtbook-to-html.step.dont-omit-xml-declaration"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="version" attribute-value="1.0" name="dtbook-to-html.step.set-xml-version"/>
            <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']" attribute-name="encoding" attribute-value="utf-8" name="dtbook-to-html.step.set-xml-encoding"/>
            <p:identity name="dtbook-to-html.step.fileset.new-resources"/>
            <px:fileset-join name="dtbook-to-html.step.fileset.join-old-and-new-resources">
                <p:input port="source">
                    <p:pipe port="result" step="dtbook-to-html.step.fileset.existing-resources"/>
                    <p:pipe port="result" step="dtbook-to-html.step.fileset.new-resources"/>
                </p:input>
            </px:fileset-join>
            <p:identity name="dtbook-to-html.step.result.fileset"/>










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
