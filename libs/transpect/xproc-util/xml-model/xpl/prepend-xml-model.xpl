<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:idml2xml="http://transpect.io/idml2xml"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="prepend-xml-model" 
  type="tr:prepend-xml-model">
  
  <p:input port="source" primary="true" sequence="true"/>
  <p:input port="models" sequence="true">
    <p:documentation>c:models documents with c:model entries. c:model has the same attributes as the xml-model
      processing instruction has pseudo-attributes, see http://www.w3.org/TR/xml-model/
      href
      type
      schematypens
      charset
      group
      phase
    </p:documentation>
    <p:empty/>
  </p:input>
  
  <p:output port="result" primary="true" sequence="true"/>

  <p:option name="hub-version" required="false" select="''"/>
  
  <p:for-each name="no-op-if-empty-source">
    <p:iteration-source>
      <p:pipe port="source" step="prepend-xml-model"/>
    </p:iteration-source>
    <p:output port="result">
      <p:pipe port="result" step="prepend-with-xslt"/>
    </p:output>
    
    <p:xslt name="prepend-with-xslt">
      <p:input port="source">
        <p:pipe step="no-op-if-empty-source" port="current"/>
        <p:pipe port="models" step="prepend-xml-model"/>
      </p:input>
      <p:with-param name="hub-version" select="$hub-version"/>
      <p:input port="stylesheet">
        <!-- inlining caused problems-->
        <p:document href="../xsl/prepend-xml-model.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>
  </p:for-each>

</p:declare-step>