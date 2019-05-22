<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-html-to-dtbook.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
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

    <!-- option supporting convert to DTBook 1.1.0 -->
    <p:option name="dtbook2005" required="false" select="'true'"/>
    <p:option name="fail-on-error" required="true"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

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
                <p:pipe port="result" step="html-to-dtbook.step.result.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="html-to-dtbook.step.result.in-memory"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>



            <px:fileset-load media-types="application/xhtml+xml" name="html-to-dtbook.step.load-xhtml">
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-max="1" message="There are multiple HTML files in the fileset; only the first one will be converted."/>
            <px:assert test-count-min="1" message="There must be a HTML file in the fileset." error-code="NORDICDTBOOKEPUB005"/>
            <p:split-sequence initial-only="true" test="position()=1" name="html-to-dtbook.step.split-sequence-only-first"/>
            <px:assert message="The HTML file must have a file extension." error-code="NORDICDTBOOKEPUB006">
                <p:with-option name="test" select="matches(base-uri(/*),'.*[^\.]\.[^\.]*$')"/>
            </px:assert>
            <p:identity name="html-to-dtbook.step.input-html"/>

            <!-- Make sure only sections corresponding to html:h[1-6] are used. -->
            <p:xslt name="html-to-dtbook.step.deep-level-grouping">
                <p:with-param name="name" select="'section article'"/>
                <p:with-param name="namespace" select="'http://www.w3.org/1999/xhtml'"/>
                <p:with-param name="max-depth" select="6"/>
                <p:with-param name="copy-wrapping-elements-into-result" select="true()"/>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/deep-level-grouping.xsl"/>
                </p:input>
            </p:xslt>

            <p:xslt name="html-to-dtbook.step.epub3-to-dtbook">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/epub3-to-dtbook.xsl"/>
                </p:input>
            </p:xslt>
            <p:choose name="html-to-dtbook.step.choose-convert-to-dtbook110">
                <p:when test="$dtbook2005='true'">
                    <!-- keep DTBook 2005-3 -->
                    <p:identity name="html-to-dtbook.step.choose-convert-to-dtbook110.do-not-convert"/>
                </p:when>
                <p:otherwise>
                    <!-- convert to DTBook 1.1.0 -->
                    <p:xslt name="html-to-dtbook.step.choose-convert-to-dtbook110.dtbook2005-to-dtbook110">
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/dtbook2005-to-dtbook110.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:otherwise>
            </p:choose>

            <p:add-attribute match="/*" attribute-name="xml:base" name="html-to-dtbook.step.add-xml-base-to-dtbook">
                <p:with-option name="attribute-value" select="concat(replace(base-uri(/*),'^(.*)\.[^/\.]*$','$1'),'.xml')">
                    <p:pipe port="result" step="html-to-dtbook.step.input-html"/>
                </p:with-option>
            </p:add-attribute>
            <p:xslt name="html-to-dtbook.step.pretty-print-dtbook">
                <!-- TODO: remove all pretty-printing to improve performance -->
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/pretty-print.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="html-to-dtbook.step.result.in-memory"/>
            <p:sink/>

            <p:identity>
                <p:input port="source">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
            </p:identity>
            <p:delete match="//d:file[@media-type=('application/xhtml+xml','text/css')]" name="html-to-dtbook.step.input-fileset-delete-xhtml-and-css"/>
            <px:fileset-add-entry media-type="application/x-dtbook+xml" name="html-to-dtbook.step.add-dtbook-to-fileset">
                <p:with-option name="href" select="(/*/d:file[@media-type='application/xhtml+xml'])[1]/replace(replace(@href,'.*/',''),'\.[^\.]*$','.xml')">
                    <p:pipe port="fileset.in" step="main"/>
                </p:with-option>
            </px:fileset-add-entry>
            <p:viewport match="//d:file[starts-with(@href,'images/')]" name="html-to-dtbook.step.move-images-in-fileset-to-images-directory">
                <p:add-attribute match="/*" attribute-name="href" name="html-to-dtbook.step.move-images-in-fileset-to-images-directory.add-attribute">
                    <p:with-option name="attribute-value" select="replace(/*/@href,'^images/','')"/>
                </p:add-attribute>
            </p:viewport>
            <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="omit-xml-declaration" attribute-value="false" name="html-to-dtbook.step.dtbook-omit-xml-declaration"/>
            <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="version" attribute-value="1.0" name="html-to-dtbook.step.dtbook-xml-version"/>
            <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="encoding" attribute-value="utf-8" name="html-to-dtbook.step.dtbook-xml-encoding"/>
            <p:choose name="html-to-dtbook.step.choose-dtbook2005-or-dtbook110-doctype">
                <p:when test="$dtbook2005='true'">
                    <!-- add doctype attributes to DTBook 2005-3 -->
                    <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="doctype-public" attribute-value="-//NISO//DTD dtbook 2005-3//EN" name="html-to-dtbook.step.choose-dtbook2005-or-dtbook110-doctype.2005-3.public"/>
                    <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="doctype-system" attribute-value="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd" name="html-to-dtbook.step.choose-dtbook2005-or-dtbook110-doctype.2005-3.system"/>
                </p:when>
                <p:otherwise>
                    <!-- add standalone 'yes' to DTBook 1.1.0 -->
                    <p:add-attribute match="//d:file[@media-type='application/x-dtbook+xml']" attribute-name="standalone" attribute-value="true" name="html-to-dtbook.step.choose-dtbook2005-or-dtbook110-doctype.1.1.0.standalone"/>
                </p:otherwise>
            </p:choose>
            <p:xslt name="html-to-dtbook.step.pretty-print-fileset">
                <!-- TODO: remove all pretty-printing to improve performance -->
                <p:with-param name="preserve-empty-whitespace" select="'false'"/>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/pretty-print.xsl"/>
                </p:input>
            </p:xslt>
            <p:identity name="html-to-dtbook.step.result.fileset"/>




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
