<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:log-error">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Log a caught XProc error.</p>
        <p>Logs a message in the job execution log and prints the stack trace in the detailed
        log.</p>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Any document sequence.</p>
        </p:documentation>
    </p:input>
    <p:input port="error" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>c:errors</code> document.</p>
        </p:documentation>
    </p:input>
    <p:option name="severity" select="'ERROR'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Severity of the log message</p>
        </p:documentation>
    </p:option>
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Copy of source.</p>
        </p:documentation>
    </p:output>

    <!--
        Implemented in ../../../java/org/daisy/pipeline/common/calabash/impl/LogError.java
    -->

</p:declare-step>
