<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:ex="http://example.net/ns" type="ex:error" version="1.0">
    
    <p:output port="result"/>
    
    <p:error code="TESTERROR" code-prefix="ex" code-namespace="http://example.net/ns">
        <p:input port="source">
            <p:inline>
                <message>MESSAGE</message>
            </p:inline>
        </p:input>
    </p:error>
    
</p:declare-step>
