<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step  version="1.0"
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:xsw="http://coko.foundation/xsweet"
  
  type="xsw:xsweet-elevation-filter" name="xsweet-elevation-filter">
  
  <!-- Implementing a mapping to modify an HTML document wrt @style and @class -->
  
  <p:input port="source" primary="true"/>
  
  <p:input port="parameters" kind="parameter"/>
  
  <p:output port="_Z_FINAL" primary="true">
    <p:pipe port="result" step="final"/>
  </p:output>
  
  <p:output port="_A_SECTIONED" primary="false">
    <p:pipe port="result" step="sections-marked"/>
  </p:output>
  
  <p:output port="_B_ARRANGED" primary="false">
    <p:pipe port="result" step="sections-nested"/>
  </p:output>
  
  <p:output port="_C_FINISHED" primary="false">
    <p:pipe port="result" step="sections-nested"/>
  </p:output>
  
  <p:serialization port="_A_SECTIONED" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_B_ARRANGED" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_C_FINISHED" indent="true" omit-xml-declaration="true"/>
  <p:serialization port="_Z_FINAL" indent="true" omit-xml-declaration="true"/>
  
  <!-- Break apart sections at h1-h6 boundaries.
       Assumption! h1-h6 have been marked. I.e. this step must occur
       *after* header promotion or the equivalent.
  
  NB by marking the sections first, we make it easier to deal with anomalous
   (broken) hierarchies in the next step. -->
  <p:xslt name="sections-marked">
    <p:input port="stylesheet">
      <p:document href="mark-sections.xsl"/>
    </p:input>
  </p:xslt>
  
  <!-- Nest the sections in hierarchies. -->
  <p:xslt name="sections-nested">
    <p:input port="stylesheet">
      <p:document href="nest-sections.xsl"/>
    </p:input>
  </p:xslt>
  
  <!--<p:xslt name="cleanup">
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0"
          xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
          xmlns="http://www.w3.org/1999/xhtml"
          xpath-default-namespace="http://www.w3.org/1999/xhtml">
        
        <xsl:template match="node() | @*">
          <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
          </xsl:copy>
        </xsl:template>
        
        <xsl:template match="span[not(@class='Hyperlink')]">
          <xsl:apply-templates/>
        </xsl:template>
        
        </xsl:stylesheet>
        
      </p:inline>
      
    </p:input>
  </p:xslt>-->
  
  <p:xslt name="finished">
    <p:input port="stylesheet">
      <p:document href="scratch-cleanup.xsl"/>
    </p:input>
  </p:xslt>
  
  
  <p:identity name="final"/>

</p:declare-step>