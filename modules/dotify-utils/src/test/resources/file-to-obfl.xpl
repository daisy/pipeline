<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0"
           xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:dotify="http://code.google.com/p/dotify/">
    
    <p:declare-step type="dotify:file-to-obfl">
        <p:option name="source" required="true"/>
        <p:output port="result"/>
        <p:option name="locale" required="true"/>
        <p:option name="format" required="false" select="'obfl'"/>
        <p:input port="parameters" kind="parameter" primary="false"/>
        <p:option name="dotify-options" required="false"/>
        <p:option name="template" required="false" select="'default'"/>
        <p:option name="rows" required="false" select="29"/>
        <p:option name="cols" required="false" select="28"/>
        <p:option name="inner-margin" required="false" select="2"/>
        <p:option name="outer-margin" required="false" select="2"/>
        <p:option name="rowgap" required="false" select="0"/>
        <p:option name="splitterMax" required="false" select="50"/>
    </p:declare-step>
    
</p:library>
