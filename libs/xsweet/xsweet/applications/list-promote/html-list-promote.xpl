<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
  type="xsw:html-header-promote" name="html-header-promote">

  <!-- XSweet: XProc implementation of two-step list promotion pipeline. -->
  
  <p:input port="source" primary="true"/>
  
  <p:input port="parameters" kind="parameter"/>
  
  <p:output port="_0_INPUT" primary="false">
    <p:pipe port="result" step="in"/>
  </p:output>
  <p:output port="_Z_FINAL" primary="true">
    <p:pipe port="result" step="final"/>
  </p:output>
  <p:output port="_A_marked" primary="false">
    <p:pipe port="result" step="mark-lists"/>
  </p:output>
  <p:output port="_B_structured" primary="false">
    <p:pipe port="result" step="structure-lists"/>
  </p:output>

  <p:serialization port="_A_marked"     indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_structured" indent="true" omit-xml-declaration="true"/>
  
  <p:identity name="in"/>
  
  <p:xslt name="mark-lists">
    <p:input port="stylesheet">
      <p:document href="mark-lists.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="structure-lists">
    <p:input port="stylesheet">
      <p:document href="itemize-lists.xsl"/>
    </p:input>
  </p:xslt>

  <p:identity name="final"/>
  
  <!--<p:xslt name="">
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0"
          xmlns="http://www.w3.org/1999/xhtml"
          xpath-default-namespace="http://www.w3.org/1999/xhtml"
          exclude-result-prefixes="#all"/>
      </p:inline>
    </p:input>
  </p:xslt>-->
  
  
  
</p:declare-step>