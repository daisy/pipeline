<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:html="http://www.w3.org/1999/xhtml" type="px:daisy202-validator" version="1.0"
    px:input-filesets="daisy202"
    xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 2.02 Validator</h1>
        <p px:role="desc">Validates a DAISY 2.02 fileset.</p>
        <address px:role="author maintainer">
            <p>Maintained by <span px:role="name">Jostein Austvik Jacobsen</span>
                (organization: <span px:role="organization">NLB</span>,
                e-mail: <a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a>).</p>
        </address>
        <p><a px:role="homepage" href="http://daisy.github.io/pipeline/modules/org/daisy/pipeline/modules/daisy202-validator/doc/daisy202-validator.html">Online Documentation</a></p>
    </p:documentation>

    <p:option name="ncc" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input NCC</h2>
            <p px:role="desc">The NCC file in the DAISY 2.02 fileset</p>
        </p:documentation>
    </p:option>

    <p:option name="timeToleranceMs" select="500" px:type="xs:integer">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Time Tolerance</h2>
            <p px:role="desc">The duration of the audio files can deviate from the duration declared in the DAISY 2.02 fileset by this many milliseconds (default: 500).</p>
        </p:documentation>
    </p:option>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML Report</h1>
            <p px:role="desc">An HTML-formatted version of the validation report.</p>
        </p:documentation>
        <p:pipe port="result" step="html-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc">Validation status (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe port="result" step="validation-status"/>
    </p:output>

    <p:import href="steps/validate.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

    <px:daisy202-load name="load">
        <p:with-option name="ncc" select="$ncc"/>
    </px:daisy202-load>

    <px:daisy202-validator.validate name="validate">
        <p:with-option name="timeToleranceMs" select="$timeToleranceMs"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
    </px:daisy202-validator.validate>
    <p:sink/>

    <p:identity>
        <p:input port="source">
            <p:pipe port="report.out" step="validate"/>
        </p:input>
    </p:identity>
    <px:combine-validation-reports>
        <p:with-option name="document-name" select="replace($ncc,'.*/','')">
            <p:empty/>
        </p:with-option>
        <p:with-option name="document-type" select="'DAISY 2.02'">
            <p:empty/>
        </p:with-option>
        <p:with-option name="document-path" select="$ncc">
            <p:empty/>
        </p:with-option>
    </px:combine-validation-reports>
    <p:identity name="xml-report"/>
    <px:validation-report-to-html>
        <p:with-option name="toc" select="'false'"/>
    </px:validation-report-to-html>
    <p:identity name="html-report"/>
    <p:sink/>

    <p:group name="validation-status">
        <p:output port="result"/>
        <p:for-each>
            <p:iteration-source select="/d:document-validation-report/d:document-info/d:error-count">
                <p:pipe port="result" step="xml-report"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:validation-status"/>
        <p:add-attribute attribute-name="result" match="/*">
            <p:with-option name="attribute-value" select="if (sum(/*/*/number(.))&gt;0) then 'error' else 'ok'"/>
        </p:add-attribute>
        <p:delete match="/*/node()"/>
    </p:group>
    <p:sink/>

</p:declare-step>
