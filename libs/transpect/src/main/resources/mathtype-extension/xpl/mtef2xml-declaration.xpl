<?xml version="1.0"?>
<p:declare-step  
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io" 
  version="1.0"
  type="tr:mtef2xml">
  
  <p:documentation>Convert an OLE-Object containing a Mathtype equation to plain XML.
  Uses Jruby to create an XML-representation of the MTEF formula.</p:documentation>
  
  <p:output port="result" primary="true" sequence="true">
    <p:documentation>The MathML equation from file @href.</p:documentation>
  </p:output>
  <p:option name="href">
    <p:documentation>The equation file URI. (OLE-Object)</p:documentation>
  </p:option>
  
</p:declare-step>
