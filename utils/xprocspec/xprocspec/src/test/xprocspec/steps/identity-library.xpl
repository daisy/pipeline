<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc">
    
    <p:declare-step xmlns:ex="http://example.net/ns" type="ex:identity-1">
        <p:input port="source">
            <p:inline>
                <doc>Hello world! #1</doc>
            </p:inline>
        </p:input>
        <p:output port="result"/>
        <p:identity/>
    </p:declare-step>
    
    <p:declare-step xmlns:ex="http://example.net/ns" type="ex:identity-2">
        <p:input port="source">
            <p:inline>
                <doc>Hello world! #2</doc>
            </p:inline>
        </p:input>
        <p:output port="result"/>
        <p:identity/>
    </p:declare-step>
    
    <p:declare-step xmlns:ex="http://example.net/ns" type="ex:identity-3">
        <p:input port="source">
            <p:inline>
                <doc>Hello world! #3</doc>
            </p:inline>
        </p:input>
        <p:output port="result"/>
        <p:identity/>
    </p:declare-step>
    
</p:library>
