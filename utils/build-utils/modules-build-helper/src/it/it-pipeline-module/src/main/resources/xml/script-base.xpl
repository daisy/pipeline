<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:foo="foo"
                xmlns:bar="bar">

    <p:input port="source">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Source document</h1>
      </p:documentation>
    </p:input>

    <p:option name="option-1"/>
    <p:option name="foo:option-1"/>
    <p:option name="bar:option-1"/>

    <p:output port="result">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Result document</h1>
      </p:documentation>
    </p:output>

</p:declare-step>
