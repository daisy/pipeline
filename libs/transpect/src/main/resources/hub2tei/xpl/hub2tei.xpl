<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io" 
  xmlns:hub2tei="http://transpect.io/hub2tei"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  version="1.0"
  name="hub2tei"
  type="hub2tei:hub2tei">
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" required="false" select="'debug/status'"/>
  
  <p:input port="source" primary="true" />
  <p:input port="paths" kind="parameter" primary="true"/>
  <p:input port="additional-inputs" sequence="true">
    <p:empty/>
  </p:input>
  
  <p:output port="result" primary="true" />
  <p:output port="report" sequence="true">
    <p:pipe port="report" step="dtp"/>
  </p:output>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/cascade/xpl/dynamic-transformation-pipeline.xpl"/>

  <tr:dynamic-transformation-pipeline load="hub2tei/hub2tei_driver" name="dtp"
    fallback-xsl="http://transpect.io/hub2tei/xsl/hub2tei.xsl"
    fallback-xpl="http://transpect.io/hub2tei/xpl/hub2tei_default.xpl">
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
    <p:input port="additional-inputs">
      <p:pipe port="additional-inputs" step="hub2tei"/>
    </p:input>
    <p:input port="options"><p:empty/></p:input>
    <p:input port="paths"><p:pipe port="paths" step="hub2tei"/></p:input>
  </tr:dynamic-transformation-pipeline>
  
</p:declare-step>
