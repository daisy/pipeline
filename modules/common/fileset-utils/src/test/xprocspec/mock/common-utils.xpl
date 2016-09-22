<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:err="http://www.w3.org/ns/xproc-error">

    <p:declare-step type="px:error">
        <p:output port="result" sequence="true"/>
        <p:option name="code" required="true"/>
        <p:option name="message"/>

        <p:error>
            <p:with-option name="code" select="$code"/>
        </p:error>
    </p:declare-step>

    <p:declare-step type="px:message">
        <p:input port="source" primary="true" sequence="true">
            <p:empty/>
        </p:input>
        <p:output port="result" sequence="true"/>
        <p:option name="severity"/>
        <p:option name="message"/>

        <p:identity/>
    </p:declare-step>

</p:library>
