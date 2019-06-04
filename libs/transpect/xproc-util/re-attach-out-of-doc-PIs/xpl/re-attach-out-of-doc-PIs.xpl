<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io"
  name="re-attach-out-of-doc-PIs" type="tr:re-attach-out-of-doc-PIs" 
  exclude-inline-prefixes="#all"
  version="1.0">

  <p:documentation>This should be the penultimate step before writing back the result document.</p:documentation>

  <p:input port="source" primary="true">
    <p:documentation>A document without out-of-document processing instructions</p:documentation>
  </p:input>
  <p:option name="file-uri">
    <p:documentation>The URI of a file that can be read with unparsed-text(). Its processing instructions
    before and after the top-level element will be prepended or appended to the source port document.</p:documentation>
  </p:option>
  <p:option name="separator" select="'&#xa;'">
    <p:documentation>Will be inserted before the first PI, between individual PIs and after the last PI.</p:documentation>
  </p:option>
  <p:output port="result" sequence="true" primary="true"/>

  <p:xslt>
    <p:with-param name="file-uri" select="$file-uri"/>
    <p:with-param name="separator" select="$separator"/>
    <p:input port="stylesheet">
      <p:document href="../xsl/re-attach-out-of-doc-PIs.xsl"/>
    </p:input>
    <p:input port="parameters"><p:empty/></p:input>
  </p:xslt>

</p:declare-step>