<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:script" name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Example script</h1>
        <p px:role="desc">Does stuff.</p>
    </p:documentation>
    
    <p:input port="source"/>
    <p:option name="option-1"/>
    <p:output port="result"/>
    
    <p:input port="foobar-params" kind="parameter" px:options="foo bar"/>
    
    <p:import href="foo.xpl"/>
    
    <px:foo>
        <p:input port="params">
            <p:pipe step="main" port="foobar-params"/>
        </p:input>
    </px:foo>
    
</p:declare-step>
