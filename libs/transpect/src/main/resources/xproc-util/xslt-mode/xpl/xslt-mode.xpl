<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:idml2xml="http://transpect.io/idml2xml"
  xmlns:docx2hub="http://transpect.io/docx2hub"
  xmlns:hub2tei="http://transpect.io/hub2tei"
  xmlns:tei2hub="http://transpect.io/tei2hub"
  xmlns:html2hub="http://transpect.io/html2hub"
  xmlns:ttt="http://transpect.io/tokenized-to-tree"
  xmlns:hub="http://transpect.io/hub"
  xmlns:hub2app="http://transpect.io/hub2app"
  xmlns:hub2htm="http://transpect.io/hub2htm"
  xmlns:tr3k2html="http://www.le-tex.de/namespace/tr3k2html"  
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="xslt-mode"
  type="tr:xslt-mode">
    
  <p:option name="mode" required="true">
    <p:documentation>Please be aware that, as per the spec, the initial mode option of
    p:xslt must be a QName. You cannot invoke the #default mode here.
    And if you’re using namespace-prefixed modes, you’ll have to declare the namespaces
    here in this .xpl file. This is admittedly unfortunate.</p:documentation>
  </p:option>
  <p:option name="prefix" required="false" select="'default'"/>
  <p:option name="msg" required="false" select="'no'"/>
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" required="true"/>
  <p:option name="debug-indent" required="false" select="'true'"/>
  <p:option name="status-dir-uri" select="concat(replace($debug-dir-uri, '^(.+)\?.*$', '$1'), '/status')"/>
  <p:option name="fail-on-error" select="'no'"/>
  <p:option name="store-secondary" select="'yes'"/>
  <p:option name="adjust-doc-base-uri" select="'yes'">
    <p:documentation>Whether to set the output base uri to what’s set as base-uri(/*) via @xml:base attribute.
    Otherwise, the output base uri will be taken from the input. This was the defaul behavior before
    this change that was introduced on 2016-08-24. Reason for this option: If people add /*/@xml:base attributes,
    they most likely want their output document URI to reflect this change.</p:documentation>
  </p:option>
  <p:option name="hub-version" required="false" select="''"/>
  
  <p:input port="source" primary="true" sequence="true"/>
  
  <p:input port="stylesheet"/>
  
  <p:input port="models" sequence="true">
    <p:empty/>
    <p:documentation>see prepend-xml-model.xpl</p:documentation>
  </p:input>
  
  <p:input port="parameters" kind="parameter" primary="true" sequence="true"/>
  
  <p:output port="result" primary="true" sequence="true"/>
  
  <p:output port="secondary" sequence="true" primary="false">
    <p:pipe port="secondary" step="try"/>
  </p:output>
  
  <p:output port="report" sequence="true" primary="false">
    <p:pipe port="report" step="try"/>
  </p:output>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/xproc-util/xml-model/xpl/prepend-xml-model.xpl" />
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  
  <p:variable name="debug-file-name" select="concat($prefix, '.', replace($mode, ':', '_'))"><p:empty/></p:variable>
  <p:variable name="debug-dir-uri1" select="replace($debug-dir-uri, '^(.+)\?.*$', '$1')"><p:empty/></p:variable>
  
  <!-- try wrapper to recover from errors and proceed with input -->
  
  <p:parameters name="consolidate-params">
    <p:input port="parameters">
      <p:pipe port="parameters" step="xslt-mode"/>
    </p:input>
  </p:parameters>
  
  <p:identity>
    <p:input port="source">
      <p:pipe port="source" step="xslt-mode"/>
    </p:input>
  </p:identity>
  
  <p:try name="try">
    <p:group>
      <p:output port="result" primary="true" sequence="true">
        <p:documentation>Although p:xslt produces exactly one result document, sequence must be true so that each branch 
          has the same signature.</p:documentation>
      </p:output>
      <p:output port="report" primary="false" sequence="true"/>
      <p:output port="secondary" primary="false" sequence="true">
        <p:pipe port="secondary" step="xslt"/>
      </p:output>
      
      <p:choose>
        <p:xpath-context><p:empty/></p:xpath-context>
        <p:when test="$msg = 'yes'">
          <cx:message>
            <p:with-option name="message" 
              select="concat('Mode: ', $mode, 
              if ($prefix and $debug = 'yes') 
              then concat('  debugs into ', $debug-dir-uri1, '/', replace($debug-file-name, '//+', '/'), '.xml') 
              else ''
              )"><p:empty/></p:with-option>
          </cx:message>
        </p:when>
        <p:otherwise>
          <p:identity/>
        </p:otherwise>
      </p:choose>
      
      <p:xslt name="xslt">
        <p:with-option name="initial-mode" select="$mode">
          <p:pipe port="stylesheet" step="xslt-mode"/>
        </p:with-option>
        <p:input port="parameters">
          <p:pipe port="result" step="consolidate-params"/>
        </p:input>
        <p:input port="stylesheet">
          <p:pipe port="stylesheet" step="xslt-mode"/>
        </p:input>
        <p:with-param name="debug" select="$debug"><p:empty/></p:with-param>
      </p:xslt>

      <p:choose name="adjust-doc-base-uri">
        <p:when test="$adjust-doc-base-uri = 'yes'">
          <p:output port="result" primary="true"/>
          <p:variable name="xml-base-att" select="/*/@xml:base"/>
          <p:xslt name="adjust-doc-base-uri1">
            <p:documentation>With Saxon 9.7 and 9.8, base-uri(/*) can have a value that is different from base-uri()
            even when there is no xml:base attribute on /*. 
            See https://github.com/ndw/xmlcalabash1/issues/281
            Using p:variable here because (base-uri(/*)[/*/@xml:base] resulted in an error: 
            "Leading '/' selects nothing: the context item is not a node"</p:documentation>
            <p:with-option name="output-base-uri"
              select="resolve-uri((base-uri(/*)[normalize-space($xml-base-att)], base-uri())[1])"/>
            <p:input port="parameters"><p:empty/></p:input>
            <p:input port="stylesheet">
              <p:inline>
                <xsl:stylesheet version="2.0">
                  <xsl:template match="/">
                    <xsl:sequence select="."/>
                  </xsl:template>
                </xsl:stylesheet>
              </p:inline>
            </p:input>
          </p:xslt>    
        </p:when>
        <p:otherwise>
          <p:output port="result" primary="true"/>
          <p:identity name="adjust-doc-base-uri0"/>
        </p:otherwise>
      </p:choose>

      <p:sink/>
      
      <p:choose>
        <p:when test="$store-secondary = ('yes', 'true')">
          <p:for-each>
            <p:iteration-source>
              <p:pipe step="xslt" port="secondary"/>
            </p:iteration-source>
            <p:store indent="true" omit-xml-declaration="false">
              <p:with-option name="href" select="base-uri()"/>
            </p:store>
          </p:for-each>    
        </p:when>
        <p:otherwise>
          <p:identity>
            <p:input port="source">
              <p:empty/>
            </p:input>
          </p:identity>
          <p:sink/>
        </p:otherwise>
      </p:choose>
      
      <tr:prepend-xml-model>
        <p:input port="source">
          <p:pipe port="result" step="adjust-doc-base-uri"/>
        </p:input>
        <p:input port="models">
          <p:pipe port="models" step="xslt-mode"/>
        </p:input>
        <p:with-option name="hub-version" select="$hub-version"/>
      </tr:prepend-xml-model>
      
      <tr:store-debug>
        <p:with-option name="pipeline-step" select="$debug-file-name"/>
        <p:with-option name="active" select="$debug" />
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
        <p:with-option name="indent" select="$debug-indent"/>
      </tr:store-debug>
      
    </p:group>
    <p:catch name="catch">
      <p:output port="result" primary="true" sequence="true">
        <p:pipe port="source" step="xslt-mode"/>
      </p:output>
      <p:output port="report" primary="false" sequence="true">
        <p:pipe port="result" step="forward-error"/>
      </p:output>
      <p:output port="secondary" primary="false" sequence="true"/>
      
      <tr:propagate-caught-error name="forward-error" fail-on-error="no" rule-family="Internal" severity="fatal-error"
        step-type="tr:xslt-mode">
        <p:input port="source">
          <p:pipe port="error" step="catch"/>
        </p:input>
        <p:with-option name="code" select="replace($mode, ':', '_')">
          <p:empty/>
        </p:with-option>
        <p:with-option name="msg-file" select="concat($debug-file-name, '.error.txt')">
          <p:empty/>
        </p:with-option>
        <p:with-option name="status-dir-uri" select="$status-dir-uri">
          <p:empty/>
        </p:with-option>
      </tr:propagate-caught-error>
      
      <tr:store-debug>
        <p:with-option name="pipeline-step" select="concat($debug-file-name, '.ERROR')"/>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri" />
      </tr:store-debug>
      
      <!-- if option fail-on-error is set to 'yes', the step fails with the original error code -->
      <p:choose>
        
        <p:when test="$fail-on-error eq 'yes'">
          
          <cx:message>
            <p:with-option name="message" select="concat('[FATAL ERROR]: XSLT mode ''', $mode, 
              ''' failed due to conversion errors. ',
              if ($prefix and $debug = 'yes') 
              then concat('Please see ', $debug-dir-uri1, '/', replace($debug-file-name, '//+', '/'), '.ERROR.xml for detailed debugging information.') 
              else ''
              )"/>
          </cx:message>
          
          <p:error>
            <p:with-option name="code" select="concat('xslt-mode-', replace($mode, ':', '_'))"/>
            <p:input port="source">
              <p:pipe port="error" step="catch"/>
            </p:input>
          </p:error>
        </p:when>
        <p:otherwise>
          
          <cx:message>
            <p:with-option name="message" select="concat('[FATAL ERROR]: XSLT mode ''', $mode, 
              ''' failed due to conversion errors. Recovering from errors and proceeding with original input. ',
              if ($prefix and $debug = 'yes') 
              then concat('Please see ', $debug-dir-uri1, '/', replace($debug-file-name, '//+', '/'), '.ERROR.xml for detailed debugging information.') 
              else ''
              )"/>
          </cx:message>
          
          <p:identity/>
          
        </p:otherwise>
      </p:choose>
      
      <p:sink/>
      
    </p:catch>
  </p:try>
  
</p:declare-step>