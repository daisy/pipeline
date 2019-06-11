<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:cxf="http://xmlcalabash.com/ns/extensions/fileutils"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="paths-for-files"
  type="tr:paths-for-files">
  
  <p:documentation>Will calculate the content repository locations for a space separated
  sequence of flat filenames. The filenames must adhere to the naming conventions, e.g.,
  101026_04711_RAT.idml or 101027_00123_ADHOC_fig_2-3.pdf.</p:documentation>
  
  <p:option name="filenames" required="true"/>
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" required="false" select="resolve-uri('debug')"/>
  <p:option name="status-dir-uri" required="false" select="'status?enabled=false'"/>
  <p:option name="fail-on-error" select="'false'"/>
  
  <p:input port="conf" primary="true">
    <p:document href="http://this.transpect.io/conf/transpect-conf.xml"/>
  </p:input>
  <p:output port="result" primary="true"/>
  
  <p:serialization port="result" method="text"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>
  <p:import href="http://transpect.io/cascade/xpl/paths.xpl"/>
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/>
  
  <p:load name="import-paths-xsl">
    <p:with-option name="href" select="(/*/@paths-xsl-uri, 'http://transpect.io/cascade/xsl/paths.xsl')[1]">
      <p:pipe port="conf" step="paths-for-files"/>
    </p:with-option>
  </p:load>
  
  <p:sink/>
  
  <p:xslt template-name="main">
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="source"><p:empty/></p:input>
    <p:with-param name="filenames" select="$filenames"/>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:param name="filenames" as="xs:string*"></xsl:param>
          <xsl:template name="main">
            <c:filenames>
              <xsl:for-each select="$filenames">
                <c:file name="{.}"/>
              </xsl:for-each>
            </c:filenames>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>
  
  <p:for-each name="iter">
    <p:iteration-source select="/c:filenames/c:file"/>
    <tr:paths name="paths">
      <p:with-option name="pipeline" select="'paths-for-files.xpl'"/>
      <p:with-option name="file" select="/*/@name"/>
      <p:with-option name="debug" select="$debug"/>  
      <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
      <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
      <p:input port="stylesheet">
        <p:pipe port="result" step="import-paths-xsl"/>
      </p:input>
      <p:input port="conf">
        <p:pipe port="conf" step="paths-for-files"/>
      </p:input>
      <p:input port="params">
        <p:empty/>
      </p:input>
    </tr:paths>
    <tr:file-uri name="file-uri">
      <p:with-option name="filename" select="/c:param-set/c:param[@name eq 'repo-href-local']/@value"/>
    </tr:file-uri>
    <p:sink/>
    <p:string-replace match="/message/file" name="error-message">
      <p:input port="source">
        <p:inline><message>The file name '<file/>' could not be allocated to a content directory according to the clades and/or filename based resolution.
         Please check whether it complies with the naming conventions.</message></p:inline>
      </p:input>
      <p:with-option name="replace" select="concat('''', /*/@lastpath, '''')">
        <p:pipe port="result" step="file-uri"/>
      </p:with-option>
    </p:string-replace>
    <p:choose>
      <p:xpath-context>
        <p:pipe port="result" step="paths"/>
      </p:xpath-context>
      <p:when test="/c:param-set/c:param[@name eq 'matching-clades']/@value = '0'">
        <p:choose>
          <p:when test="$fail-on-error = 'true'">
            <p:error code="tr:PATH02">
              <p:input port="source">
                <p:pipe port="result" step="error-message"/>
              </p:input>
            </p:error>    
          </p:when>
          <p:otherwise>
            <cx:message>
              <p:with-option name="message" select="/message"/>
            </cx:message>
          </p:otherwise>
        </p:choose>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
    <p:sink/>
    <p:string-replace match="/x/y">
      <p:input port="source">
        <p:inline><x><y/> </x></p:inline>
      </p:input>
      <p:with-option name="replace" select="concat('''', /*/@os-path, '''')">
        <p:pipe port="result" step="file-uri"/>
      </p:with-option>
    </p:string-replace>
  </p:for-each>
  
  <p:wrap-sequence wrapper="foo" name="wrap-seq"/>

</p:declare-step>