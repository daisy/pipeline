<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="dedicon:pre-processing" version="1.0"
                 xmlns:dedicon="http://www.dedicon.nl"
                 xmlns:p="http://www.w3.org/ns/xproc"
                 xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                 xmlns:d="http://www.daisy.org/ns/pipeline/data"
                 xmlns:c="http://www.w3.org/ns/xproc-step"
                 xmlns:pef="http://www.daisy.org/ns/2008/pef"
                 exclude-inline-prefixes="#all"
                 name="main">
    
    <p:input port="source"/>
    <p:input port="parameters" kind="parameter"/>
    <p:output port="result"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <px:message message="Running Dedicon-specific pre-processing steps"/>
    <p:xslt>
        <p:input port="parameters">
            <p:pipe port="parameters" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/generate-boilerplate.xsl"/>
        </p:input>
    </p:xslt>
    <px:message message="Finished running Dedicon-specific pre-processing steps" severity="DEBUG"/>
    
</p:declare-step>
