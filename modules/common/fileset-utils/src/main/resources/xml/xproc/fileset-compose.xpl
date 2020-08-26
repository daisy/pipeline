<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:fileset-compose">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Return composition of two mapping documents.</p>
    </p:documentation>

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input mapping documents.</p>
            <p>There must be exactly two of them.</p>
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

    <px:assert test-count-min="2" test-count-max="2" message="Exactly two input documents expected" error-code="XXX"/>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="../xslt/fileset-compose.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>
