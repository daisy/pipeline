<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:option name="epub" required="true"/>
    <p:input port="opf" primary="false"/>
    <p:input port="obfl" primary="false">
        <p:empty/>
    </p:input>
    <p:input port="pef" primary="true">
        <p:empty/>
    </p:input>
    
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="brf-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    <p:option name="obfl-output-dir" select="''"/>
    
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-brf" select="'false'"/>
    <p:option name="ascii-file-format" select="''"/>
    <p:option name="ascii-table" select="''"/>
    
    <p:import href="xml-to-pef.store.xpl">
        <p:documentation>
            px:xml-to-pef.store
        </p:documentation>
    </p:import>
    
    <px:xml-to-pef.store>
        <p:input port="obfl">
            <p:pipe step="main" port="obfl"/>
        </p:input>
        <p:with-option name="name" select="if (ends-with(lower-case($epub),'.epub')) then replace($epub,'^.*/([^/]*)\.[^/\.]*$','$1')
                                           else (/opf:package/opf:metadata/dc:identifier[not(@refines)], 'unknown-identifier')[1]">
            <p:pipe step="main" port="opf"/>
        </p:with-option>
        <p:with-option name="include-brf" select="$include-brf"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="ascii-file-format" select="$ascii-file-format"/>
        <p:with-option name="ascii-table" select="$ascii-table"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="brf-output-dir" select="$brf-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="obfl-output-dir" select="$obfl-output-dir"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
