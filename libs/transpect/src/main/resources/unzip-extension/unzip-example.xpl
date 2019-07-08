<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:tr="http://transpect.io"
  version="1.0">

  <p:output port="result" primary="true">
    <p:documentation>/c:files/c:file</p:documentation>
    <p:pipe port="result" step="unzip"/>
  </p:output>
  <p:serialization port="result" omit-xml-declaration="false" indent="true"/>

  <p:output port="list-with-directories">
    <p:documentation>/c:files/c:directory/c:file</p:documentation>
    <p:pipe port="list-with-directories" step="unzip"/>
  </p:output>
  <p:serialization port="list-with-directories" omit-xml-declaration="false" indent="true"/>

  <p:option name="zip" required="true"/>
  <p:option name="path" required="true"/>

  <p:import href="unzip-declaration.xpl"/>

  <tr:unzip name="unzip">
    <p:with-option name="zip" select="$zip"/>
    <p:with-option name="dest-dir" select="$path"/>
    <p:with-option name="overwrite" select="'yes'"/>
  </tr:unzip>

</p:declare-step>