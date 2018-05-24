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
  
  <p:output port="_0_ooxml_document" primary="false">
    <p:pipe port="result" step="wordml-document"/>
  </p:output>
  <p:output port="_A_extracted" primary="false">
    <p:pipe port="_A_extracted" step="document-production"/>
  </p:output>
  <p:output port="_B_arranged" primary="false">
    <p:pipe port="_B_arranged" step="document-production"/>
  </p:output>
  <p:output port="_C_scrubbed" primary="false">
    <p:pipe port="_C_scrubbed" step="document-production"/>
  </p:output>
  <p:output port="_D_trimmed" primary="false">
    <p:pipe port="_D_trimmed" step="document-production"/>
  </p:output>
  <p:output port="_E_folded" primary="false">
    <p:pipe port="_E_folded" step="document-production"/>
  </p:output>
  <p:output port="_F_mapped" primary="false">
    <p:pipe port="result" step="mapped"/>
  </p:output>
  <p:output port="_G_rinsed" primary="false">
    <p:pipe port="result" step="rinsed"/>
  </p:output>
  <p:output port="_H_rewired" primary="false">
    <p:pipe port="result" step="rewired"/>
  </p:output>
  
  <p:output port="_O_plaintext" primary="false">
    <p:pipe port="result" step="plaintext"/>
  </p:output>
  <p:output port="_O_analysis" primary="false">
    <p:pipe port="result" step="analysis"/>
  </p:output>
  
  <p:serialization port="_Z_FINAL"     indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_0_ooxml_document" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_A_extracted" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_arranged"  indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_C_scrubbed"  indent="false" omit-xml-declaration="true"/>
  <p:serialization port="_D_trimmed"   indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_E_folded"    indent="true" omit-xml-declaration="true"/>
  
  <p:serialization port="_F_mapped"    indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_G_rinsed"    indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_H_rewired"   indent="true" omit-xml-declaration="true"/>
  
  <p:serialization port="_O_plaintext" method="text" />
  <p:serialization port="_O_analysis"  indent="true" omit-xml-declaration="true"/>
  
  <p:import href="docx-extract/docx-document-production.xpl"/>
  
  <p:variable name="document-path" select="concat('jar:',$docx-file-uri,'!/word/document.xml')"/>
  <!--<p:variable name="document-xml"  select="doc($document-path)"/>-->
  <!-- Validate HTML5 results here:  http://validator.w3.org/nu/ -->

  <p:load name="wordml-document">
    <p:with-option name="href" select="$document-path"/>
  </p:load>
  
  
  <xsw:docx-document-production name="document-production"/>
  
  <p:identity name="mapped"/>
  <!--<p:xslt name="mapped">
    <p:input port="stylesheet">
      <p:document href="local-fixup/ucp-fixup.xsl"/>
    </p:input>
  </p:xslt>-->
  
  <p:xslt name="rinsed">
    <p:input port="stylesheet">
      <p:document href="html-polish/final-rinse.xsl"/>
    </p:input>
  </p:xslt>
  
  <!--<p:identity name="rewired"/>-->
  <p:xslt name="rewired">
    <p:input port="stylesheet">
      <p:document href="css-abstract/css-abstract.xsl"/>
    </p:input>
  </p:xslt>
 
  <p:identity name="final"/>
  
  <p:xslt name="analysis">
    <!--<p:input port="source">
      <p:pipe port="_Z_FINAL" step="document-production"/>
    </p:input>-->
    <p:input port="stylesheet">
      <p:document href="produce-analysis/html-analysis.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:xslt name="plaintext">
    <p:input port="source">
      <p:pipe port="result" step="final"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="produce-plaintext/plaintext.xsl"/>
    </p:input>
  </p:xslt>
 
</p:declare-step>