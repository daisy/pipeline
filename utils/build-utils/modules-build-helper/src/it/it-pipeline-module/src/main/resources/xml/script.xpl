<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:script" version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Example script</h1>
        <p px:role="desc">Does stuff.</p>
    </p:documentation>
    
    <p:input port="source"/>
    <p:option name="option-1"/>
    <p:option name="option-2"/>
    <p:output port="result"/>
    
    <p:import href="foo.xpl"/>
    
    <px:foo/>
    
</p:declare-step>
