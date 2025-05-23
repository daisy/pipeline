<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:pef-add-metadata"
                name="main">
    
    <p:documentation>
        <p>Add <a
        href="https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata">OPF
        metadata</a> to PEF document</p>
    </p:documentation>
    
    <p:input port="source" primary="true" px:media-type="application/x-pef+xml"/>
    <p:input port="metadata"/>
    <p:output port="result" px:media-type="application/x-pef+xml"/>
    
    <p:xslt px:progress="1">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
            <p:pipe step="main" port="metadata"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="add-opf-metadata-to-pef.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
