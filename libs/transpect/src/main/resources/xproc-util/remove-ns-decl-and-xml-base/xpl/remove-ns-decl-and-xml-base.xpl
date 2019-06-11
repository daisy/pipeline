<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="remove-ns-decl-and-xml-base" 
  type="tr:remove-ns-decl-and-xml-base">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>The purpose of this identity transformation is to remove all namespace declarations. 
      The step prevents that XProc writes all prefixes declared in the pipeline are written 
      into the output.</p>
  </p:documentation>
  
  <p:input port="source"/>
  
  <p:output port="result"/>
  
  <p:xslt>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
          <xsl:template match="* | @*">
            <xsl:copy copy-namespaces="no">
              <xsl:apply-templates select="@*, node()"/>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="@xml:base"/>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt> 
</p:declare-step>