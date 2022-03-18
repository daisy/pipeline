<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:html-to-pef.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="html" primary="false"/>
    <p:input port="obfl" primary="false">
        <p:empty/>
    </p:input>
    <p:input port="pef" primary="true">
        <p:empty/>
    </p:input>
    
    <p:option name="output-dir" select="''"/>
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    <p:option name="obfl-output-dir" select="''"/>
    
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-pef" select="'false'"/>
    <p:option name="output-file-format" select="''"/>
    <p:option name="preview-table" select="''"/>
    
    <p:import href="xml-to-pef.store.xpl">
        <p:documentation>
            px:xml-to-pef.store
        </p:documentation>
    </p:import>
    
    <px:xml-to-pef.store>
        <p:input port="obfl">
            <p:pipe step="main" port="obfl"/>
        </p:input>
        <p:with-option name="name" select="replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1')">
            <p:pipe step="main" port="html"/>
        </p:with-option>
        <p:with-option name="include-pef" select="$include-pef"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="output-file-format" select="$output-file-format"/>
        <p:with-option name="preview-table" select="$preview-table"/>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="obfl-output-dir" select="$obfl-output-dir"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
