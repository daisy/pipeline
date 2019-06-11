<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:tr="http://transpect.io"
  exclude-inline-prefixes="#all" 
  version="1.0" 
  type="tr:patch-svrl" 
  name="patch-svrl">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml"> 
    <p>This step patches error report(s) into an HTML document 
      and provide an HTML report.</p> 
  </p:documentation>

  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" select="'status?enabled=false'"/>
  <p:option name="fail-on-error" select="'false'"/>
  <p:option name="max-errors-per-rule" required="false" select="'200'"/>
  <p:option name="severity-default-name" required="false" select="'no-role'"/>
  <p:option name="report-title" required="false" select="''"/>
  <p:option name="show-adjusted-srcpath" select="'yes'"/>
  <p:option name="discard-empty-schematron-outputs" select="'no'" required="false">
  	<p:documentation>If whole schematron outputs are empty their name is by default displayed. 
  		(Normally with an OK or ✓ message. To override that message you can overwrite the 
  		template named »l10n:message-empty« in your project's specific htmlreport svrl2xsl.xsl)
  		To avoid displaying those outputs at all you can set this option to »true« or »yes«.</p:documentation>
  </p:option>
  <p:option name="suppress-embedding" select="''">
    <p:documentation>Space-separated list of tokens. Available tokens are: image video script style audio object #all.
    The documentation in tr:html-embed-resources could be more up to date.</p:documentation>
  </p:option>
  <p:option name="max-base64-encoded-size-kb" select="1000">
    <p:documentation>
      Limit (KB) for embedded base64 data URIs. 
    </p:documentation>
  </p:option>

  <p:input port="source" primary="true">
    <p:documentation>An XML document with srcpath attributes. Typically an XHTML rendering.</p:documentation>
  </p:input>
  <p:input port="reports" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>SVRL outputs that carry <code>@tr:rule-family</code> attributes on their top-level
        elements or <code>c:errors</code> elements with try/catch results. Only <code>c:errors/c:error[@code]</code>
        errors will be visualized in the HTML report (i.e., they need a code attribute).</p>
    <p>Other attributes are <code>tr:step-name</code> (conversion step name that will be rendered below a message) and 
      <code>tr:include-location-in-msg</code>. The latter option will render the SVRL messages’ <code>@location</code> 
      attributes below the rendered messages. This is useful if the user should be able to fix the issues in the
      XML input and the exakt location is not obvious.</p></p:documentation>
  </p:input>
  <p:input port="params" kind="parameter" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A parameter set with the top-level element <code>c:param-set</code></p></p:documentation>
  </p:input>

  <p:output port="result" primary="true">
    <p:pipe step="remove-fallback" port="result"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The HTML report document containing the error messages.</p></p:documentation>
  </p:output>
  <p:output port="secondary" sequence="true">
    <p:documentation>messages-grouped-by-type.xml, linked-messages-grouped-by-srcpath.xml
    for further processing (e.g., list of all message types)</p:documentation>
    <p:pipe step="create-patch-xsl" port="secondary"/>
  </p:output>
  <p:output port="msgs">
    <p:pipe port="result" step="create-success-messages"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Receives a total of all kinds of messages. (<code>c:messages/c:message</code>)</p>
      <p>The format depends on the configuration parameter 'report-summary-components'.
      See a list of possible values in ../xsl/create-success-messages.xsl (variable
      $summary-component-vocabulary). The default should be 'prose' (human-readable)</p>
    </p:documentation>
  </p:output>
  <p:output port="severity-totals">
    <p:pipe port="result" step="severity-totals"/>
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A machine-readable error summary, like on the msgs port, but with report-summary-components 
        set to 'severity-totals'.</p></p:documentation>
  </p:output>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  <p:import href="http://transpect.io/xproc-util/html-embed-resources/xpl/html-embed-resources.xpl"/>

  <p:parameters name="paths">
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
  </p:parameters>

  <tr:simple-progress-msg name="start-msg" file="patch-svrl-start.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Patching messages into HTML rendering</c:message>
          <c:message xml:lang="de">Montiere die Meldungen in das HTML-Rendering</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <!--  * 
        * embed resources in the source HTML file.  
        * -->
  
  <tr:html-embed-resources name="html-embed-resources-before-delete">
    <p:input select="/html:html" port="source">
      <p:pipe port="source" step="patch-svrl"/>
    </p:input>
    <p:input port="catalog">
      <p:documentation>We need to pass the project’s catalog so that tr:html-embed-resources will be 
      able to resolve canonical URIs to resources. The catalog’s location can be given in a canonical
      URI since this will be resolved by the default XML catalog resolver.
      Should we take precautions for when there is no catalog at that location?</p:documentation>
      <p:document href="http://this.transpect.io/xmlcatalog/catalog.xml"/>
    </p:input>
    <p:with-option name="max-base64-encoded-size-kb" select="$max-base64-encoded-size-kb"/>
    <p:with-option name="exclude" select="$suppress-embedding"/>
    <p:with-option name="fail-on-error" select="$fail-on-error">
      <p:documentation>embed resources before local @xml:base attributes get lost</p:documentation>
    </p:with-option>
    <p:with-option name="debug" select="$debug"/>
  </tr:html-embed-resources>
  
  <p:delete name="filter-document" match="@xml:base">
    <p:documentation>Just in case that there are blank lines in front of the XHTML -- these
      will constitute an empty document by themselves. In addition, @xml:base attributes 
      will give a funny link click experience.</p:documentation>
  </p:delete>
  
  <tr:store-debug pipeline-step="htmlreports/filtered">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:sink/>

  <!--  * wrap and regroup reports.  
        * -->

  <p:wrap-sequence name="reports" wrapper="c:reports">
    <p:input port="source">
      <p:pipe port="reports" step="patch-svrl"/>
    </p:input>
  </p:wrap-sequence>
  
  <p:xslt name="reorder-messages-by-category" cx:depends-on="reports">
    <p:documentation>This XSLT will regroup the messages using a span in the asserts/reports. 
      The span's class used to regroup can be defined as te content of param name 'rule-category-span-class' 
      in the parameter set. (For example in a project-specific transpect-conf.xml)
      The span's content will appear as a heading in the html report.
      If it isn't defined or no such spans occur the reports document will be reproduced. 
      If not every assert/report has a span with that class the original rule-family is used.
      If there is no assert/report but the SVRL contains one or more title elements (in any namespace),
      then the tr:rule-family of this SVRL will be set to the title element content that matches the
      interface language. (We did not use the SVRL title attribute since the interface language is
      not known in certain steps that perform Schematron validation and the default XSLT Schematron
      will always fill the title attribute from the title element that comes last in document order.)  
    </p:documentation>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/regroup-messages-to-category.xsl"/>
    </p:input>
    <p:with-param name="discard-empty-schematron-outputs" select="($discard-empty-schematron-outputs, 'no')[1]"/>
  </p:xslt>
  
  <tr:store-debug pipeline-step="htmlreports/reports-regrouped">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:sink/>
  
  <!--  * load HTML template. The template is the base of 
        * the HTML report and the content is injected into 
        * the div with the id 'tr-content' 
        * -->
    
  <tr:load-cascaded name="load-template" filename="htmlreports/template/template.html">
    <p:with-option name="fallback" select="resolve-uri('../template/template.html')"/>
    <p:input port="paths">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:load-cascaded>
    
  <p:sink/>
    
  <tr:store-debug pipeline-step="htmlreports/template-loaded" extension="html" name="debug-load-template" cx:depends-on="load-template">
    <p:input port="source">
      <p:pipe port="result" step="load-template"/>
    </p:input>
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <!-- insert content html into template -->
  
  <p:insert match="//html:div[@id eq 'tr-content']" position="first-child" name="inject-body" cx:depends-on="load-template">
    <p:input port="insertion" select="/html:html/html:body/*">
      <p:pipe port="result" step="filter-document"/>
    </p:input>
  </p:insert>
  
  <!-- later, messages without srcpath are patched into this section -->
  
  <p:insert match="//html:div[@id eq 'tr-content']" position="first-child" name="create-element-for-orphaned-messages" cx:depends-on="inject-body">
    <p:input port="insertion">
      <p:inline>
        <div xmlns="http://www.w3.org/1999/xhtml" id="BC_orphans"><p srcpath="BC_orphans"/><p srcpath=""/></div>
      </p:inline>
    </p:input>
  </p:insert>
  
  <!-- insert html head of content file into template -->
  
  <p:insert match="/html:html/html:head" position="last-child" name="inject-head" cx:depends-on="create-element-for-orphaned-messages">
    <p:input port="insertion" 
      select="/html:html/html:head/(html:link[@type eq 'text/css'] 
                                    | html:style
                                    | html:meta[@name = 'source-dir-uri'])">
      <p:pipe port="result" step="filter-document"/>
    </p:input>
  </p:insert>
  
  <tr:store-debug pipeline-step="htmlreports/template-injected" extension="html">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <!-- and this is where the magic happens. all external resources are embedded via data uri -->
  
  <tr:html-embed-resources name="html-embed-resources" cx:depends-on="inject-head">
    <p:input port="catalog">
      <p:documentation>see above, at the other tr:html-embed-resources instance</p:documentation>
      <p:document href="http://this.transpect.io/xmlcatalog/catalog.xml"/>
    </p:input>
    <p:with-option name="fail-on-error" select="$fail-on-error">
      <p:documentation>sometimes resources such as CSS overrides in the content repository don't exist</p:documentation>
    </p:with-option>
    <p:with-option name="exclude" select="$suppress-embedding"/>
    <p:with-option name="debug" select="$debug"/>
  </tr:html-embed-resources>
  
  <tr:store-debug pipeline-step="htmlreports/template-with-data-uris" extension="html">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:sink/>

  <tr:store-debug pipeline-step="htmlreports/reports">
    <p:input port="source">
      <p:pipe step="reports" port="result"/>
    </p:input>
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:sink/>

  <tr:load-cascaded name="load-svrl2xsl" filename="htmlreports/svrl2xsl.xsl"
    fallback="http://transpect.io/htmlreports/xsl/svrl2xsl.xsl">
    <p:input port="paths">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:load-cascaded>

  <p:sink/>

  <p:xslt name="create-patch-xsl" cx:depends-on="load-svrl2xsl">
    <p:input port="source">
      <p:pipe step="reorder-messages-by-category" port="result">
        <p:documentation>The SVRL report.</p:documentation>
      </p:pipe>
    	<p:pipe step="html-embed-resources" port="result">
        <p:documentation>To be able to avoid some messages to be rendered in special sections of the XML 
          (for example sections that will be discarded later) an HTML @class 'bc_ignore' can be added to 
          the content. Those elements and its children will not carry messages in the htmlreport.</p:documentation>
      </p:pipe>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="load-svrl2xsl" port="result"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
    <p:with-param name="report-title" select="$report-title"/>
    <p:with-param name="severity-default-name" select="$severity-default-name"/>
    <p:with-param name="max-errors-per-rule" select="$max-errors-per-rule"/>
    <p:with-param name="show-adjusted-srcpath" select="$show-adjusted-srcpath"/>
  </p:xslt>

  <tr:store-debug pipeline-step="htmlreports/patch-svrl" extension="xsl">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:sink/>

  <!-- for-each only used for debugging -->

  <p:for-each>
    <p:iteration-source>
      <p:pipe step="create-patch-xsl" port="secondary"/>
    </p:iteration-source>
    
    <tr:store-debug>
      <p:with-option name="pipeline-step" select="concat('htmlreports', replace(base-uri(), '^.+(/.+?).xml', '$1'))"/>
      <p:with-option name="active" select="$debug"/>
      <p:with-option name="base-uri" select="$debug-dir-uri"/>
    </tr:store-debug>
    
    <p:sink/>
    
  </p:for-each>
  
  <p:xslt name="create-fallback" initial-mode="create-fallback">
    <p:input port="source">
      <p:pipe step="html-embed-resources" port="result"/>
    </p:input>
    <p:input port="stylesheet">
      <p:pipe step="create-patch-xsl" port="result"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
  </p:xslt>

  <tr:store-debug pipeline-step="htmlreports/1.create-fallback" extension="xhtml">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:xslt name="patch">
    <p:input port="stylesheet">
      <p:pipe step="create-patch-xsl" port="result"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
  </p:xslt>
  
  <tr:store-debug pipeline-step="htmlreports/2.patch-main" extension="xhtml">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:xslt name="remove-fallback" initial-mode="remove-fallback">
    <p:input port="stylesheet">
      <p:pipe step="create-patch-xsl" port="result"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
  </p:xslt>
  
  <tr:store-debug pipeline-step="htmlreports/3.remove-fallback" extension="xhtml">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <p:sink/>

  <p:identity name="messages-grouped-by-type">
    <p:input port="source" select="/*[ends-with(base-uri(), 'messages-grouped-by-type.xml')]">
      <p:pipe port="secondary" step="create-patch-xsl"/>
    </p:input>
  </p:identity>

  <p:xslt name="create-success-messages">
    <p:input port="stylesheet">
      <p:document href="../xsl/create-success-messages.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="patch-svrl"/>
    </p:input>
  </p:xslt>

  <tr:simple-progress-msg name="success-msg" file="patch-svrl-success.txt">
    <p:input port="msgs">
      <p:pipe port="result" step="create-success-messages"/>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>

  <p:sink name="s10"/>
  
  <p:xslt name="severity-totals">
    <p:input port="source">
      <p:pipe port="result" step="messages-grouped-by-type"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/create-success-messages.xsl"/>
    </p:input>
    <p:with-param name="report-summary-components" select="'severity-totals'"/>
  </p:xslt>

  <p:sink name="s11"/>

  <!-- should be stored via output port (otherwise it might interfere with svn.mk) -->
  <!--<p:try name="try-store-msg-summary">
    <p:group>
      <p:store name="store-msg-summary" omit-xml-declaration="false" indent="true">
        <p:with-option name="href"
          select="concat(/c:param-set/c:param[@name eq 's9y1-path']/@value,
                        'report/',
                        /c:param-set/c:param[@name eq 'basename']/@value,
                        '.summary.xml')">
          <p:pipe port="result" step="paths"/>
        </p:with-option>
      </p:store>    
    </p:group>
    <p:catch>
      <p:sink name="s11"/>
    </p:catch>
  </p:try>-->

</p:declare-step>
