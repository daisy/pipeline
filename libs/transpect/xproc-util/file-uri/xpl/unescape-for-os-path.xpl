<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io" 
  version="1.0" 
  name="unescape-uri" 
  type="tr:unescape-uri">

  <p:input port="source"><p:empty/></p:input>

  <p:option name="uri" select="''"/>
  <p:option name="attribute-names" select="''"/>
  
  <p:output port="result" primary="true">
    <p:documentation>Dual use: If $uri is non-empty, a c:result element with the %HH-unescaped $uri string as text content.
    If $uri is the empty string or if it contains only WS, then $attribute-names needs to be a space-separated string of attribute
    names. In the latter case, there must also be a document on the source port. All attribute occurrences of the given names will 
    be unescaped in this document.</p:documentation>
  </p:output>
  
  <p:xslt name="unescape" template-name="main">
    <p:input port="source">
      <p:pipe port="source" step="unescape-uri"/>
    </p:input>
    <p:input port="parameters"><p:empty/></p:input>
    <p:with-param name="uri" select="$uri"/>
    <p:with-param name="attribute-names" select="$attribute-names"/>
    <p:input port="stylesheet">
      <p:document href="../xsl/unescape-for-os-path.xsl"/>
    </p:input>
  </p:xslt>
  
</p:declare-step>
