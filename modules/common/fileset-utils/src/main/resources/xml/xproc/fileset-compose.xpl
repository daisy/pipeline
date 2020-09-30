<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-inline-prefixes="#all"
                type="px:fileset-compose">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Return composition of mapping documents.</p>
    </p:documentation>

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input mapping documents (one or more).</p>
            <p>A mapping document is a <code>d:fileset</code> document that maps files from
            <code>@original-href</code> to <code>@href</code> and anchors from
            <code>@original-id</code> to <code>@id</code>.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The composed mapping document.</p>
            <p>The mappings are applied in the order in which they appear on the source port.</p>
        </p:documentation>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>

    <p:declare-step type="pxi:fileset-compose-recursively">
        <p:input port="source" sequence="true"/>
        <p:output port="result"/>
        <p:identity name="source"/>
        <p:count/>
        <p:choose>
            <p:when test="/*=1">
                <p:sink/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="source" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:split-sequence test="position()&lt;=2" name="first-two">
                    <p:input port="source">
                        <p:pipe step="source" port="result"/>
                    </p:input>
                </p:split-sequence>
                <p:xslt name="first-two-composed">
                    <p:input port="stylesheet">
                        <p:document href="../xslt/fileset-compose.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <p:sink/>
                <pxi:fileset-compose-recursively>
                    <p:input port="source">
                        <p:pipe step="first-two-composed" port="result"/>
                        <p:pipe step="first-two" port="not-matched"/>
                    </p:input>
                </pxi:fileset-compose-recursively>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

    <px:assert test-count-min="1" message="At least one input document expected" error-code="XXX"/>

    <pxi:fileset-compose-recursively/>

</p:declare-step>
