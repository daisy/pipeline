<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-dtbook-to-epub3" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:l="http://xproc.org/library" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Nordic DTBook to EPUB3</h1>
        <p px:role="desc">Transforms a DTBook document into an EPUB3 publication according to the nordic markup guidelines.</p>
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

    <p:option name="dtbook" required="true" px:type="anyFileURI" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
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

    <p:option name="discard-intermediary-html" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Discard intermediary HTML</h2>
            <p px:role="desc">Whether or not to include the intermediary HTML in the output (does not include external resources such as images). Set to false to include the HTML.</p>
        </p:documentation>
    </p:option>

    <p:option name="no-legacy" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Disallow legacy markup</h2>
            <p px:role="desc">If set to false, will upgrade DTBook versions earlier than 2005-3 to 2005-3, and fix some non-standard practices that appear in older DTBooks.</p>
        </p:documentation>
    </p:option>

    <p:option name="fail-on-error" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Stop processing on validation error</h2>
            <p px:role="desc">Whether or not to stop the conversion when a validation error occurs. Setting this to false may be useful for debugging or if the validation error is a minor one. The
                output is not guaranteed to be valid if this option is set to false.</p>
        </p:documentation>
    </p:option>

    <p:import href="step/dtbook-validate.step.xpl"/>
    <p:import href="step/dtbook-to-html.step.xpl"/>
    <p:import href="step/html-validate.step.xpl"/>
    <p:import href="step/html-store.step.xpl"/>
    <p:import href="step/html-to-epub3.step.xpl"/>
    <p:import href="step/epub3-store.step.xpl"/>
    <p:import href="step/epub3-validate.step.xpl"/>
    <p:import href="step/format-html-report.xpl"/>
    <p:import href="step/fail-on-error-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:variable name="dtbook-href" select="resolve-uri($dtbook,static-base-uri())"/>

    <px:message message="$1">
        <p:with-option name="param1" select="/*">
            <p:document href="../version-description.xml"/>
        </p:with-option>
    </px:message>
    
    <px:normalize-uri name="dtbook">
        <p:with-option name="href" select="resolve-uri($dtbook,static-base-uri())"/>
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

    <px:fileset-create name="dtbook-to-epub3.create-dtbook-fileset">
        <p:with-option name="base" select="replace(/*/text(),'[^/]+$','')">
            <p:pipe port="normalized" step="dtbook"/>
        </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entry name="dtbook-to-epub3.add-dtbook-to-fileset" media-type="application/x-dtbook+xml">
        <p:with-option name="href" select="replace(/*/text(),'.*/','')">
            <p:pipe port="normalized" step="dtbook"/>
        </p:with-option>
    </px:fileset-add-entry>
    <px:nordic-dtbook-validate.step name="dtbook-to-epub3.dtbook-validate" check-images="true" cx:depends-on="nordic-version-message-and-variables">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="allow-legacy" select="if ($no-legacy='false') then 'true' else 'false'"/>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
    </px:nordic-dtbook-validate.step>

    <px:nordic-dtbook-to-html.step name="dtbook-to-epub3.dtbook-to-html">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="temp-dir" select="concat(/*/text(),'html/')">
            <p:pipe port="normalized" step="temp-dir"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="dtbook-to-epub3.dtbook-validate"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="dtbook-to-epub3.dtbook-validate"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="dtbook-to-epub3.dtbook-validate"/>
        </p:input>
    </px:nordic-dtbook-to-html.step>

    <px:nordic-html-validate.step name="dtbook-to-epub3.html-validate" check-images="false">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="dtbook-to-epub3.dtbook-to-html"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="dtbook-to-epub3.dtbook-to-html"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="dtbook-to-epub3.dtbook-to-html"/>
        </p:input>
    </px:nordic-html-validate.step>

    <p:choose name="dtbook-to-epub3.choose-discard-intermediary">
        <p:xpath-context>
            <p:pipe port="status.out" step="dtbook-to-epub3.html-validate"/>
        </p:xpath-context>
        <p:when test="$discard-intermediary-html='false' or (/*/@result='error' and $fail-on-error='true')">
            <p:variable name="subdir-name" select="substring-before(replace(/*/d:file[@media-type='application/xhtml+xml'][1]/@href,'^.*/',''),'.')"/>
            <px:message message="Storing intermediary HTML$1">
                <p:with-option name="param1" select="if ($discard-intermediary-html) then '' else ' (contains errors)'"/>
            </px:message>
            <px:fileset-move name="dtbook-to-epub3.choose-discard-intermediary.html-move">
                <p:with-option name="new-base" select="concat(if (ends-with(/*/text(),'/')) then /*/text() else concat(/*/text(),'/'), $subdir-name, '/')">
                    <p:pipe port="normalized" step="output-dir"/>
                </p:with-option>
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.out" step="dtbook-to-epub3.html-validate"/>
                </p:input>
            </px:fileset-move>
            <px:nordic-html-store.step name="dtbook-to-epub3.choose-discard-intermediary.nordic-html-store">
                <p:with-option name="fail-on-error" select="$fail-on-error"/>
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.out" step="dtbook-to-epub3.choose-discard-intermediary.html-move"/>
                </p:input>
            </px:nordic-html-store.step>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <px:nordic-html-to-epub3.step name="dtbook-to-epub3.html-to-epub3">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="dtbook-to-epub3.html-validate"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="dtbook-to-epub3.html-validate"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="dtbook-to-epub3.html-validate"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat(/*/text(),'epub/')">
            <p:pipe port="normalized" step="temp-dir"/>
        </p:with-option>
        <p:with-option name="compatibility-mode" select="'true'"/>
    </px:nordic-html-to-epub3.step>

    <px:nordic-epub3-store.step name="dtbook-to-epub3.epub3-store">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="dtbook-to-epub3.html-to-epub3"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="dtbook-to-epub3.html-to-epub3"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="dtbook-to-epub3.html-to-epub3"/>
        </p:input>
        <p:with-option name="output-dir" select="/*/text()">
            <p:pipe port="normalized" step="output-dir"/>
        </p:with-option>
    </px:nordic-epub3-store.step>

    <px:nordic-epub3-validate.step name="dtbook-to-epub3.epub3-validate" check-images="false">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="dtbook-to-epub3.epub3-store"/>
        </p:input>
        <p:input port="report.in">
            <p:pipe port="report.out" step="dtbook-to-epub3.epub3-store"/>
        </p:input>
        <p:input port="status.in">
            <p:pipe port="status.out" step="dtbook-to-epub3.epub3-store"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat(/*/text(),'validate-epub/')">
            <p:pipe port="normalized" step="temp-dir"/>
        </p:with-option>
    </px:nordic-epub3-validate.step>
    <p:sink/>

    <px:nordic-format-html-report name="dtbook-to-epub3.nordic-format-html-report">
        <p:input port="source">
            <p:pipe port="report.out" step="dtbook-to-epub3.epub3-validate"/>
        </p:input>
    </px:nordic-format-html-report>
    <p:store include-content-type="false" method="xhtml" omit-xml-declaration="false" name="dtbook-to-epub3.store-report">
        <p:with-option name="href" select="concat(/*/text(),if (ends-with(/*/text(),'/')) then '' else '/','report.xhtml')">
            <p:pipe port="normalized" step="html-report"/>
        </p:with-option>
    </p:store>
    <px:set-doctype doctype="&lt;!DOCTYPE html&gt;" name="dtbook-to-epub3.set-report-doctype">
        <p:with-option name="href" select="/*/text()">
            <p:pipe port="result" step="dtbook-to-epub3.store-report"/>
        </p:with-option>
    </px:set-doctype>
    <p:sink/>
    
    <px:nordic-fail-on-error-status name="status">
        <p:with-option name="fail-on-error" select="$fail-on-error"/>
        <p:with-option name="output-dir" select="/*/text()">
            <p:pipe port="normalized" step="output-dir"/>
        </p:with-option>
        <p:input port="source">
            <p:pipe port="status.out" step="dtbook-to-epub3.epub3-validate"/>
        </p:input>
    </px:nordic-fail-on-error-status>
    <p:sink/>

</p:declare-step>
