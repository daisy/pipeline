<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           version="1.0">
    
    <p:import href="validate-braille.xpl"/>
    <p:import href="mark-transitions.xpl"/>
    <p:import href="parse-xml-stylesheet-instructions.xpl"/>
    <p:import href="select-by-base.xpl"/>
    <p:import href="select-by-position.xpl"/>
    <p:import href="xslt-for-each.xpl"/>
    <p:import href="add-parameters.xpl"/>
    <p:import href="merge-parameters.xpl"/>
    <p:import href="delete-parameters.xpl"/>
    
    <p:declare-step type="px:transform">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Query an XML transformer of a certain type and apply it to a node.</p>
        </p:documentation>
        <p:input port="source"/>
        <p:input port="parameters" kind="parameter" primary="false"/>
        <p:output port="result"/>
        <p:option name="query" required="true"/>
        <!--
            implemented in Java (org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep)
        -->
    </p:declare-step>
    
</p:library>
