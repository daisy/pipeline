<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:deep-parse-page-and-volume-stylesheets"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:input port="source" sequence="true"/>
    <p:output port="result"/>
    
    <p:xslt template-name="main">
        <p:input port="stylesheet">
            <p:document href="deep-parse-page-and-volume-stylesheets.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
