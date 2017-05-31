<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:ex="http://example.net/ns" type="ex:identity-with-foreign-namespaces" version="1.0" xmlns:px="http://example.net/foreign" px:attribute="foo">
    
    <p:input port="source" xmlns:px="http://example.net/foreign" px:attribute="foo"/>
    
    <p:output port="result" xmlns:px="http://example.net/foreign" px:attribute="foo"/>
    
    <p:option name="option" xmlns:px="http://example.net/foreign" px:attribute="foo"/>
    
    <p:identity/>
    
</p:declare-step>
