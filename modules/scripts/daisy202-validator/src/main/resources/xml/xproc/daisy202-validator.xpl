<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:daisy202-validator.script"
                px:input-filesets="daisy202">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 2.02 Validator</h1>
        <p px:role="desc">Validates a DAISY 2.02 fileset.</p>
        <address px:role="author maintainer">
            <p>Maintained by <span px:role="name">Jostein Austvik Jacobsen</span>
                (organization: <span px:role="organization">NLB</span>,
                e-mail: <a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a>).</p>
        </address>
        <p><a px:role="homepage" href="http://daisy.github.io/pipeline/modules/daisy202-validator">Online Documentation</a></p>
    </p:documentation>

    <p:option name="ncc" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">NCC</h2>
            <p px:role="desc">The NCC file in the DAISY 2.02 fileset</p>
        </p:documentation>
    </p:option>

    <p:option name="timeToleranceMs" select="500" px:type="xs:integer">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Time Tolerance</h2>
            <p px:role="desc">The duration of the audio files can deviate from the duration declared in the DAISY 2.02 fileset by this many milliseconds.</p>
        </p:documentation>
    </p:option>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation report</h1>
        </p:documentation>
        <p:pipe step="validate" port="html-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc" xml:space="preserve">The validation status

[More details on the file format](http://daisy.github.io/pipeline/StatusXML).</p>
        </p:documentation>
        <p:pipe step="validate" port="validation-status"/>
    </p:output>

    <p:import href="steps/validate.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl"/>

    <px:daisy202-load name="load">
        <p:with-option name="ncc" select="$ncc"/>
    </px:daisy202-load>

    <px:daisy202-validator name="validate">
        <p:with-option name="timeToleranceMs" select="$timeToleranceMs"/>
        <p:with-option name="ncc" select="$ncc"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
    </px:daisy202-validator>
    <p:sink/>

</p:declare-step>
