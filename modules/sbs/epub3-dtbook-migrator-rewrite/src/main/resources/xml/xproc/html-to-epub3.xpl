<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-html-to-epub3" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:l="http://xproc.org/library" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Nordic HTML5 to EPUB3</h1>
        <p px:role="desc">Transforms a HTML document into an EPUB3 publication according to the nordic markup guidelines.</p>
    </p:documentation>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc">Validation status (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe port="result" step="status"/>
    </p:output>

    <p:option name="html-report" required="true" px:output="result" px:type="anyDirURI" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML Report</h1>
            <p px:role="desc">An HTML-formatted version of the validation report.</p>
        </p:documentation>
    </p:option>

    <p:option name="html" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">Input DTBook to be converted.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="organization-specific-validation" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Organization-specific validation</h2>
            <p px:role="desc">Leave blank for the default validation schemas. Use 'nota' to validate using Nota-specific validation rules.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Temporary directory for use by the script.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Output directory</h2>
            <p px:role="desc">Output directory for the EPUB publication.</p>
        </p:documentation>
    </p:option>

    <p:option name="fail-on-error" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Stop processing on validation error</h2>
            <p px:role="desc">Whether or not to stop the conversion when a validation error occurs. Setting this to false may be useful for debugging or if the validation error is a minor one. The
                output is not guaranteed to be valid if this option is set to false.</p>
        </p:documentation>
    </p:option>

    <p:import href="step/validation-status.xpl"/>
    <p:import href="step/html-validate.step.xpl"/>
    <p:import href="step/html-to-epub3.step.xpl"/>
    <p:import href="step/epub3-store.step.xpl"/>
    <p:import href="step/epub3-validate.step.xpl"/>
    <p:import href="step/format-html-report.xpl"/>
    <p:import href="step/fail-on-error-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

    <px:message message="$1">
        <p:with-option name="param1" select="/*">
            <p:document href="../version-description.xml"/>
        </p:with-option>
    </px:message>
    
    <px:normalize-uri name="html">
        <p:with-option name="href" select="resolve-uri($html,static-base-uri())"/>
    </px:normalize-uri>
    <px:normalize-uri name="html-report">
        <p:with-option name="href" select="resolve-uri($html-report,static-base-uri())"/>
    </px:normalize-uri>
    <px:normalize-uri name="temp-dir">
        <p:with-option name="href" select="resolve-uri($temp-dir,static-base-uri())"/>
    </px:normalize-uri>
    <px:normalize-uri name="output-dir">
        <p:with-option name="href" select="resolve-uri($output-dir,static-base-uri())"/>
    </px:normalize-uri>
    <p:identity name="nordic-version-message-and-variables"/>
    <p:sink/>
    
    <px:fileset-create name="html-to-epub3.create-html-fileset">
        <p:with-option name="base" select="replace(/*/text(),'[^/]+$','')">
            <p:pipe port="normalized" step="html"/>
        </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/xhtml+xml" name="html-to-epub3.add-html-to-fileset">
        <p:with-option name="href" select="replace(/*/text(),'.*/','')">
            <p:pipe port="normalized" step="html"/>
        </p:with-option>
    </px:fileset-add-entry>
    <p:identity name="html-to-epub3.html-fileset.no-resources"/>

    <px:check-files-wellformed name="html-to-epub3.check-html-wellformed"/>

    <p:choose name="html-to-epub3.html-load">
        <p:xpath-context>
            <p:pipe port="validation-status" step="html-to-epub3.check-html-wellformed"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' or $fail-on-error = 'false'">
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="html-to-epub3.html-load.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="html-to-epub3.html-load.load"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>

            <p:load name="html-to-epub3.html-load.load">
                <p:with-option name="href" select="/*/text()">
                    <p:pipe port="normalized" step="html"/>
                </p:with-option>
            </p:load>

            <px:html-to-fileset name="html-to-epub3.html-load.resource-fileset"/>
            <px:fileset-join name="html-to-epub3.html-load.fileset">
                <p:input port="source">
                    <p:pipe port="result" step="html-to-epub3.html-fileset.no-resources"/>
                    <p:pipe port="fileset.out" step="html-to-epub3.html-load.resource-fileset"/>
                </p:input>
            </px:fileset-join>

        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" sequence="true" primary="true">
                <p:pipe port="result" step="html-to-epub3.html-fileset.no-resources"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:empty/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:pipe port="report" step="html-to-epub3.check-html-wellformed"/>
            </p:output>

            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>

    <px:nordic-html-validate.step name="html-to-epub3.html-validate" check-images="true">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
        <p:input port="in-memory.in">
            <p:pipe step="html-to-epub3.html-load" port="in-memory.out"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe step="html-to-epub3.html-load" port="report.out"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="validation-status" step="html-to-epub3.check-html-wellformed"/>
        </p:input>
    </px:nordic-html-validate.step>

    <px:nordic-html-to-epub3.step name="html-to-epub3.html-to-epub3">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="temp-dir" select="concat(/*/text(),'html/')">
            <p:pipe step="temp-dir" port="normalized"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="html-to-epub3.html-validate"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="html-to-epub3.html-validate"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="html-to-epub3.html-validate"/>
        </p:input>
    </px:nordic-html-to-epub3.step>

    <px:nordic-epub3-store.step name="html-to-epub3.epub3-store">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="output-dir" select="/*/text()">
            <p:pipe step="output-dir" port="normalized"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="html-to-epub3.html-to-epub3"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="html-to-epub3.html-to-epub3"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="html-to-epub3.html-to-epub3"/>
        </p:input>
    </px:nordic-epub3-store.step>

    <px:nordic-epub3-validate.step name="html-to-epub3.epub3-validate" check-images="false">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="temp-dir" select="concat(/*/text(),'validate-epub/')">
            <p:pipe step="temp-dir" port="normalized"/>
        </p:with-option>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="html-to-epub3.epub3-store"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="html-to-epub3.epub3-store"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="html-to-epub3.epub3-store"/>
        </p:input>
    </px:nordic-epub3-validate.step>
    <p:sink/>

    <px:nordic-format-html-report name="html-to-epub3.nordic-format-html-report">
        <p:input port="source">
            <p:pipe port="report.out" step="html-to-epub3.epub3-validate"/>
        </p:input>
    </px:nordic-format-html-report>
    <p:store include-content-type="false" method="xhtml" omit-xml-declaration="false" name="html-to-epub3.store-report">
        <p:with-option name="href" select="concat(/*/text(),if (ends-with(/*/text(),'/')) then '' else '/','report.xhtml')">
            <p:pipe port="normalized" step="html-report"/>
        </p:with-option>
    </p:store>
    <px:set-doctype doctype="&lt;!DOCTYPE html&gt;" name="html-to-epub3.set-report-doctype">
        <p:with-option name="href" select="/*/text()">
            <p:pipe port="result" step="html-to-epub3.store-report"/>
        </p:with-option>
    </px:set-doctype>
    <p:sink/>
    
    <px:nordic-fail-on-error-status name="status">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="output-dir" select="/*/text()">
            <p:pipe step="output-dir" port="normalized"/>
        </p:with-option>
        <p:input port="source">
            <p:pipe port="status.out" step="html-to-epub3.epub3-validate"/>
        </p:input>
    </px:nordic-fail-on-error-status>
    <p:sink/>

</p:declare-step>
