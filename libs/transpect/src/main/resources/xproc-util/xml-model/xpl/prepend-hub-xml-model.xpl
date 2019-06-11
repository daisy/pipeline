<?xml version="1.0" encoding="utf-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:idml2xml="http://transpect.io/idml2xml"
  xmlns:tr="http://transpect.io"
  version="1.0">

  <p:declare-step   
    name="prepend-hub-xml-model"
    type="tr:prepend-hub-xml-model">
    
    <p:input port="source" primary="true" />
    <p:output port="result" primary="true" sequence="true"/>

    <p:option name="hub-version" required="true"/>

    <p:xslt>
      <p:with-param name="hub-version" select="$hub-version"/>
      <p:input port="stylesheet">
        <p:inline>
          <xsl:stylesheet version="2.0">
            <xsl:param name="hub-version" as="xs:string"/>
            <xsl:template match="/">
              <xsl:text>&#xa;</xsl:text>
              <xsl:processing-instruction name="xml-model" 
                select="concat('href=&#x22;http://www.le-tex.de/resource/schema/hub/', $hub-version, 
                '/hub.rng&#x22; type=&#x22;application/xml&#x22; schematypens=&#x22;http://relaxng.org/ns/structure/1.0&#x22;')"/>
              <xsl:text>&#xa;</xsl:text>
              <xsl:processing-instruction name="xml-model" 
                select="concat('href=&#x22;http://www.le-tex.de/resource/schema/hub/', $hub-version, 
                '/hub.rng&#x22; type=&#x22;application/xml&#x22; schematypens=&#x22;http://purl.oclc.org/dsdl/schematron&#x22;')"/>
              <xsl:text>&#xa;</xsl:text>
              <xsl:copy-of select="*"/>
            </xsl:template>
          </xsl:stylesheet>
        </p:inline>
      </p:input>
      <p:input port="parameters"><p:empty/></p:input>
    </p:xslt>
    
  </p:declare-step>

</p:library>