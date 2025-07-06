<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                type="px:foo" name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>p:foo</p>
    </p:documentation>
    
    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input port "source"</p>
        </p:documentation>
    </p:input>
    
    <p:input port="params" kind="parameter"/>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output port "result"</p>
        </p:documentation>
    </p:output>
    
    <p:declare-step type="px:java-step">
        <p:input port="source"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="foo.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:pipe step="main" port="params"/>
        </p:input>
    </p:xslt>
    
    <px:java-step/>
    
    <p:rename match="/*">
        <p:with-option name="new-name" select="pf:xslt-function(local-name(/*))"/>
    </p:rename>
    
</p:declare-step>
