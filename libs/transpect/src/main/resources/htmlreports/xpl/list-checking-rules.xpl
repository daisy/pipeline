<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  exclude-inline-prefixes="#all" 
  version="1.0" 
  type="tr:list-checking-rules" 
  name="list-checking-rules">

  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status?enabled=false'"/>
  <p:option name="report-title" required="false" select="''"/>
  <p:option name="interface-language" required="false" select="'en'"/>

  <p:input port="source" primary="true" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Will render an HTML report for two different scenarios (and input files). What comes out will
      depend on the input elements (of course).</p>
      <h4>Use case 1</h4>
      <p>Schematron documents and Relax NG schemas.</p>
      <p>If the Schematron documents have an tr:rule-family attribute, it will be used as a heading.</p>
      <h4>Use case 2</h4>
      <p>A messages-grouped-by-type.xml document as produced on the secondary port of patch-svrl.xpl</p>
    </p:documentation>
  </p:input>
  <p:input port="stylesheet">
    <p:document href="../xsl/list-checking-rules.xsl"/>
  </p:input>
  <p:input port="parameters" kind="parameter" primary="true"/>

  <p:output port="result" primary="true"/>
  <p:serialization port="result" omit-xml-declaration="false" method="xhtml"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>

  <tr:simple-progress-msg name="start-msg" file="list-checking-rules-start.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Generating a list of checking rules that have been applied.</c:message>
          <c:message xml:lang="de">Erstelle eine Liste der angewandten Prüfregeln.</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"><p:empty/></p:with-option>
  </tr:simple-progress-msg>

  <p:xslt name="generate-html" template-name="main">
    <p:input port="stylesheet">
      <p:pipe step="list-checking-rules" port="stylesheet"/>
    </p:input>
    <p:with-param name="interface-language" select="$interface-language"><p:empty/></p:with-param>
    <p:with-param name="title" select="$report-title"><p:empty/></p:with-param>
  </p:xslt>

  <tr:store-debug pipeline-step="htmlreports/list-checking-rules" extension="xhtml">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <tr:simple-progress-msg name="success-msg" file="patch-svrl-success.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Messages have been inserted into the HTML report</c:message>
          <c:message xml:lang="de">Meldungen wurden in den HTML-Prüfbericht eingefügt</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>

</p:declare-step>
