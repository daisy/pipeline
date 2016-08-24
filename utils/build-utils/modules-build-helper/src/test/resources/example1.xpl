<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:xd="http://www.daisy.org/ns/pipeline/doc" version="1.0">

    <p:documentation xd:target="parent">
        <xd:short>example1</xd:short>
        <xd:detail>Example 1</xd:detail>
    </p:documentation>

    <p:input port="source">
        <p:inline>
            <doc>Hello world!</doc>
        </p:inline>
    </p:input>
    <p:output port="result"/>
    <p:identity/>
    
</p:declare-step>
