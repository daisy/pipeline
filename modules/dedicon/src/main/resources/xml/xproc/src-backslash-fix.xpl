<?xml version="1.0" encoding="UTF-8"?>
<!--
    Workaround for issue https://github.com/daisy/pipeline-mod-braille/issues/162
    The application doesn't handle backslashes well so we convert them to forward slashes
-->
<p:declare-step type="dedicon:src-backslash-fix" version="1.0"
                 xmlns:dedicon="http://www.dedicon.nl"
                 xmlns:p="http://www.w3.org/ns/xproc"
                 xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                 exclude-inline-prefixes="#all"
                 name="main">
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <px:message message="Applying fix for backslashes in attribute src"/>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/src-backslash-fix.xsl"/>
        </p:input>
    </p:xslt>
    <px:message message="Finished applying fix for backslashes in attribute src" severity="DEBUG"/>
    
</p:declare-step>
