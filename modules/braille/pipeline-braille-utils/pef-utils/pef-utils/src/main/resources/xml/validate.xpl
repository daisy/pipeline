<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pef:validate"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pef="http://www.daisy.org/ns/2008/pef"
    version="1.0">
    
    <p:input port="source" sequence="false" primary="true"/>
    <p:output port="result" sequence="false" primary="true"/>
    <p:output port="validation-status" sequence="false"/>
    <p:option name="assert-valid" required="false"/>
    <p:option name="temp-dir" required="true" px:type="anyDirURI"/>
    
</p:declare-step>
