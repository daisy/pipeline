<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="px"
                type="px:fileset-diff" name="main">

  <p:input port="source" primary="true"/>
  <p:input port="secondary"/>
  <p:output port="result"/>

  <p:import href="fileset-join.xpl"/>

  <px:fileset-join name="source">
    <p:documentation>Normalize @href</p:documentation>
  </px:fileset-join>
  <p:sink/>

  <px:fileset-join name="secondary">
    <p:documentation>Normalize @href</p:documentation>
    <p:input port="source">
      <p:pipe step="main" port="secondary"/>
    </p:input>
  </px:fileset-join>
  <p:sink/>

  <p:viewport match="d:file">
    <p:viewport-source>
      <p:pipe step="source" port="result"/>
    </p:viewport-source>
    <p:variable name="href" select="*/resolve-uri(@href,base-uri(.))"/>
    <p:choose>
      <p:xpath-context>
        <p:pipe step="secondary" port="result"/>
      </p:xpath-context>
      <p:when test="//d:file[resolve-uri(@href,base-uri(.))=$href]">
        <p:delete match="*"/>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
  </p:viewport>

</p:declare-step>
