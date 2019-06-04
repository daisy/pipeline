<?xml version="1.0" encoding="utf-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:tr="http://transpect.io"
  version="1.0">

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/htmlreports/xpl/patch-svrl.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/schematron/xpl/oxy-schematron.xpl"/>
  
  <p:declare-step name="validate-with-schematron" type="tr:validate-with-schematron">

    <p:option name="debug" required="false" select="'no'"/>
    <p:option name="debug-dir-uri" select="'debug'"/>
    <p:option name="status-dir-uri" select="'status?enabled=false'"/>
    <p:option name="phase" required="false" select="'#ALL'"/>
    <p:option name="active" required="false" select="'true'"/>
  	<p:option name="schematron-rule-msg" select="'no'">
  		<p:documentation>Prints a status message with the Id of the currently fired schematron report or assert.</p:documentation>
  	</p:option>
    
    <p:input port="source" primary="true" sequence="true">
      <p:documentation>If multiple source documents are given, a report will be created for every document.</p:documentation>
    </p:input>
    <p:input port="html-in" sequence="true">
      <p:documentation>One or zero documents. If zero, no attempt is made to patch the results into the HTML</p:documentation>
    </p:input>
    <p:input port="parameters" kind="parameter" primary="true">
      <p:documentation>The parameter 'family' is used for looking up and assembling the Schematron files from the 
      cascaded configuration.</p:documentation>
    </p:input>
    <p:output port="result" primary="true" sequence="true"><p:pipe port="result" step="actually-do-something"/></p:output>
    <p:output port="report" sequence="true"><p:pipe port="report" step="actually-do-something"/></p:output>
    <p:output port="htmlreport" sequence="true"><p:pipe port="htmlreport" step="actually-do-something"/></p:output>
    <p:output port="schema" sequence="true">
      <p:documentation>A compound Schematron document that was assembled by loading the whole cascade of Schematrons for
        the given family and merging it so that only the most specific pattern for a given pattern ID wins.</p:documentation>
      <p:pipe step="actually-do-something" port="assemble-schematron"/>
    </p:output>

    <p:import href="assemble-schematron.xpl"/>
    <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
    
    <p:choose name="actually-do-something">
      <p:xpath-context>
        <p:empty/>
      </p:xpath-context>
      <p:when test="$active = ('true', 'yes')">
        <p:output port="report" sequence="true">
          <p:pipe step="validate-if-assembled-has-pattern" port="report"/>
        </p:output>
        <p:output port="result" primary="true" sequence="true">
          <p:pipe step="validate-if-assembled-has-pattern" port="result"/>
        </p:output>
        <p:output port="htmlreport" sequence="true">
          <p:pipe step="validate-if-assembled-has-pattern" port="htmlreport"/>
        </p:output>
        <p:output port="assemble-schematron">
          <p:pipe step="assemble-schematron" port="result"/>
        </p:output>

        <p:parameters name="consolidate-params">
          <p:input port="parameters">
            <p:pipe port="parameters" step="validate-with-schematron"/>
          </p:input>
        </p:parameters>
       
        <p:string-replace match="c:family" name="replace-schematron-family-name-in-start-msg">
          <p:with-option name="replace" select="concat('''', /c:param-set/c:param[@name eq 'family']/@value, '''')">
            <p:pipe port="result" step="consolidate-params"/>
          </p:with-option>
          <p:input port="source">
            <p:inline>
              <c:messages>
                <c:message xml:lang="en">Starting '<c:family>family</c:family>' Schematron checks</c:message>
                <c:message xml:lang="de">Beginne Schematron-Prüfungen der Kategorie '<c:family>family</c:family>'</c:message>
              </c:messages>
            </p:inline>
          </p:input>
        </p:string-replace>
       
        <tr:simple-progress-msg name="start-msg" >
          <p:with-option name="file" select="concat('validate-with-schematron_', /c:param-set/c:param[@name eq 'family']/@value,'_start.txt')">
            <p:pipe port="result" step="consolidate-params"/>
          </p:with-option>
          <p:input port="msgs">
            <p:pipe port="result" step="replace-schematron-family-name-in-start-msg"/>
          </p:input>
          <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
        </tr:simple-progress-msg>

        <p:sink/>

        <tr:assemble-schematron name="assemble-schematron">
          <p:input port="paths">
            <p:pipe port="result" step="consolidate-params"/>
          </p:input>
          <p:with-option name="debug" select="$debug"/>
          <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
          <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
        	<p:with-option name="schematron-rule-msg" select="$schematron-rule-msg"/>
        </tr:assemble-schematron>
        
        <p:sink/>
        
        <p:choose name="validate-if-assembled-has-pattern">
          <p:xpath-context>
            <p:pipe port="result" step="assemble-schematron"/>
          </p:xpath-context>
          <p:when test="//s:pattern">
            <p:output port="report" sequence="true"><p:pipe step="pattern-in-assembled" port="report"/></p:output>
            <p:output port="result" primary="true" sequence="true"><p:pipe step="pattern-in-assembled" port="result"/></p:output>
            <p:output port="htmlreport" sequence="true"><p:pipe step="pattern-in-assembled" port="htmlreport"/></p:output>
            <tr:validate-with-schematron2 name="pattern-in-assembled">
              <p:with-option name="debug" select="$debug"/>
              <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
              <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
              <p:with-option name="phase" select="$phase"/>
              <p:input port="source"><p:pipe port="source" step="validate-with-schematron"/></p:input>
              <p:input port="html-in"><p:pipe port="html-in" step="validate-with-schematron"/></p:input>
              <p:input port="schema"><p:pipe port="result" step="assemble-schematron"/></p:input>
              <p:input port="parameters"><p:pipe port="result" step="consolidate-params"/></p:input>
            </tr:validate-with-schematron2>
          </p:when>
          <p:otherwise>
            <p:output port="report" sequence="true"><p:empty/></p:output>
            <p:output port="result" primary="true" sequence="true"><p:pipe step="id" port="result"/></p:output>
            <p:output port="htmlreport" sequence="true"><p:pipe step="validate-with-schematron" port="html-in"/></p:output>
            <p:identity name="id">
              <p:input port="source"><p:pipe port="source" step="validate-with-schematron"/></p:input>
            </p:identity>
          </p:otherwise>
        </p:choose>

        <p:sink/>

        <p:string-replace match="c:family" name="replace-schematron-family-name-in-success-msg">
          <p:with-option name="replace" select="concat('''', /c:param-set/c:param[@name eq 'family']/@value, '''')">
            <p:pipe port="result" step="consolidate-params"/>
          </p:with-option>
          <p:input port="source">
            <p:inline>
              <c:messages>
                <c:message xml:lang="en">Successfully finished '<c:family>family</c:family>' Schematron checks</c:message>
                <c:message xml:lang="de">Schematron-Prüfungen der Kategorie '<c:family>family</c:family>' erfolgreich abgeschlossen</c:message>
              </c:messages>
            </p:inline>
          </p:input>
        </p:string-replace>
        
        <tr:simple-progress-msg name="success-msg" >
          <p:with-option name="file" select="concat('validate-with-schematron_', /c:param-set/c:param[@name eq 'family']/@value,'_success.txt')">
            <p:pipe port="result" step="consolidate-params"/>
          </p:with-option>
          <p:input port="msgs">
            <p:pipe port="result" step="replace-schematron-family-name-in-success-msg"/>
          </p:input>
          <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
        </tr:simple-progress-msg>
        <p:sink/>
      </p:when>
      
      <p:otherwise>
        <p:output port="report" sequence="true"><p:empty/></p:output>
        <p:output port="result" primary="true"><p:pipe step="id" port="result"/></p:output>
        <p:output port="htmlreport" sequence="true"><p:pipe step="validate-with-schematron" port="html-in"/></p:output>
        <p:output port="assemble-schematron"><p:empty/></p:output>
        <p:identity name="id">
          <p:input port="source"><p:pipe port="source" step="validate-with-schematron"/></p:input>
        </p:identity>
        
      </p:otherwise>
    </p:choose>
    
  </p:declare-step>
  
  <p:declare-step type="tr:validate-with-schematron2" name="validate-with-schematron2">
    <p:option name="debug" required="false" select="'no'"/>
    <p:option name="debug-dir-uri"/>
    <p:option name="status-dir-uri"/>
    <p:option name="phase" select="'#ALL'"/>
    
    <p:input port="source" primary="true" sequence="true"/>
    <p:input port="html-in" sequence="true"></p:input>
    <p:input port="schema"/>
    <p:input port="parameters" kind="parameter" primary="true"/>
    <p:output port="result" primary="true" sequence="true">
      <p:pipe port="source" step="validate-with-schematron2"/>
    </p:output>
    <p:output port="report" sequence="true">
      <p:pipe port="partial-reports" step="validate-loop"/>
    </p:output>
    <p:output port="htmlreport" sequence="true">
      <p:pipe port="result" step="conditionally-patch-html"/>
    </p:output>
    
    <p:parameters name="consolidate-params">
      <p:input port="parameters"><p:pipe port="parameters" step="validate-with-schematron2"/></p:input>
    </p:parameters>
    
    <p:for-each name="validate-loop">
      <p:iteration-source>
        <p:pipe port="source" step="validate-with-schematron2"/>
      </p:iteration-source>
      <p:output port="partial-reports">
        <p:pipe port="result" step="copy-titles-to-svrl"/>
      </p:output>
      <tr:oxy-validate-with-schematron name="sch" assert-valid="false">
        <p:input port="schema">
          <p:pipe port="schema" step="validate-with-schematron2"/>
        </p:input>
        <p:input port="source">
          <p:pipe port="current" step="validate-loop"/>
        </p:input>
        <p:input port="parameters">
          <p:pipe port="result" step="consolidate-params"/>
          <!--<p:pipe port="parameters" step="validate-with-schematron2"/>-->
        </p:input>
        <p:with-option name="phase" select="$phase">
          <p:empty/>
        </p:with-option>
        <p:with-param name="allow-foreign" select="'true'">
          <p:empty/>
        </p:with-param>
      </tr:oxy-validate-with-schematron>
                
      <p:sink/>

      <p:add-attribute match="/*" attribute-name="tr:step-name">
        <p:with-option name="attribute-value" select="/c:param-set/c:param[@name eq 'step-name']/@value">
          <p:pipe port="result" step="consolidate-params"/>
        </p:with-option>
        <p:input port="source">
          <p:pipe port="report" step="sch"/>
        </p:input>
      </p:add-attribute>
      
      <p:add-attribute match="/*" attribute-name="tr:rule-family" name="add-family-attribute">
        <p:with-option name="attribute-value" select="/c:param-set/c:param[@name eq 'family']/@value">
          <p:pipe port="result" step="consolidate-params"/>
        </p:with-option>
      </p:add-attribute>
      
      <p:insert name="copy-titles-to-svrl" match="/*" position="first-child">
        <p:documentation>We will use the (potentially localized) title elements for grouping the messages according
        to their categories, where categories can be assigned to individual assert/report and diagnostic messages 
        either by span.category or by the schema’s title element that best matches the user interface language.
        We cannot use the SVRL’s title attribute since it only contains the contents of the last title element
        (in document order) of the underlying schema.</p:documentation>
        <p:input port="insertion" select="/*/*:title">
          <p:pipe port="schema" step="validate-with-schematron2"/>
        </p:input>
      </p:insert>

      <tr:store-debug extension="svrl.xml">
        <p:with-option name="pipeline-step" 
          select="concat('schematron/', /c:param-set/c:param[@name = 'basename']/@value, '.', /c:param-set/c:param[@name = 'family']/@value)">
          <p:pipe port="result" step="consolidate-params"/>
        </p:with-option>
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
      </tr:store-debug>
      <p:sink/>
    </p:for-each>    

    <p:sink/>
    
    <p:for-each name="conditionally-patch-html">
      <p:documentation>This is no actual loop. It is just for patching the HTML input
      if there is an HTML document on the html-in port, and to do nothing if there isn't.</p:documentation>
      <p:iteration-source>
        <p:pipe port="html-in" step="validate-with-schematron2"/>
      </p:iteration-source>
      <p:output port="result" sequence="true">
        <p:pipe port="result" step="patch"/>
      </p:output>
      
      <p:sink/>
      <tr:patch-svrl name="patch">
        <p:input port="reports">
          <p:pipe step="validate-loop" port="partial-reports"/>
        </p:input>
        <p:input port="source">
          <p:pipe step="conditionally-patch-html" port="current"/>
        </p:input>
        <p:with-option name="debug" select="$debug"/>
        <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
        <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
        <p:input port="params">
          <p:pipe port="parameters" step="validate-with-schematron2"/>
        </p:input>
      </tr:patch-svrl>
    </p:for-each>

    <p:sink/>

  </p:declare-step>
</p:library>
