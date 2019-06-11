<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:idml2xml="http://transpect.io/idml2xml"
  xmlns:docx2hub="http://transpect.io/docx2hub"
  xmlns:tr="http://transpect.io"  
  version="1.0"
  name="extract-cssa-rules-from-nonhub"
  >
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" required="false" select="'debug'"/>
  <p:option name="file" select="''"/>
  <p:option name="all-styles" required="false" select="'yes'"/>
  
  <p:input port="previous" sequence="true">
    <p:documentation>Optional: an old version of cssa.xml</p:documentation>
    <p:empty/>
  </p:input>
  <p:output port="result" primary="true">
    <p:documentation>A document with a css:rules top-level element</p:documentation>
  </p:output>
  <p:serialization port="result" omit-xml-declaration="false" indent="true"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/idml2xml/xpl/idml2hub.xpl"/>
  <p:import href="http://transpect.io/docx2hub/xpl/wml2hub.xpl"/>
  <p:import href="extract-cssa-rules.xpl"/>
  
  <p:choose>
    <p:when test="$file eq ''">
      <cx:message message="Error: parameter file is empty!"/>
    </p:when>
    <p:when test="matches($file, '\.docx$')">
      <docx2hub:convert name="docx2hub">
        <p:with-option name="debug" select="$debug"/>
        <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
        <p:with-option name="docx" select="$file"/>
      </docx2hub:convert>
    </p:when>
    <p:when test="matches($file, '\.idml$')">
      <idml2xml:hub name="idml2hub" srcpaths="no">
        <p:with-option name="all-styles" select="$all-styles"/>
        <p:with-option name="debug" select="$debug"/>  
        <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>  
        <p:with-option name="idmlfile" select="$file"/>
      </idml2xml:hub>
    </p:when>
    <p:otherwise>
      <cx:message message="Error: Input file extension unknown!"/>
    </p:otherwise>
  </p:choose>  

  <tr:extract-cssa-rules>
    <p:with-option name="debug" select="$debug"/>  
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>  
    <p:input port="previous">
      <p:pipe port="previous" step="extract-cssa-rules-from-nonhub"/>
    </p:input>
  </tr:extract-cssa-rules>
  
</p:declare-step>
