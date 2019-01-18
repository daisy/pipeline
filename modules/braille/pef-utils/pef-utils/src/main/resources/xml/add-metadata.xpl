<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pef:add-metadata" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:opf="http://www.idpf.org/2007/opf"
                exclude-inline-prefixes="p px xsl"
                name="main">
    
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
