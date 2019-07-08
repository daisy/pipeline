<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  xmlns:mt="http://transpect.io/calabash-extensions/mathtype-extension/"
  version="1.0">

  <p:output port="result" primary="true" sequence="true">
    <p:documentation>formula</p:documentation>
  </p:output>

  <p:option name="file" required="true"/>

  <p:import href="mathtype2mml-declaration.xpl"/>

  <tr:mathtype2mml name="mathtype2mml">
    <p:with-option name="href" select="$file"/>
  </tr:mathtype2mml>

</p:declare-step>
