<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"   
  xmlns:tr="http://transpect.io"
  version="1.0"
  type="tr:assemble-schematron"
  name="assemble-schematron">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    This step takes a cascade path parameter file as input and looks for a directory entitled <code>schematron</code>
    in each cascade level directory. The Schematron files within these directories are loaded and assembled. The most specific
    Schematron patterns override ones from more general cascade levels.
  </p:documentation>
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status?enabled=false'"/>
	<p:option name="schematron-rule-msg" select="'no'">
		<p:documentation>Prints a status message with the Id of the currently fired schematron report or assert.</p:documentation>
	</p:option>
  
  <p:input port="paths" kind="parameter" primary="true"/>
  <p:output port="result" primary="true">
    <p:pipe port="result" step="xslt"/>
  </p:output>
  <p:output port="report" primary="false" sequence="true">
    <p:pipe port="result" step="choose-debug"/>
  </p:output>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="validate-with-rng.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
  
  <p:parameters name="cons">
    <p:input port="parameters">
      <p:pipe port="paths" step="assemble-schematron"/>
    </p:input>
  </p:parameters>
  
  <p:xslt name="xslt">
    <p:input port="source">
      <p:pipe step="cons" port="result"/>
      <p:document href="http://this.transpect.io/xmlcatalog/catalog.xml"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/assemble-schematron.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="result" step="cons"/>
    </p:input>
    <p:with-param name="debug" select="$debug"/>
  	<p:with-param name="schematron-rule-msg" select="$schematron-rule-msg"/>    
  </p:xslt>
  
  <p:add-attribute attribute-name="queryBinding" attribute-value="xslt2" match="/*"/>
  
  <tr:store-debug>
    <p:with-option name="pipeline-step" select="concat('schematron/', /c:param-set/c:param[@name eq 'family']/@value)" >
      <p:pipe step="cons" port="result"/>
    </p:with-option>
    <p:with-option name="extension" select="'sch'"/>
    <p:with-option name="active" select="$debug" />
    <p:with-option name="base-uri" select="$debug-dir-uri" />
  </tr:store-debug>
  
  <p:choose name="choose-debug">
    <p:when test="$debug eq 'yes'">
      <p:output port="result" sequence="true">
        <p:pipe port="report" step="validate-with-rng"/>
      </p:output>
      
      <tr:validate-with-rng-svrl name="validate-with-rng">
        <p:with-option name="debug" select="$debug"/>
        <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
        <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
        <p:input port="schema">
          <p:document href="../schema/iso-schematron/1.0/rng/iso-schematron.rng"/>
        </p:input>
      </tr:validate-with-rng-svrl>
      
      <tr:store-debug>
        <p:input port="source">
          <p:pipe port="report" step="validate-with-rng"/>
        </p:input>
        <p:with-option name="pipeline-step" 
          select="concat('schematron/', /c:param-set/c:param[@name eq 'family']/@value, '-sch-validation-errors')" >
          <p:pipe step="cons" port="result"/>
        </p:with-option>
        <p:with-option name="active" select="$debug" />
        <p:with-option name="base-uri" select="$debug-dir-uri" />
      </tr:store-debug>
      
      <p:sink/>
      
    </p:when>
    <p:otherwise>
      <p:output port="result" sequence="true">
        <p:empty/>
      </p:output>
      
      <p:sink/>    
      
    </p:otherwise>
  </p:choose>
  
  
</p:declare-step>
