<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           version="1.0">
    
    <p:import href="validate-braille.xpl"/>
    <p:import href="mark-transitions.xpl"/>
    <p:import href="select-by-base.xpl"/>
    <p:import href="select-by-position.xpl"/>
    <p:import href="xslt-for-each.xpl"/>
    
    <p:declare-step type="px:transform">
        <p:input port="source"/>
        <p:input port="parameters" kind="parameter" primary="false"/>
        <p:output port="result"/>
        <p:option name="query" required="true"/>
        <p:option name="temp-dir" required="false"/>
    </p:declare-step>
    
</p:library>
