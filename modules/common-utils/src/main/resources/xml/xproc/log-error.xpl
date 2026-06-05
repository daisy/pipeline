<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="px:log-error">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Log caught XProc errors.</p>
        <p>Logs messages in the job execution log and prints the stack trace in the detailed
        log.</p>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Any document sequence.</p>
        </p:documentation>
    </p:input>
    <p:input port="error" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Zero or more <a
            href="https://www.w3.org/TR/xproc/#cv.errors"><code>c:errors</code></a> documents.</p>
        </p:documentation>
    </p:input>
    <p:option name="severity" select="'ERROR'" cx:type="ERROR|WARN|INFO|DEBUG|TRACE">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Severity of the log messages</p>
        </p:documentation>
    </p:option>
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Copy of <code>source</code>.</p>
        </p:documentation>
    </p:output>

    <!--
        Implemented in ../../../java/org/daisy/pipeline/common/calabash/impl/LogError.java
    -->

</p:declare-step>
