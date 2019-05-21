<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-dtbook-validate" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:pxp="http://exproc.org/proposed/steps" xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Nordic DTBook Validator</h1>
        <p px:role="desc">Validates an dtbook publication according to the nordic markup guidelines.</p>
        <a px:role="homepage" href="http://nlbdev.github.io/nordic-epub3-dtbook-migrator/">https://github.com/josteinaj/nordic-epub3-dtbook-migrator</a>
        <div px:role="author maintainer">
            <p px:role="name">Jostein Austvik Jacobsen</p>
            <a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a>
            <p px:role="organization">NLB - Norwegian library of talking books and braille</p>
        </div>
    </p:documentation>

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Nordic DTBook Validator</h1>
        <p px:role="desc">Validates an dtbook publication according to the nordic markup guidelines.</p>
    </p:documentation>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc">Validation status (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe port="status.out" step="dtbook-validate.dtbook-validate"/>
    </p:output>

    <p:option name="dtbook" required="true" px:type="anyFileURI" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">DTBook marked up according to the nordic markup guidelines.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="organization-specific-validation" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Organization-specific validation</h2>
            <p px:role="desc">Leave blank for the default validation schemas. Use 'nota' to validate using Nota-specific validation rules.</p>
        </p:documentation>
    </p:option>

    <p:option name="check-images" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validate images</h2>
            <p px:role="desc">Whether or not to check that referenced images exist and has the right file signatures.</p>
        </p:documentation>
    </p:option>

    <p:option name="no-legacy" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Disallow legacy markup</h2>
            <p px:role="desc">If set to false, will upgrade DTBook versions earlier than 2005-3 to 2005-3, and fix some non-standard practices that appear in older DTBooks.</p>
        </p:documentation>
    </p:option>

    <p:option name="html-report" required="true" px:output="result" px:type="anyDirURI" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML Report</h1>
            <p px:role="desc">An HTML-formatted version of the validation report.</p>
        </p:documentation>
    </p:option>

    <p:import href="step/dtbook-validate.step.xpl"/>
    <p:import href="step/format-html-report.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

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
    <p:identity name="dtbook-validate.nordic-version-message-and-variables"/>
    <p:sink/>

    <px:fileset-create name="dtbook-validate.create-dtbook-fileset">
        <p:with-option name="base" select="replace(/*/text(),'[^/]+$','')">
            <p:pipe port="normalized" step="dtbook"/>
        </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/x-dtbook+xml" name="dtbook-validate.add-dtbook-to-fileset">
        <p:with-option name="href" select="replace(/*/text(),'.*/','')">
            <p:pipe port="normalized" step="dtbook"/>
        </p:with-option>
    </px:fileset-add-entry>
    <px:nordic-dtbook-validate.step name="dtbook-validate.dtbook-validate" cx:depends-on="dtbook-validate.nordic-version-message-and-variables" fail-on-error="true">
        <p:with-option name="check-images" select="$check-images"/>
        <p:with-option name="allow-legacy" select="if ($no-legacy='false') then 'true' else 'false'"/>
        <p:with-option name="organization-specific-validation" select="$organization-specific-validation"/>
    </px:nordic-dtbook-validate.step>
    <p:sink/>

    <px:fileset-load media-types="application/x-dtbook+xml" method="xml" name="dtbook-validate.load-dtbook">
        <p:input port="fileset">
            <p:pipe port="fileset.out" step="dtbook-validate.dtbook-validate"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.out" step="dtbook-validate.dtbook-validate"/>
        </p:input>
    </px:fileset-load>
    <p:xslt name="dtbook-validate.info-report">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/info-report.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="dtbook-validate.report.nordic"/>
    <p:sink/>

    <px:nordic-format-html-report name="dtbook-validate.nordic-format-html-report">
        <p:input port="source">
            <p:pipe port="result" step="dtbook-validate.report.nordic"/>
            <p:pipe port="report.out" step="dtbook-validate.dtbook-validate"/>
        </p:input>
    </px:nordic-format-html-report>
    <p:store include-content-type="false" method="xhtml" omit-xml-declaration="false" name="dtbook-validate.store-report">
        <p:with-option name="href" select="concat(/*/text(),if (ends-with(/*/text(),'/')) then '' else '/','report.xhtml')">
            <p:pipe port="normalized" step="html-report"/>
        </p:with-option>
    </p:store>
    <px:set-doctype doctype="&lt;!DOCTYPE html&gt;" name="dtbook-validate.set-report-doctype">
        <p:with-option name="href" select="/*/text()">
            <p:pipe port="result" step="dtbook-validate.store-report"/>
        </p:with-option>
    </px:set-doctype>
    <p:sink/>

</p:declare-step>
