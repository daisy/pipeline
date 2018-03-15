<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    exclude-inline-prefixes="px"
    type="px:pef-to-html.convert" name="pef-to-html" version="1.0">

    <p:input port="source" primary="true" px:media-type="application/x-pef+xml"/>
    <p:output port="result" primary="true" px:media-type="text/html"/>
    
    <p:option name="table" required="true"/>

    <!-- Pad pages with whitespace -->
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="pef-padding.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <!-- Generate HTML preview -->
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="pef-preview.xsl"/>
        </p:input>
        <p:with-param name="table" select="$table"/>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
