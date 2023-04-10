<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pef:validate">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Validate a PEF document.</p>
    </p:documentation>
    
    <p:input port="source" sequence="false" primary="true"/>
    <p:output port="result" sequence="false" primary="true"/>
    <p:output port="validation-status" sequence="false"/>
    <p:option name="assert-valid" required="false"/>
    <p:option name="temp-dir" required="true" cx:type="xs:anyURI" cx:as="xs:string"/>
    
    <!--
        Implemented in ../../org/daisy/pipeline/braille/pef/calabash/impl/ValidateStep.java
    -->
    
</p:declare-step>
