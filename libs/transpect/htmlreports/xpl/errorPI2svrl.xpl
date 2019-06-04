<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  version="1.0" 
  exclude-inline-prefixes="#all"
  type="tr:errorPI2svrl" 
  name="errorPI2svrl">

  <p:option name="group-by-srcpath" required="false" select="'yes'"/>
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status?enabled=false'"/>
  <p:option name="pi-names" required="false" select="'letex'">
    <p:documentation>Space-separated names of processing instructions in the source document 
    that carry error messages.
    The PI text may convey additional error classification information. Example:
    &lt;?letex w2d-101 This is some error message.?&gt;</p:documentation>
  </p:option>  
  <p:option name="severity" required="false" select="'warning'">
    <p:documentation>message | warning | error | fatal-error</p:documentation>
  </p:option>
  <p:option name="step-name" select="''"/>

  <p:input port="source" primary="true" />
  <p:input port="errorPI2svrl-xsl">
    <p:document href="../xsl/errorPI2svrl.xsl"/>
  </p:input>

  <p:output port="result" primary="true">
    <p:pipe step="errorPI2svrl" port="source"/>
  </p:output>
  
  <p:output port="report" sequence="true">
    <p:documentation>The errors that were encoded as PIs, now as SVRL documents.
      They may carry a @tr:rule-family attribute on the top-level element. It could
      be 'w2d' in the example above. The XSLT, ../xsl/errorPI2svrl.xsl, has to take
      care of proper result document generation for each of these virtual checking 
      families.</p:documentation>
    <p:pipe step="create-svrls" port="secondary"/>
  </p:output>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  
  <tr:simple-progress-msg name="start-msg" file="error-pi2svrl-start.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Creating SVRL documents from error/warning processing instructions</c:message>
          <c:message xml:lang="de">Wandle als Processing Instruction kodierte Warnungen und Fehler in SVRL-Reports um</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <p:sink/>
  
  <p:xslt name="create-svrls">
    <p:input port="source">
      <p:pipe step="errorPI2svrl" port="source"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe port="errorPI2svrl-xsl" step="errorPI2svrl"/>
    </p:input>
    <p:input port="parameters"><p:empty/></p:input>
    <p:with-param name="group-by-srcpath" select="($group-by-srcpath,'yes')[1]"/>
    <p:with-param name="pi-names" select="$pi-names"/>
    <p:with-param name="severity" select="$severity"/>
    <p:with-param name="step-name" select="$step-name"/>
  </p:xslt>

  <p:sink/>

  <p:for-each>
    <p:iteration-source>
      <p:pipe step="create-svrls" port="secondary"/>
    </p:iteration-source>
    <tr:store-debug>
      <p:with-option name="pipeline-step" select="concat('schematron/pi2svrl/', replace(base-uri(), '^(.+/)(.+).xml', '$2'))"/>
      <p:with-option name="active" select="$debug" />
      <p:with-option name="base-uri" select="$debug-dir-uri" />
    </tr:store-debug>
    <p:sink/>
  </p:for-each>
  
  <tr:simple-progress-msg name="success-msg" file="error-pi2svrl-success.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Successfully created SVRL documents from error/warning processing instructions</c:message>
          <c:message xml:lang="de">Umwandlung der als Processing Instruction kodierten Warnungen und Fehler in SVRL-Reports erfolgreich abgeschlossen</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <p:sink/>
  
</p:declare-step>