<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pef:text2pef">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Convert an ASCII-based braille format into PEF.</p>
    </p:documentation>
    
    <p:input port="source" sequence="false" primary="true"/>
    <p:output port="result" sequence="false" primary="true"/>
    <p:option name="table" required="true"/>
    <p:option name="temp-dir" required="true" cx:type="xs:anyURI" cx:as="xs:string"/>
    <p:option name="title" required="false"/>
    <p:option name="creator" required="false"/>
    <p:option name="duplex" required="false"/>
    
    <!--
        Implemented in ../../org/daisy/pipeline/braille/pef/calabash/impl/Text2PEFStep.java
    -->
    
</p:declare-step>
