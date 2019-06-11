<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="validate-with-rng-svrl"
  type="tr:validate-with-rng-svrl">
  
  <p:documentation>
    This step validates an XML document with a RelaxNG schema and 
    provides the validation results as schematron report. The source 
    XML document is also the primary output. The report output port
    provides the schematron report.
  </p:documentation>
  
  <p:input port="source" primary="true">
    <p:documentation>
      The source port expects the xml document to be validated. Prior to validation, @srcpath attributes (and /*/@source-dir-uri)
    will be removed if $remove-srcpaths is true (default).</p:documentation>
  </p:input>
  <p:input port="schema" primary="false">
    <p:documentation>
      A RelaxNG-XML-schema is expected to arrive at the schema port.
    </p:documentation>
  </p:input>
  <p:input port="errorPI2svrl-xsl">
    <p:document href="../xsl/errorPI2svrl.xsl"/>
  </p:input>
  
  <p:output port="report" sequence="true">
    <p:documentation>
      The schematron document.
    </p:documentation>
    <p:pipe step="errorPI2svrl" port="report"/>
  </p:output>
  <p:output port="result" primary="true">
    <p:documentation>
      The source XML file
    </p:documentation>
    <p:pipe step="errorPI2svrl" port="result"/>
  </p:output>

  <p:option name="debug" select="'yes'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status?enabled=false'"/>
  <p:option name="remove-srcpaths" select="'true'" required="false">
    <p:documentation>The effect of this option being true is: remove @srcpath and /*/@source-dir-uri from the source document 
      prior to validation, but use the unaltered source document for looking up the closest @srcpaths for each validation error.
      This should be set to false for validation of Hub XML and other schemas in which @srcpath and /*/@source-dir-uri are legal.
    </p:documentation>
  </p:option>
  <p:option name="group-by-srcpath" required="false" select="'no'">
    <p:documentation>see tr:errorPI2svrl</p:documentation>
  </p:option>
  <p:option name="step-name" select="''"/>
  
  <p:import href="errorPI2svrl.xpl"/>
  <p:import href="http://transpect.io/calabash-extensions/rng-extension/xpl/rng-validate-to-PI.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  
  <tr:validate-with-rng-PI name="rng2pi">
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
    <p:with-option name="remove-srcpaths" select="$remove-srcpaths"/>
    <p:input port="schema">
      <p:pipe port="schema" step="validate-with-rng-svrl"/>
    </p:input>
  </tr:validate-with-rng-PI>
  
  <tr:store-debug>
    <p:with-option name="pipeline-step" select="concat('rngvalid/', /*/local-name(), '/with-PIs_2')">
      <p:pipe port="source" step="validate-with-rng-svrl"/>
    </p:with-option>
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
      
  <tr:errorPI2svrl name="errorPI2svrl" severity="error">
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
    <p:with-option name="group-by-srcpath" select="$group-by-srcpath"/>
    <p:with-option name="step-name" select="$step-name"/>
    <p:input port="errorPI2svrl-xsl">
      <p:pipe port="errorPI2svrl-xsl" step="validate-with-rng-svrl"/>
    </p:input>
  </tr:errorPI2svrl>
  
</p:declare-step>
