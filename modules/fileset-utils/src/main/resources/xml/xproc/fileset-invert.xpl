<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:fileset-invert">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Return the inverse of a mapping document.</p>
    </p:documentation>

    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input mapping document.</p>
            <p>A mapping document is a <code>d:fileset</code> document that maps files from
            <code>@original-href</code> to <code>@href</code> and anchors from
            <code>@original-id</code> to <code>@id</code>.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The inverse mapping document.</p>
            <p>Applying <code>px:fileset-invert</code> twice yields the input document again.</p>
        </p:documentation>
    </p:output>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="../xslt/fileset-invert.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>
