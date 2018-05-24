<?xml version="1.0" encoding="UTF-8"?>

<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
  type="xsw:html-header-promote" name="html-header-promote">
  
  <p:input port="parameters" kind="parameter"/>
  
  <p:option name="dirpath" required="true"/>
  
  <p:output port="HTML" primary="false">
    <p:pipe port="result" step="produce-manifest-html"/>
  </p:output>
  <p:output port="HTML-sorted" primary="false">
    <p:pipe port="result" step="organize-manifest"/>
  </p:output>
  <p:output port="MARKDOWN" primary="true">
    <p:pipe port="result" step="render-markdown"/>
  </p:output>

  <p:serialization port="HTML"        method="html" omit-xml-declaration="true" indent="true"/>
  <p:serialization port="HTML-sorted" method="html" omit-xml-declaration="true" indent="true"/>
  <p:serialization port="MARKDOWN"    method="text" omit-xml-declaration="true"/>
  
  <p:xslt name="produce-manifest-html">
    <p:input port="source">
      <p:document href="directory-manifest.xsl"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="directory-manifest.xsl"/>
    </p:input>
    <p:with-param name="dirpath" select="$dirpath"/>
  </p:xslt>
  
  <p:xslt name="organize-manifest">
    <p:input port="stylesheet">
      <p:document href="manifest-reorder.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="render-markdown">
    <p:input port="stylesheet">
      <p:document href="html-to-markdown.xsl"/>
    </p:input>
  </p:xslt>
  
</p:declare-step>