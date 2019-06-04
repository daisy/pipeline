<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io" 
  xmlns:hub2htm="http://transpect.io/hub2htm"
  exclude-inline-prefixes="#all"
  version="1.0"
  name="hub2html"
  type="hub2htm:convert">

  <p:documentation>IMPORTANT: If you are already invoking this step without a paths
  port, your pipeline probably won’t work any more. Please add the following connection
  to the invocation:
    &lt;p:input port="paths"&gt;&lt;p:empty/&gt;&lt;/p:input&gt;
  </p:documentation>

  <p:option name="debug" select="'yes'"/> 
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status'"/>
  
  <p:option name="target" select="'EPUB2'"/>
  <p:option name="sections" select="'no'">
    <p:documentation>Create HTML sections from DocBook hierarchies.</p:documentation>
  </p:option>
	<p:option name="filename-driver" required="false" select="'hub2html/hub2html'"/>

  <p:input port="source" primary="true" select="/*">
    <p:documentation>A Hub 1.1+ document</p:documentation>
  </p:input>
  <p:input port="paths" sequence="true">
    <p:documentation>If you don’t want to use the tr:load-cascaded mechanism but 
    rather use the default XSLT, you can submit <p:empty/> to this port.</p:documentation>
  </p:input>
  <p:input port="other-params" sequence="true" kind="parameter" primary="true"/>
    
  <p:output port="result" primary="true"/>
  <p:output port="report" sequence="true">
    <p:pipe port="report" step="default"/>
    <p:pipe port="report" step="css"/>
    <p:pipe port="report" step="lists"/>
    <p:pipe port="report" step="cals2html"/>
    <p:pipe port="report" step="figures-equations"/>
    <p:pipe port="report" step="references"/>
    <p:pipe port="report" step="remove-ns"/>
  </p:output>  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/xslt-mode/xpl/xslt-mode.xpl"/>
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  
  <tr:simple-progress-msg name="start-msg" file="hub2html-start.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Starting HTML rendering of flat Hub XML</c:message>
          <c:message xml:lang="de">Beginne Rendering von flachem Hub XML als HTML</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <p:sink/>
  
  <p:parameters name="params">
    <p:input port="parameters">
      <p:pipe step="hub2html" port="paths"/>
      <p:pipe step="hub2html" port="other-params"/>
    </p:input>
  </p:parameters>
  
  <tr:load-cascaded name="lc" required="no" fallback="http://transpect.io/hub2html/xsl/hub2html.xsl">
    <p:input port="paths">
      <p:pipe port="paths" step="hub2html"/>
    </p:input>
    <p:with-option name="filename" select="concat(($filename-driver, 'hub2html/hub2html')[1], '.xsl')"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:load-cascaded>
  
  <p:sink/>

  <tr:xslt-mode msg="yes" mode="hub2htm-default" name="default">
    <p:input port="source">
      <p:pipe port="source" step="hub2html"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm1'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  
  <tr:xslt-mode msg="yes" mode="hub2htm:css" name="css">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm2'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  
  <tr:xslt-mode msg="yes" mode="hub2htm-lists" name="lists">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm3'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  
  <tr:xslt-mode msg="yes" mode="hub2htm-cals2html" name="cals2html">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm4'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  
  <tr:xslt-mode msg="yes" mode="hub2htm-figures-equations" name="figures-equations">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm5'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  
  <tr:xslt-mode msg="yes" mode="hub2htm-references" name="references">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm6'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  

  <tr:xslt-mode msg="yes" mode="hub2htm-remove-ns" name="remove-ns">
    <p:input port="parameters">
      <p:pipe step="params" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="lc" port="result"/>
    </p:input>
    <p:input port="models">
      <p:empty/>
    </p:input>
    <p:with-option name="prefix" select="'hub2html/hub2htm7'"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:xslt-mode>
  

  <tr:simple-progress-msg name="success-msg" file="hub2html-success.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Successfully rendered flat Hub XML as HTML</c:message>
          <c:message xml:lang="de">Rendering von flachem Hub XML als HTML erfolgreich abgeschlossen</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
</p:declare-step>
