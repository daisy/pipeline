<?xml version="1.0" encoding="utf-8"?>
<p:declare-step 
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:tr="http://transpect.io"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl" 
  version="1.0"
  name="dtp"
  type="tr:dynamic-transformation-pipeline">
  
  <p:documentation>This encapsulates the dynamic loading of an XSLT stylesheet and an XPL pipeline and execution of the
    pipeline. Dynamic loading means that the most specific (specificity tr: work, series, publisher, common) XSL and XPL
    files will be loaded. The pipeline usually consists of transformations with the same dynamically loaded stylesheet in different
    modes, according to the dynamically loaded pipeline (that consists of tr:xslt-mode steps). The pipeline may have multiple
    documents on the source and result ports. The transformation will be applied to each document in turn. 
    
    Additional input files that will be passed to each transformation may be sent to the additional-input port. They must be 
    wrapped in cx:document with the appropriate port name of the dynamically executed pipeline. Even if you don’t have additional 
    inputs, make sure to always include the additional-inputs port when invoking tr:dynamic-transformation-pipeline. Connect it to
    p:empty by default. Please note the difference between a sequence of source documents (will be transformed one by one) and a
    sequence of additional files (will be available to the dynamically loaded pipeline and will typically be used as additional
    input documents for some of the tr:xslt-mode steps therein, thus making it available via the default collection in each 
    transformation).
    
    There is a limitation that additional inputs are limited to single documents per port. If you have a sequence, you’ll have 
    to wrap it into a custom element before wrapping it in the cx:document.
    
    There is also an options port. You may submit options in a cx:options document with cx:option entries (with name and value
    attributes). These options will be merged with the debug and debug-dir-uri options and passed on to the dynamically loaded
    pipeline. Before preparing a cx:options document, check whether the options can be read in the dynamically invoked pipeline
    from the primary parameter port.
  </p:documentation>
  
  <p:option name="load" xml:id="load">
    <p:documentation>The base name of the .xsl and .xpl files to load, e.g., foo2bar/foo2bar,
      where foo2bar is the name of directories in the customization folders for publisher, series, etc.</p:documentation>
  </p:option>
  <p:option name="fallback-xsl" required="false" select="''">
    <p:documentation>Fallback URI to 'default' stylesheet file. Will be loaded when no customization is available 
      in the customization folders for publisher, series, etc. You have to use the URI located in [code repo]/xmlcatalog/catalog.xml
      together wie the path to the stylesheet. Example (evolve-hub): http://transpect.io/evolve-hub/xsl/evolve-hub.xsl</p:documentation>
  </p:option>
  <p:option name="fallback-xpl" required="false" select="''">
    <p:documentation>Fallback URI to 'default' pipeline file. Leave it empty when no .xpl exists.</p:documentation>
  </p:option>
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" required="false" select="'debug'"/>
  
  <p:input port="source" primary="true" sequence="true"/>
  <p:input port="additional-inputs" primary="false" sequence="true">
    <p:empty/>
    <p:documentation>Additional cx:document(s) whose port attribute(s) designate(s) the input port(s) of the dynamically evaluated XProc step</p:documentation>
  </p:input>
  <p:input port="options" sequence="true">
    <p:documentation>Options that will be passed to the pipeline, in a cx:options document</p:documentation>
  </p:input>
  <p:input port="paths" kind="parameter" primary="true"/>
  <p:output port="report" sequence="true">
    <p:documentation>A sequence of either svrl:schematron-report or c:errors documents.</p:documentation>
    <p:pipe port="report" step="iteration"/>
  </p:output>
  <p:output port="result" primary="true" sequence="true">
    <p:documentation>A sequence of documents</p:documentation>
    <p:pipe port="result" step="iteration"/>
  </p:output>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/cascade/xpl/load-cascaded.xpl"/>

  <p:variable name="calabash-eval-multidoc-bug" select="'true'[p:system-property('p:product-name') = 'XML Calabash']">
    <p:documentation>Work around the issue described here: https://lists.w3.org/Archives/Public/xproc-dev/2014Oct/0003.html
    and that keeps us from receiving a report port or other ports from cx:eval.</p:documentation>
  </p:variable>

  <p:parameters name="consolidate-params">
    <p:input port="parameters">
      <p:pipe port="paths" step="dtp"/>
    </p:input>
  </p:parameters>

  <p:wrap wrapper="cx:document" match="/">
    <p:input port="source">
      <p:pipe step="consolidate-params" port="result"/>
    </p:input>
  </p:wrap>
  <p:add-attribute name="parameters" attribute-name="port" attribute-value="parameters" match="/*"/>

  <p:sink/>

  <tr:load-cascaded name="load-stylesheet">
    <p:with-option name="filename" select="concat($load, '.xsl')"/>
    <p:with-option name="fallback" select="$fallback-xsl"/>
    <p:input port="paths">
      <p:pipe port="result" step="consolidate-params"/>
    </p:input>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:load-cascaded>
  
  <p:sink/>
  
  <p:wrap wrapper="cx:document" match="/">
    <!-- Workaround – primary output port of previous step not bound --> 
    <p:input port="source">
      <p:pipe port="result" step="load-stylesheet"/>  
    </p:input>
  </p:wrap>
  <p:add-attribute name="stylesheet" attribute-name="port" attribute-value="stylesheet" match="/*"/>
  
  <p:sink/> 
  
  <tr:load-cascaded name="pipeline0">
    <p:with-option name="filename" select="concat($load, '.xpl')"/>
    <p:with-option name="fallback" select="$fallback-xpl"/>
    <p:with-option name="debug" select="$debug"/>
    <p:with-option name="debug-dir-uri" select="$debug-dir-uri"/>
  </tr:load-cascaded>
  
  <p:sink>
    <p:documentation>Without this sink and the explicit rewiring, there was a spurious:
    "calabash ERR:Unbound primary output: [output result on pipeline0" error sometimes.</p:documentation>
  </p:sink>
  
  <p:choose name="pipeline">
    <p:when test="$calabash-eval-multidoc-bug = 'true'">
      <p:output port="result" primary="true"/>
      <p:xslt name="patch-pipeline">
        <p:input port="source">
          <p:pipe port="result" step="pipeline0"/>
        </p:input>
        <p:input port="stylesheet">
          <p:document href="../xsl/calabash-workaround-patch-dtp.xsl"/>
        </p:input>
        <p:input port="parameters">
          <p:empty/>
        </p:input>
      </p:xslt>
      <tr:store-debug name="store-patched-pipeline" extension="xpl">
        <p:with-option name="active" select="$debug"/>
        <p:with-option name="base-uri" select="$debug-dir-uri"/>
        <p:with-option name="pipeline-step" select="string-join(($load, 'patched-for-calabash'), '_')"/>
      </tr:store-debug>
    </p:when>
    <p:otherwise>
      <p:output port="result" primary="true"/>
      <p:identity>
        <p:input port="source">
          <p:pipe port="result" step="pipeline0"/>
        </p:input>
      </p:identity>
    </p:otherwise>
  </p:choose>
  
  <p:try name="validate-pipeline">
    <p:group>
      <p:validate-with-relax-ng  assert-valid="true">
        <p:input port="schema">
          <p:document href="http://www.w3.org/TR/xproc/schema/1.0/xproc.rng"/>
        </p:input>
        <p:input port="source">
          <p:pipe port="result" step="pipeline"/>
        </p:input>
      </p:validate-with-relax-ng>
      <p:sink/>
    </p:group>
    <p:catch name="failed">
      <cx:message>
        <p:with-option name="message" select="concat('Invalid dynamic transformation pipeline (or no pipeline present): ', base-uri(/*),
          '&#xa;Please look for error messages in ', resolve-uri('dynamic-transformation-pipeline.error.xml', $debug-dir-uri))">
          <p:pipe port="result" step="pipeline"/>
        </p:with-option>
      </cx:message>
      <p:store>
        <p:with-option name="href" select="resolve-uri('dynamic-transformation-pipeline.error.xml', $debug-dir-uri)"/>
        <p:input port="source">
          <p:pipe step="failed" port="error"/>  
        </p:input>
      </p:store>
      <p:store>
        <p:with-option name="href" select="resolve-uri(concat('dynamic-transformation-pipeline.error_', replace(base-uri(/*), '^.+/', '')), $debug-dir-uri)">
          <p:pipe step="pipeline" port="result"/>           
        </p:with-option>
        <p:input port="source">
          <p:pipe step="pipeline" port="result"/>  
        </p:input>
      </p:store>
    </p:catch>
  </p:try>
  
  <p:xslt name="options" template-name="main">
    <p:with-param name="debug" select="$debug"/>
    <p:with-param name="debug-dir-uri" select="$debug-dir-uri"/>
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="source">
      <p:pipe step="dtp" port="options"/> 
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:param name="debug" as="xs:string"/>
          <xsl:param name="debug-dir-uri" as="xs:string"/>
          <xsl:template name="main">
            <cx:options>
              <cx:option name="debug" value="{$debug}"/>
              <cx:option name="debug-dir-uri" value="{$debug-dir-uri}"/>
              <xsl:sequence select="collection()/cx:options/cx:option"/>
            </cx:options>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

  <p:sink/>

  <p:for-each name="iteration">
    <p:output port="result" primary="true" sequence="true">
      <p:pipe port="result" step="result-and-report"/>
    </p:output>
    <p:output port="report" sequence="true">
      <p:pipe port="report" step="result-and-report"/>
    </p:output>
    <p:iteration-source>
      <p:pipe port="source" step="dtp"/>
    </p:iteration-source>
    <p:wrap wrapper="cx:document" match="/">
      <p:input port="source">
        <p:pipe step="iteration" port="current"/>
      </p:input>
    </p:wrap>
    <p:add-attribute name="source" attribute-name="port" attribute-value="source" match="/*"/>
    
    <p:sink/>
    
    <cx:eval name="eval" detailed="true">
      <p:input port="pipeline" xml:id="eval-pipeline">
        <p:pipe port="result" step="pipeline"/>
      </p:input>
      <p:input port="source">
        <p:pipe port="result" step="stylesheet"/>
        <p:pipe port="result" step="source"/>
        <p:pipe port="result" step="parameters"/>
        <p:pipe port="additional-inputs" step="dtp"/>
      </p:input>
      <p:input port="options">
        <p:pipe port="result" step="options"/>
      </p:input>
    </cx:eval>

    <p:unwrap match="/cx:document[@port eq 'result']" name="unwrap-result"/>
    
    <p:choose name="result-and-report">
      <p:when test="$calabash-eval-multidoc-bug = 'true'">
        <p:output port="result" sequence="true" primary="true">
          <p:pipe port="matched" step="split"/>
        </p:output>
        <p:output port="report" sequence="true">
          <p:pipe port="not-matched" step="split"/>
        </p:output>
        <p:split-sequence test="not(/c:errors | /svrl:schematron-report)" name="split">
          <p:documentation>We just assume in this workaround that all c:errors and svrl:schematron-report documents 
            belong to the report output port.</p:documentation>
          <p:input port="source" select="/c:wrapper/*"/>
        </p:split-sequence>
        <p:sink/>
      </p:when>
      <p:otherwise>
        <p:output port="result" primary="true" sequence="true">
          <p:pipe port="result" step="unwrap-result"/>
        </p:output>
        <p:output port="report" sequence="true">
          <p:pipe port="result" step="unwrap-report"/>
        </p:output>
        <p:unwrap name="unwrap-report" match="/cx:document[@port eq 'result']">
          <p:input port="source">
            <p:pipe port="result" step="eval"/>
          </p:input>
        </p:unwrap>
      </p:otherwise>
    </p:choose>

    <p:sink/>
    
  </p:for-each>
    
</p:declare-step>