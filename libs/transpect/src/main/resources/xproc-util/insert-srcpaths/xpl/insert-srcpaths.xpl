<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="insert-srcpaths"
  type="tr:insert-srcpaths">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step inserts the XPath location of any element as attribute.</p>
    <p>Consider this example:</p>
    <pre>&lt;root>
  &lt;element>Text&lt;/element>
&lt;/root></pre>
    <p>After applying the step, each element includes a <code>srcpath</code> 
      attribute containing its XPath location.</p>
    <pre>&lt;root srcpath="/root">
  &lt;element srcpath="/root/element">Text&lt;/element>
&lt;/root></pre>
  </p:documentation>

  <p:input port="source"/>
  <p:output port="result"/>

  <p:option name="schematron-like-paths" select="'no'">
    <p:documentation>If this option is set to 'yes', the XPath includes the full 
      namespace URI and always the position even if it equals 1. For example instead of 
      '/html', the sourcepath will be expanded to '/*:html[namespace-uri()='http://www.w3.org/1999/xhtml'][1]'. 
      This is necessary for htmlreports, if you want to use Schematron without adding srcpath spans. 
    </p:documentation>
  </p:option>
  <p:option name="exclude-elements" select="''">
    <p:documentation>White-space separated list of element names.</p:documentation>
  </p:option>  
  <p:option name="exclude-descendants" select="'yes'">
    <p:documentation>Whether the descendants of the excluded elements should be processed.</p:documentation>
  </p:option>
  
  <p:xslt>
    <p:with-param name="schematron-like-paths" select="$schematron-like-paths"/>
    <p:with-param name="exclude-elements" select="$exclude-elements"/>
    <p:with-param name="exclude-descendants" select="$exclude-descendants"/>
    <p:input port="stylesheet">
      <p:document href="../xsl/insert-srcpaths.xsl"/>
    </p:input>
  </p:xslt>

</p:declare-step>