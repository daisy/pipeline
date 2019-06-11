<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  version="1.0" 
  name="paths-for-files-xml"
  type="tr:paths-for-files-xml">
  
  <p:documentation>
    An implementation of paths-for-files which provides XML output
  </p:documentation>
  
  <p:input port="conf">
    <p:document href="http://this.transpect.io/conf/transpect-conf.xml"/>
  </p:input>
  <p:output port="result"/>
  
  <p:option name="filenames">
    <p:documentation>space-separated list of filenames</p:documentation>
  </p:option>
  
  <p:import href="http://transpect.io/cascade/xpl/paths-for-files.xpl"/>
  
  <tr:paths-for-files name="paths-for-files">
    <p:with-option name="filenames" select="$filenames"/>
    <p:input port="conf">
      <p:pipe port="conf" step="paths-for-files-xml"/>
    </p:input>
  </tr:paths-for-files>
  
  <p:xslt name="file-representation">
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
          version="2.0" 
          exclude-result-prefixes="#all">
          
          <xsl:template match="/">
            <c:files>
              <xsl:for-each select="tokenize(string-join(.//text(), '\s'), '\s+')[normalize-space()]">
                <c:file name="{.}"/>
              </xsl:for-each>
            </c:files>
          </xsl:template>
          
        </xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>
  
</p:declare-step>