<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:ex="http://example.net/ns" type="ex:throw-error" version="1.0">
  <p:input port="source"/>
  <p:output port="result"/>
  <p:error code="foo">
    <p:input port="source">
      <p:inline><message>foobar</message></p:inline>
    </p:input>
  </p:error>
</p:declare-step>
