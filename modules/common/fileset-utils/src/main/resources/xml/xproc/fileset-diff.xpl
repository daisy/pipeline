<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-diff" name="main"
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
  exclude-inline-prefixes="px">

  <p:input port="source" primary="true"/>
  <p:input port="secondary"/>
  <p:output port="result"/>




  <p:viewport match="d:file">
    <p:variable name="href" select="*/resolve-uri(@href,base-uri(.))"/>
    <p:choose>
      <p:xpath-context>
        <p:pipe port="secondary" step="main"/>
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
