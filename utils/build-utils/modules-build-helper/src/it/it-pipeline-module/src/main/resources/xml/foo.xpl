<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:foo" version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>p:foo</p>
    </p:documentation>
    
    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input port "source"</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output port "result"</p>
        </p:documentation>
    </p:output>
    
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Unused import</p>
        </p:documentation>
    </p:import>
    
    <p:declare-step type="px:java-step">
        <p:input port="source"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="foo.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <px:java-step/>
    
</p:declare-step>
