<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="check-styles"
  type="tr:check-styles">
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:option name="status-dir-uri" required="false" select="'status?enabled=false'"/>
  <p:option name="active" required="false" select="'true'"/>
  <p:option name="step-name" required="false" select="'sch_styles'"/>
  <p:option name="cssa" select="'styles/simple-template.cssa.xml'">
    <p:documentation>Name of the CSSa file with the style definitions (for load-cascaded).</p:documentation>
  </p:option>
  <p:option name="differentiate-by-style" required="false" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>This option will create a distinct error class for each missing style name. 
      In order to use this option, you need to create a &lt;span class="style-name"> in
      your schematron rule in your adaption’s styles.sch.xml, like this:</p>
    <pre><code>
    &lt;rule context="*[count($template-style-names) gt count($default-style-names)][@role][not(self::dbk:keywordset or self::dbk:keyword or self::dbk:tab)]">
      &lt;let name="base-role" value="replace(@role, '(_-_|[~&#x2dc;]).+$', '')" />
      &lt;assert test="($base-role = $template-style-names) or (some $i in $template-style-regexes satisfies (matches(@role, $i)))" role="warning" id="sch_styles_undefined" diagnostics="sch_styles_undefined_de">
        &lt;span class="srcpath">&lt;xsl:value-of select="concat($base-dir, (@srcpath, ancestor::*[@srcpath][1]/@srcpath, .//@srcpath)[1])"/>&lt;/span>
        Style '&lt;span class="style-name">&lt;xsl:value-of select="$base-role"/>&lt;/span>' not found in allowed templates catalog 
        '&lt;value-of select="string-join(tokenize(base-uri($template-styles[1]), '/')[position() ge last() - 4],'/')"/>'.&lt;/assert>
    &lt;/rule></code></pre>
    </p:documentation>
  </p:option> 
   <p:option name="load-cssa-cascade" required="false" select="false()">
    <p:documentation>If this option is set true() the whole cssa cascade will be loaded</p:documentation>
  </p:option>
  
  <p:input port="source" primary="true"/>
  <p:input port="html-in" >
    <p:empty/>
  </p:input>
  <p:input port="parameters" kind="parameter" primary="true"/>
  <p:output port="result" primary="true">
    <p:pipe port="source" step="check-styles"/>
  </p:output>
  <p:output port="doc-and-template-styles">
    <p:pipe port="result" step="sch"/>
  </p:output>
  <p:output port="report" sequence="true">
    <p:pipe port="result" step="success-msg"/>
  </p:output>
  <p:output port="schema" sequence="true">
    <p:pipe port="schema" step="sch"/>
  </p:output>
  <p:output port="styledoc">
    <p:pipe port="result" step="styledoc-embed-resources"/>
  </p:output>
  
  <p:output port="htmlreport" sequence="true">
    <p:documentation>Please note that the HTML report will not reflect the differentiated 
    styles. It will only do so if the report is rendered by a subsequent step. This is because 
    the svrl in the output will be patched after the tr:validate-with-schematron step.</p:documentation>
    <p:pipe port="htmlreport" step="sch"/>
  </p:output>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/htmlreports/xpl/validate-with-schematron.xpl"/>
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  <p:import href="http://transpect.io/xproc-util/html-embed-resources/xpl/html-embed-resources.xpl"/>
  
  <tr:simple-progress-msg name="start-msg" file="check-styles-start.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Starting Schematron check of used styles against list of permissible styles (look for 'styles' messages in the report)</c:message>
          <c:message xml:lang="de">Beginne Schematron-Prüfung der verwendeten Formatvorlagen gegen Positivliste (entspr. Meldungen tauchen unter der Kategorie 'styles' auf)</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>

  <p:group name="template-styles">
   <p:output port="result" primary="true" sequence="true">
     <p:pipe port="result" step="load-cssa"/>
   </p:output>
     <p:choose name="load-cssa">
       <p:when test="$load-cssa-cascade = true()">
         <p:output port="result" primary="true" sequence="true">
           <p:pipe port="result" step="load-whole-cssa-cascade"/>
          </p:output>
         <tr:load-whole-cascade name="load-whole-cssa-cascade">
           <p:with-option name="filename" select="$cssa"/>
           <p:input port="paths">
             <p:pipe port="parameters" step="check-styles"/>
           </p:input>
         </tr:load-whole-cascade>
         <p:sink/>
       </p:when>
       <p:otherwise>
          <p:output port="result" primary="true">
           <p:pipe port="result" step="load-cssa-cascade"/>
         </p:output>
         <tr:load-cascaded name="load-cssa-cascade">
           <p:with-option name="filename" select="$cssa"/>
           <p:input port="paths">
             <p:pipe port="parameters" step="check-styles"/>
           </p:input>
           <p:with-option name="debug" select="$debug"/>
           <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
         </tr:load-cascaded>
         <p:sink/>
       </p:otherwise>
     </p:choose>
  </p:group> 
  
  <p:sink/>
  
  <p:wrap-sequence name="doc-and-template-styles" wrapper="tr:doc-and-template-styles">
    <p:input port="source">
      <p:pipe port="source" step="check-styles"/>
      <p:pipe port="result" step="template-styles"/>
    </p:input>
  </p:wrap-sequence>
  
  <tr:store-debug pipeline-step="styles/doc-and-template-styles">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

  <tr:validate-with-schematron name="sch">
    <p:input port="html-in">
      <p:pipe port="html-in" step="check-styles"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="parameters" step="check-styles"/>
    </p:input>
    <p:with-param name="family" select="'styles'"/>
    <p:with-param name="step-name" select="$step-name"/>
    <p:with-param name="fallback-uri" select="'http://transpect.io/htmlreports/sch/styles.sch.xml'"/>
    <p:with-option name="active" select="$active"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:validate-with-schematron>

  <p:sink/>
    
  <p:xslt name="styledoc" template-name="main">
    <p:input port="source">
      <p:pipe port="result" step="doc-and-template-styles"/>
    </p:input>
    <p:input port="stylesheet" >
      <p:document href="../xsl/styledoc.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:with-param name="function" select="'prescriptive'"/>
    <p:with-param name="cssa" select="$cssa"/>
  </p:xslt>

  <tr:store-debug pipeline-step="styles/styledoc" extension="xhtml">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <tr:html-embed-resources name="styledoc-embed-resources">
    <p:input port="catalog">
      <p:document href="http://this.transpect.io/xmlcatalog/catalog.xml"/>
    </p:input>
<!--    <p:with-option name="exclude" select="$suppress-embedding"/>-->
    <p:with-option name="fail-on-error" select="'false'"/>
    <p:with-option name="debug" select="$debug"/>
  </tr:html-embed-resources>

  <p:sink/>

  <p:split-sequence initial-only="true" test="true()">
    <p:documentation>tr:validate-with-schematron may produce multiple reports. We know that we put in only one document, so we
    only select the first report.</p:documentation>
    <p:input port="source">
      <p:pipe port="report" step="sch"/>
    </p:input>
  </p:split-sequence>

  <p:choose name="potentially-differentiate-by-style">
    <p:when test="$differentiate-by-style = 'true'">
      <p:xslt name="add-style-names-to-error-codes">
        <p:input port="parameters"><p:empty/></p:input>
        <p:input port="stylesheet">
          <p:inline>
            <xsl:stylesheet version="2.0">
              <xsl:template match="* | @*">
                <xsl:copy>
                  <xsl:apply-templates select="@*, node()"/>
                </xsl:copy>
              </xsl:template>
              <xsl:template match="svrl:failed-assert/@id[. = 'sch_styles_undefined']">
                <xsl:attribute name="{name()}" select="../svrl:text/s:span[@class = 'style-name']"/>
              </xsl:template>
            </xsl:stylesheet>
          </p:inline>
        </p:input>
      </p:xslt>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>

  <tr:simple-progress-msg name="success-msg" file="check-styles-success.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Successfully finished style checks</c:message>
          <c:message xml:lang="de">Formatvorlagenprüfung erfolgreich abgeschlossen</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <p:sink/>

</p:declare-step>
