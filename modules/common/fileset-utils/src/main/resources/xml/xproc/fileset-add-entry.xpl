<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="px"
                type="px:fileset-add-entry" name="main">

  <p:input port="source.fileset" primary="true">
    <p:inline exclude-inline-prefixes="#all"><d:fileset/></p:inline>
  </p:input>
  <p:input port="source.in-memory" sequence="true">
    <p:empty/>
  </p:input>
  <p:output port="result.fileset" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:pipe step="add-entry" port="result.in-memory"/>
  </p:output>
  <p:input port="entry" sequence="true">
    <p:empty/>
  </p:input>
  <p:option name="href" cx:as="xs:string" select="''"/>
  <p:option name="media-type" select="''"/>
  <p:option name="original-href" select="''"/>
  <p:option name="first" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace-attributes" cx:as="xs:boolean" select="false()"/>
  <p:input port="file-attributes" kind="parameter" primary="false">
    <p:inline>
      <c:param-set/>
    </p:inline>
  </p:input>

  <p:import href="fileset-add-entries.xpl"/>

  <px:fileset-add-entries assert-single-entry="true" name="add-entry">
    <p:input port="source.in-memory">
      <p:pipe step="main" port="source.in-memory"/>
    </p:input>
    <p:input port="entries">
      <p:pipe step="main" port="entry"/>
    </p:input>
    <p:input port="file-attributes">
      <p:pipe step="main" port="file-attributes"/>
    </p:input>
    <p:with-option name="href" select="$href[not(.='')]"/>
    <p:with-option name="media-type" select="$media-type"/>
    <p:with-option name="original-href" select="$original-href"/>
    <p:with-option name="first" select="$first"/>
    <p:with-option name="replace" select="$replace"/>
    <p:with-option name="replace-attributes" select="$replace-attributes"/>
  </px:fileset-add-entries>

</p:declare-step>
