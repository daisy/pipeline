<?xml version="1.0" encoding="UTF-8"?>

<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
  type="xsw:html-header-promote" name="html-header-promote">


  <!-- XSweet: Apply header promotion XSLT chain into a single XProc call -->
  <!-- Input: an HTML typescript file with no headers  -->
  <!-- Output: a copy, with headers promoted -->
<!-- Note: unlike the 'chooser' header promotion macro XSLT, this XProc is wired up to 'property-based' header promotion. -->
  <p:input port="source" primary="true"/>
  
  <p:input port="parameters" kind="parameter"/>
  
  <p:output port="_Z_FINAL" primary="true">
    <p:pipe port="result" step="final"/>
  </p:output>
  <p:output port="_A_digested" primary="false">
    <p:pipe port="result" step="digest-paragraphs"/>
  </p:output>
  <p:output port="_B_headers-promoted" primary="false">
    <p:pipe port="result" step="apply-the-header-mapping-xslt"/>
  </p:output>
  <p:output port="_X_escalator-xslt" primary="false">
    <p:pipe port="result" step="escalator-xslt"/>
  </p:output>

  <p:serialization port="_A_digested"         indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_headers-promoted" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_X_escalator-xslt"   indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_Z_FINAL"            indent="true" omit-xml-declaration="true"/>
  
  <p:identity name="in"/>
  
  <!-- First, reduce the input to a "weighted profile" of its paragraph styles -->
  <p:xslt name="digest-paragraphs">
    <p:input port="stylesheet">
      <p:document href="digest-paragraphs.xsl"/>
    </p:input>
  </p:xslt>
  
  <!-- Then generate an XSLT stylesheet from it -->
  <p:xslt name="escalator-xslt">
    <p:input port="stylesheet">
      <p:document href="make-header-escalator-xslt.xsl"/>
    </p:input>
  </p:xslt>

  <!-- We apply the stylesheet we have generated to the original doc -->
  <p:xslt name="apply-the-header-mapping-xslt">
    <p:input port="source">
      <p:pipe port="result" step="in"/>
    </p:input><p:input port="stylesheet">
      <p:pipe port="result" step="escalator-xslt"/>
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