<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-join" name="main"
  xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

  <p:input port="source" sequence="true"/>
  <p:output port="result" primary="true"/>

  <p:xslt template-name="join">
    <p:input port="stylesheet">
      <p:document href="../xslt/fileset-join.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
