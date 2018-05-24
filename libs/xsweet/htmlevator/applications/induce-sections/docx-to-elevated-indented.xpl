<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
  xmlns:xsw="http://coko.foundation/xsweet"
  type="xsw:docx-extract-and-refine" name="docx-extract-and-refine">
  
  <p:input port="parameters" kind="parameter"/>
  
  <p:option name="docx-file-uri" required="true"/>
  
  <p:output port="_Z_FINAL">
    <p:pipe port="result" step="final"/>
  </p:output>
  <p:output port="_A_EXTRACTED">
    <p:pipe port="_Z_FINAL" step="document-production"/>
  </p:output>
  <p:output port="_B_HEADERS-PROMOTED">
    <p:pipe port="_Z_FINAL" step="header-promote"/>
  </p:output>
  <p:output port="_C_RINSED">
    <p:pipe port="result" step="rinsed"/>
  </p:output>
  
  <p:output port="_DA_SECTIONED">
    <p:pipe port="_A_SECTIONED" step="html-elevated"/>
  </p:output>
  <p:output port="_DB_ARRANGED">
    <p:pipe port="_B_ARRANGED" step="html-elevated"/>
  </p:output>
  <p:output port="_DC_FINISHED">
    <p:pipe port="_C_FINISHED" step="html-elevated"/>
  </p:output>
  <p:output port="_DZ_ELEVATED">
    <p:pipe port="_Z_FINAL" step="html-elevated"/>
  </p:output>
  
  <p:serialization port="_Z_FINAL"            indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_A_EXTRACTED"        indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_HEADERS-PROMOTED" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_C_RINSED"           indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_DA_SECTIONED"       indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_DB_ARRANGED"        indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_DC_FINISHED"        indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_DZ_ELEVATED"        indent="true" omit-xml-declaration="true"/>
  
  <!--<p:import href="docx-extract/docx-document-production.xpl"/>-->
  <p:import href="../XSweet/applications/docx-extract/docx-document-production.xpl"/>
  
  <p:import href="../XSweet/applications/header-promote/html-header-promote.xpl"/>
  
  <p:import href="xsweet-elevation-filter.xpl"/>
  
  <p:variable name="document-path" select="concat('jar:',$docx-file-uri,'!/word/document.xml')"/>
  <!--<p:variable name="document-xml"  select="doc($document-path)"/>-->
  <!-- Validate HTML5 results here:  http://validator.w3.org/nu/ -->

  <p:load>
    <p:with-option name="href" select="$document-path"/>
  </p:load>
  
  <xsw:docx-document-production name="document-production"/>

  <xsw:html-header-promote name="header-promote"/>
  
  <p:xslt name="rinsed">
    <p:input port="stylesheet">
      <p:document href="../XSweet/applications/html-polish/final-rinse.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="css-abstracted">
    <p:input port="stylesheet">
      <p:document href="../XSweet/applications/css-abstract/css-abstract.xsl"/>
    </p:input>
  </p:xslt>
  
  <xsw:xsweet-elevation-filter name="html-elevated"/>
  
  <p:identity name="final"/>
  
 
</p:declare-step>