<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step  version="1.0"
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:xsw="http://coko.foundation/xsweet"
  type="xsw:docx-document-production" name="docx-document-production">
  
  <!-- Run on a document.xml file (extracted from a .docx file)
       with its neighbor files in position. -->
  
  <p:input port="source"/>

  <p:input port="parameters" kind="parameter"/>
  
  <p:output port="_Z_FINAL" primary="true">
    <p:pipe port="result" step="final"/>
  </p:output>
  
  <p:output port="_A_extracted" primary="false">
    <p:pipe port="result" step="slops-extracted"/>
  </p:output>
  <p:output port="_B_arranged" primary="false">
    <p:pipe port="result" step="notes-arranged"/>
  </p:output>
  <p:output port="_C_scrubbed" primary="false">
    <p:pipe port="result" step="scrubbed"/>
  </p:output>
  <p:output port="_D_trimmed" primary="false">
    <p:pipe port="result" step="trimmed"/>
  </p:output>
  <p:output port="_E_folded" primary="false">
    <p:pipe port="result" step="folded"/>
  </p:output>
  
  <p:serialization port="_Z_FINAL"     indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_A_extracted" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_arranged"  indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_C_scrubbed"  indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_D_trimmed"   indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_E_folded"    indent="true" omit-xml-declaration="true"/>
  
  
  <!-- Now it beginneth. -->
  
  <p:xslt name="slops-extracted">
    <p:input port="stylesheet">
      <p:document href="docx-html-extract.xsl"/>
    </p:input>
    <p:with-param name="show-css" select="'yes'"/>
  </p:xslt>
  
  <p:xslt name="notes-arranged">
    <p:input port="stylesheet">
      <p:document href="handle-notes.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="scrubbed">
    <p:input port="stylesheet">
      <p:document href="scrub.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="trimmed">
    <p:input port="stylesheet">
      <p:document href="join-elements.xsl"/>
    </p:input>
  </p:xslt>
  
  <!-- Promotes detectable paragraph-wide styles into CSS on @style -->
  <p:xslt name="folded">
    <p:input port="stylesheet">
      <p:document href="collapse-paragraphs.xsl"/>
    </p:input>
  </p:xslt>
  
  <!--<p:xslt name="mapped">
    <p:input port="stylesheet">
      <p:document href="ucp-fixup.xsl"/>
    </p:input>
  </p:xslt>-->
  
  <p:identity name="final"/>

</p:declare-step>