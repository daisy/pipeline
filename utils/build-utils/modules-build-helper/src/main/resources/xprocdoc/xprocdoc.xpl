<p:declare-step version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:c="http://www.w3.org/ns/xproc-step"
            xmlns:xd="http://github.com/vojtechtoman/xprocdoc"
            xmlns:xderr="http://github.com/vojtechtoman/xprocdoc-err"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            type="xd:xprocdoc" name="xprocdoc" exclude-inline-prefixes="#all">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>A simple Javadoc-style XProc API documentation generator written in XProc and XSLT 2.0.</p>

    <p>The documentation for the XProc pipelines is specified inline, in the actual source files, similar to Javadoc comments in Java source files. The documentation takes the form of XHTML fragments wrapped in the standard XProc <tt>p:documentation</tt> element, and can be provided for the following XProc elements:</p>

    <ul>
      <li><tt>p:library</tt> - A library containing zero or more XProc steps:
      <pre><![CDATA[<p:library version="1.0">
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Library for simple DITA processing.</p>
  </p:documentation>

  <p:declare-step type="...">
    ...
  </p:declare-step>
  ...
</p:library>]]></pre></li>

      <li><tt>p:declare-step</tt> and <tt>p:pipeline</tt> - An XProc step:
      <pre><![CDATA[<p:declare-step>
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This pipeline transforms a DITA topic to XHTML.</p>
  </p:documentation>
  ...
</p:declare-step>]]></pre></li>

      <li><tt>p:input</tt> - An input port of a step:
      <pre><![CDATA[<p:declare-step>
  <p:input port="source">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The DITA topic to transform.</p>
    </p:documentation>
  </p:input>
  ...
</p:declare-step>]]></pre></li>

      <li><tt>p:output</tt> - An output port of a step:
      <pre><![CDATA[<p:declare-step>
  ...
  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The generated XHTML document.</p>
    </p:documentation>
  </p:output>
  ...
</p:declare-step>]]></pre></li>

      <li><tt>p:option</tt> - An option of a step:
      <pre><![CDATA[<p:declare-step>
  ...
  <p:option name="validate" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>This option indicates whether to validate the
         topic before it is processed. The value of the
         option must be a boolean.</p>
    </p:documentation>
  </p:option>
  ...
</p:declare-step>]]></pre></li>
    </ul>

    <p>To generate the XHTML documentation for a set of XProc files, simply pass the files to the <tt>source</tt> input port of the <tt>xd:xprocdoc</tt> step. The step will process the XProc sources and will generate a set of XHTML files in the current working directory (or in a location specified using the <tt>output-base-uri</tt> option). The file <tt>index.html</tt> can be used to access the documentation overview page.</p>

    <p>The <tt>xd:xprocdoc</tt> step generates documentation for all XProc steps that are:
    <ul>
      <li>direct children of p:library and specify the type attribute; or</li>
      <li>top-level steps (i.e. not contained in p:library) with or without the type attribute.</li>
    </ul>

    (In other words, documentation will be generated only for those steps that can be used by developers, either directly or by importing an XProc library.)</p>

    <p>Steps can also be explicitly excluded from the generated documentation by setting the <tt>exclude</tt> attribute (in the <tt>http://github.com/vojtechtoman/xprocdoc</tt> namespace) to <tt>true</tt> on the step's <tt>p:declare-step</tt> or <tt>p:pipeline</tt> element:</p>

    <pre><![CDATA[<p:declare-step xd:exclude="true"
                xmlns:xd="http://www.emc.com/documentum/xml/xproc/doc">
  ...
</p:declare-step>]]></pre>

    <p>The documentation generation pipeline also follows <tt>p:import</tt> statements in XProc libraries. That way, the documentation can be generated for the complete dependency set. (The pipeline supports re-entrant and circular imports.)</p>
  </p:documentation>

  <p:input port="source" sequence="true" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The pipelines or pipeline libraries to process.</p>
    </p:documentation>
  </p:input>

  <p:output port="result" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A sequence of <tt>c:result</tt> documents pointing to the generated documentation files.</p>
    </p:documentation>
  </p:output>

  <p:option name="product" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The name of the product that will appear on the generated overview XHTML page.</p>
    </p:documentation>
  </p:option>

  <p:option name="input-base-uri" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The base URI of the source XProc pipelines. This parameter can be used to customize the way the source URIs are presented in the generated XHTML. For example, if the source pipelines <tt>src1.xpl</tt> and <tt>src2.xpl</tt> are stored in the directory with the base URI <tt>file:/home/fred/pipelines/</tt>, setting the <tt>input-base-uri</tt> option to <tt>file:/home/fred/</tt> will cause the pipelines to appear as <tt>pipelines/src1.xpl</tt> and <tt>pipelines/src2.xpl</tt>. If the <tt>input-base-uri</tt> option is left unspecified, the pipelines will appear as <tt>file:/home/fred/pipelines/src1.xpl</tt> and <tt>file:/home/fred/pipelines/src2.xpl</tt></p>
    </p:documentation>
  </p:option>

  <p:option name="output-base-uri" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The base URI of the directory where the generated XHTML output is stored. If not specified, the current working directory will be used.</p>
    </p:documentation>
  </p:option>

  <p:option name="overview-file" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The URI of a file with an XHTML boilerplate text that will be inserted in the generated overview page. If not specified, no boilerplate text will be inserted.</p>
    </p:documentation>
  </p:option>


  <!-- -->

  <p:declare-step type="xd:process-sources" name="process-sources">
    <p:input port="source" sequence="true" primary="true"/>
    <p:input port="summary">
      <p:inline>
        <xd:summary/>
      </p:inline>
    </p:input>
    <p:output port="result"/>

    <p:count/>
    
    <p:choose>
      <p:when test="/c:result = 0">
        <!-- no more sources to process: return the current summary -->
        <p:identity>
          <p:input port="source">
            <p:pipe step="process-sources" port="summary"/>
          </p:input>
        </p:identity>
      </p:when>
      <p:otherwise>
        <p:split-sequence test="position() = 1" initial-only="true" name="split">
          <p:input port="source">
            <p:pipe step="process-sources" port="source"/>
          </p:input>
        </p:split-sequence>

        <!-- process the first source -->
        <xd:process-source name="process-first" standalone="true">
          <p:input port="source">
            <p:pipe step="split" port="matched"/>
          </p:input>
          <p:input port="summary">
            <p:pipe step="process-sources" port="summary"/>
          </p:input>
        </xd:process-source>

        <!-- process the rest recursively -->
        <xd:process-sources>
          <p:input port="source">
            <p:pipe step="split" port="not-matched"/>
          </p:input>
          <p:input port="summary">
            <p:pipe step="process-first" port="result"/>
          </p:input>
        </xd:process-sources>
      </p:otherwise>
    </p:choose>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-source" name="process-source">
    <p:input port="source" primary="true"/>
    <p:input port="summary"/>
    <p:output port="result"/>
    <p:option name="standalone" select="'false'"/>
    
    <p:choose>
      <p:when test="/p:library">
        <xd:process-library-as-source name="process-lib">
          <p:input port="summary">
            <p:pipe step="process-source" port="summary"/>
          </p:input>
        </xd:process-library-as-source>
      </p:when>
      <p:when test="/p:pipeline or /p:declare-step">
        <xd:process-step-as-source name="process-step">
          <p:input port="summary">
            <p:pipe step="process-source" port="summary"/>
          </p:input>
          <p:with-option name="standalone" select="$standalone"/>
        </xd:process-step-as-source>
      </p:when>
      <p:otherwise>
        <p:wrap-sequence wrapper="error" name="create-error-message">
          <p:input port="source">
            <p:inline>
              <message>Unsupported document element. One of p:library, p:declare-step, or p:pipeline expected</message>
            </p:inline>
            <p:pipe step="process-source" port="source"/>
          </p:input>
        </p:wrap-sequence>
        <p:error code="xderr:err0001">
          <p:input port="source">
            <p:pipe step="create-error-message" port="result"/>
          </p:input>
        </p:error>
      </p:otherwise>
    </p:choose>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-library-as-source" name="process-library-as-source">
    <p:input port="source" primary="true"/>
    <p:input port="summary"/>
    <p:output port="result"/>

    <p:variable name="source-base-uri" select="p:base-uri()"/>

    <p:choose name="choose">
      <p:when test="/xd:summary/xd:source[@href = $source-base-uri]">
        <p:xpath-context>
          <p:pipe step="process-library-as-source" port="summary"/>
        </p:xpath-context>
        <!-- source already processed; ignore it and return the summary unchanged -->
        <p:output port="result">
          <p:pipe step="process-library-as-source" port="summary"/>
        </p:output>
        <p:sink/>
      </p:when>

      <p:otherwise>
        <p:output port="result"/>
        <p:group name="create-source-element">
          <p:output port="result"/>
          <p:add-attribute match="xd:source" attribute-name="href">
            <p:input port="source">
              <p:inline>
                <xd:source/>
              </p:inline>
            </p:input>
            <p:with-option name="attribute-value" select="$source-base-uri">
              <p:empty/>
            </p:with-option>
          </p:add-attribute>
          <p:choose>
            <p:xpath-context>
              <p:pipe step="process-library-as-source" port="source"/>
            </p:xpath-context>
            <p:when test="/*/@px:public-name">
              <p:add-attribute match="xd:source" attribute-name="name">
                <p:with-option name="attribute-value" select="/*/@px:public-name">
                  <p:pipe step="process-library-as-source" port="source"/>
                </p:with-option>
              </p:add-attribute>
            </p:when>
            <p:otherwise>
              <p:identity/>
            </p:otherwise>
          </p:choose>
        </p:group>
        
        <p:group name="add-source-to-summary">
          <p:output port="result"/>
          <p:insert match="xd:summary" position="last-child">
            <p:input port="source">
              <p:pipe step="process-library-as-source" port="summary"/>
            </p:input>
            <p:input port="insertion">
              <p:pipe step="create-source-element" port="result"/>
            </p:input>
          </p:insert>
        </p:group>
        
        <xd:process-imports name="process-imports">
          <p:input port="source" select="/*/p:import">
            <p:pipe step="process-library-as-source" port="source"/>
          </p:input>
          <p:input port="summary">
            <p:pipe step="add-source-to-summary" port="result"/>
          </p:input>
        </xd:process-imports>
        
        <xd:process-steps-in-library name="process-steps-in-lib">
          <p:input port="source" select="/*/p:declare-step | /*/p:pipeline">
            <p:pipe step="process-library-as-source" port="source"/>
          </p:input>
          <p:input port="context-source">
            <p:pipe step="process-library-as-source" port="source"/>
          </p:input>
        </xd:process-steps-in-library>
        
        <p:wrap-sequence wrapper="xd:library" name="create-library-element">
          <p:input port="source">
            <p:pipe step="process-imports" port="result"/>
            <p:pipe step="process-steps-in-lib" port="result"/>
          </p:input>
        </p:wrap-sequence>
        
        <xd:process-documentation name="process-doc">
          <p:input port="source">
            <p:pipe step="process-library-as-source" port="source"/>
          </p:input>
          <p:input port="target">
            <p:pipe step="create-library-element" port="result"/>
          </p:input>
        </xd:process-documentation>

        <p:insert position="first-child">
          <p:input port="source">
            <p:pipe step="process-imports" port="result-summary"/>
          </p:input>
          <p:input port="insertion">
            <p:pipe step="process-doc" port="result"/>
          </p:input>
          <p:with-option name="match" select="concat('/xd:summary/xd:source[@href=&quot;',$source-base-uri, '&quot;]')"/>
        </p:insert>
      </p:otherwise>
    </p:choose>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-imports" name="process-imports">
    <p:input port="source" sequence="true" primary="true"/>
    <p:input port="summary"/>
    <p:output port="result" sequence="true" primary="true">
      <p:pipe step="choose" port="result"/>
    </p:output>
    <p:output port="result-summary">
      <p:pipe step="choose" port="result-summary"/>
    </p:output>

    <p:count/>

    <p:choose name="choose">
      <p:when test="/c:result = 0">
        <!-- no more p:imports to process: return an empty sequence and the summary unchanged -->
        <p:output port="result" sequence="true">
          <p:empty/>
        </p:output>
        <p:output port="result-summary">
          <p:pipe step="process-imports" port="summary"/>
        </p:output>
        <p:sink/>
      </p:when>
      <p:otherwise>
        <p:output port="result" sequence="true">
          <p:pipe step="process-first" port="result"/>
          <p:pipe step="process-rest" port="result"/>
        </p:output>
        <p:output port="result-summary">
          <p:pipe step="process-rest" port="result-summary"/>
        </p:output>

        <p:split-sequence test="position() = 1" initial-only="true" name="split">
          <p:input port="source">
            <p:pipe step="process-imports" port="source"/>
          </p:input>
        </p:split-sequence>

        <!-- process the first import -->
        <xd:process-import name="process-first">
          <p:input port="source">
            <p:pipe step="split" port="matched"/>
          </p:input>
          <p:input port="summary">
            <p:pipe step="process-imports" port="summary"/>
          </p:input>
        </xd:process-import>

        <!-- process the rest recursively -->
        <xd:process-imports name="process-rest">
          <p:input port="source">
            <p:pipe step="split" port="not-matched"/>
          </p:input>
          <p:input port="summary">
            <p:pipe step="process-first" port="result-summary"/>
          </p:input>
        </xd:process-imports>
      </p:otherwise>
    </p:choose>

  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-import" name="process-import">
    <p:input port="source" primary="true"/>
    <p:input port="summary"/>
    <p:output port="result" primary="true"/>
    <p:output port="result-summary">
      <p:pipe step="choose" port="result-summary"/>
    </p:output>

    <p:variable name="source-base-uri" select="p:resolve-uri(/p:import/@href, p:base-uri())"/>

    <p:choose name="choose">
      <p:when test="/xd:summary/xd:source[@href = $source-base-uri]">
        <p:xpath-context>
          <p:pipe step="process-import" port="summary"/>
        </p:xpath-context>
        <!-- source already processed; ignore it and return the summary unchanged -->
        <p:output port="result-summary">
          <p:pipe step="process-import" port="summary"/>
        </p:output>
        <p:sink/>
      </p:when>

      <p:otherwise>
        <p:output port="result-summary">
          <p:pipe step="process-source" port="result"/>
        </p:output>
        <p:load>
          <p:with-option name="href" select="$source-base-uri"/>
        </p:load>
        <xd:process-source name="process-source">
          <p:input port="summary">
            <p:pipe step="process-import" port="summary"/>
          </p:input>
        </xd:process-source>
      </p:otherwise>
    </p:choose>

    <!-- create an xd:import element to record the p:import occurrence -->
    <p:add-attribute match="xd:import" attribute-name="href">
      <p:input port="source">
        <p:inline>
          <xd:import/>
        </p:inline>
      </p:input>
      <p:with-option name="attribute-value" select="$source-base-uri"/>
    </p:add-attribute>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-step-as-source" name="process-step-as-source">
    <p:input port="source" primary="true"/>
    <p:input port="summary"/>
    <p:output port="result" primary="true"/>
    <p:option name="standalone" select="'false'"/>

    <p:variable name="source-base-uri" select="p:base-uri()"/>

    <p:choose name="choose">
      <p:when test="/xd:summary/xd:source[@href = $source-base-uri]">
        <p:xpath-context>
          <p:pipe step="process-step-as-source" port="summary"/>
        </p:xpath-context>
        <!-- source already processed; ignore it and return the summary unchanged -->
        <p:output port="result">
          <p:pipe step="process-step-as-source" port="summary"/>
        </p:output>
        <p:sink/>
      </p:when>

      <p:otherwise>
        <p:output port="result"/>

        <p:group name="create-source-element">
          <p:output port="result"/>
          <p:add-attribute match="xd:source" attribute-name="href">
            <p:input port="source">
              <p:inline>
                <xd:source/>
              </p:inline>
            </p:input>
            <p:with-option name="attribute-value" select="$source-base-uri">
              <p:empty/>
            </p:with-option>
          </p:add-attribute>
          <p:choose>
            <p:xpath-context>
              <p:pipe step="process-step-as-source" port="source"/>
            </p:xpath-context>
            <p:when test="/*/@px:public-name">
              <p:add-attribute match="xd:source" attribute-name="name">
                <p:with-option name="attribute-value" select="/*/@px:public-name">
                  <p:pipe step="process-step-as-source" port="source"/>
                </p:with-option>
              </p:add-attribute>
            </p:when>
            <p:otherwise>
              <p:identity/>
            </p:otherwise>
          </p:choose>
        </p:group>

        <p:group name="add-source-to-summary">
          <p:output port="result"/>
          <p:insert match="xd:summary" position="last-child">
            <p:input port="source">
              <p:pipe step="process-step-as-source" port="summary"/>
            </p:input>
            <p:input port="insertion">
              <p:pipe step="create-source-element" port="result"/>
            </p:input>
          </p:insert>
        </p:group>

        <!-- here is potentially the right place to process imports within the step -->
        <xd:process-step name="process-step">
          <p:input port="source">
            <p:pipe step="process-step-as-source" port="source"/>
          </p:input>
          <p:input port="context-source">
            <p:pipe step="process-step-as-source" port="source"/>
          </p:input>
          <p:with-option name="standalone" select="$standalone"/>
        </xd:process-step>

        <p:insert position="first-child">
          <p:input port="source">
            <p:pipe step="add-source-to-summary" port="result"/>
          </p:input>
          <p:input port="insertion">
            <p:pipe step="process-step" port="result"/>
          </p:input>
          <p:with-option name="match" select="concat('/xd:summary/xd:source[@href=&quot;',$source-base-uri, '&quot;]')"/>
        </p:insert>
      </p:otherwise>
    </p:choose>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-steps-in-library" name="process-steps-in-lib">
    <p:input port="source" sequence="true" primary="true"/>
    <p:input port="context-source"/>
    <p:output port="result" sequence="true"/>

    <p:count/>

    <p:choose>
      <p:when test="/c:result = 0">
        <!-- no more steps to process: return an empty sequence -->
        <p:identity>
          <p:input port="source">
            <p:empty/>
          </p:input>
        </p:identity>
      </p:when>
      <p:otherwise>
        <p:split-sequence test="position() = 1" initial-only="true" name="split">
          <p:input port="source">
            <p:pipe step="process-steps-in-lib" port="source"/>
          </p:input>
        </p:split-sequence>

        <!-- process the first step -->
        <xd:process-step name="process-first">
          <p:input port="source">
            <p:pipe step="split" port="matched"/>
          </p:input>
          <p:input port="context-source">
            <p:pipe step="process-steps-in-lib" port="context-source"/>
          </p:input>
        </xd:process-step>

        <!-- process the rest recursively -->
        <xd:process-steps-in-library name="process-rest">
          <p:input port="source">
            <p:pipe step="split" port="not-matched"/>
          </p:input>
          <p:input port="context-source">
            <p:pipe step="process-steps-in-lib" port="context-source"/>
          </p:input>
        </xd:process-steps-in-library>

        <p:identity>
          <p:input port="source">
            <p:pipe step="process-first" port="result"/>
            <p:pipe step="process-rest" port="result"/>
          </p:input>
        </p:identity>
      </p:otherwise>
    </p:choose>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-step" name="process-step">
    <!-- returns an xd:step document or an empty sequence for steps with no type -->
    <p:input port="source" primary="true"/>
    <p:input port="context-source"/> <!-- the document containing the step (either a step or a library); used for namespace URI lookup -->
    <p:output port="result" sequence="true"/>
    <p:option name="standalone" select="'false'"/>

    <p:choose>
      <p:when test="not(/*/@xd:exclude='true') and ($standalone='true' or /*[@type != ''])">
        <p:variable name="type-raw" select="/*/@type"/>
        <p:variable name="local-name" select="substring-after($type-raw,':')"/>
        <p:variable name="prefix" select="substring-before($type-raw, ':')"/>
        <!-- The following is a bit awkward, but I could not find any simpler way to
             do namespace URI lookup in XProc...
             The context source is either p:library or p:pipeline|p:declare-step.
             For p:library, we have to look at its direct children to find the step; for
             p:pipeline|p:declare-step we have to look directly at the document node.
        -->
        <p:variable name="namespace-uri" select="((/p:declare-step | /p:pipeline)[$prefix != '' and ($standalone='true' or @type=$type-raw)] | (/p:library/p:declare-step | /p:library/p:pipeline)[$prefix != '' and @type=$type-raw])/namespace::*[local-name()=$prefix]">
          <p:pipe step="process-step" port="context-source"/>
        </p:variable>

        <p:variable name="xproc-version" select="((/p:declare-step | /p:pipeline)[$standalone='true' or @type=$type-raw] | (/*/p:declare-step | /*/p:pipeline)[@type=$type-raw])/ancestor-or-self::*[@version][1]/@version">
          <p:pipe step="process-step" port="context-source"/>
        </p:variable>

        <p:variable name="xpath-version" select="((/p:declare-step | /p:pipeline)[$standalone='true' or @type=$type-raw] | (/*/p:declare-step | /*/p:pipeline)[@type=$type-raw])/ancestor-or-self::*[@xpath-version][1]/@xpath-version">
          <p:pipe step="process-step" port="context-source"/>
        </p:variable>

        <p:variable name="psvi-required" select="((/p:declare-step | /p:pipeline)[$standalone='true' or @type=$type-raw] | (/*/p:declare-step | /*/p:pipeline)[@type=$type-raw])/ancestor-or-self::*[@psvi-required][1]/@psvi-required">
          <p:pipe step="process-step" port="context-source"/>
        </p:variable>

        <p:choose name="preprocess-step">
          <p:when test="/p:pipeline">
            <p:output port="result"/>
            <!-- for p:pipeline, make sure that the default "source", "result",
                 and "parameters" ports are taken into account -->
            <p:insert match="p:pipeline" position="first-child">
              <p:input port="insertion">
                <p:inline>
                  <p:input port="source" kind="document" sequence="false" primary="true"/>
                </p:inline>
                <p:inline>
                  <p:input port="parameters" kind="parameter" primary="true"/>
                </p:inline>
                <p:inline>
                  <p:output port="result" kind="document" sequence="false" primary="true"/>
                </p:inline>
              </p:input>
            </p:insert>
          </p:when>
          <p:otherwise>
            <p:output port="result"/>
            <p:identity/>
          </p:otherwise>
        </p:choose>

        <!-- Note: we don't process p:import here as imports inside steps are
             not visible to the outside. -->
        
        <p:group name="process-document-input-ports">
          <p:output port="result" sequence="true"/>
          <p:for-each>
            <p:iteration-source select="/*/p:input[not(@kind) or @kind='document']">
              <p:pipe step="preprocess-step" port="result"/>
            </p:iteration-source>
            
            <xd:process-port type="input">
              <p:with-option name="port-count" select="p:iteration-size()"/>
            </xd:process-port>
          </p:for-each>
        </p:group>
        
        <p:group name="process-parameter-input-ports">
          <p:output port="result" sequence="true"/>
          <p:for-each>
            <p:iteration-source select="/*/p:input[@kind='parameter']">
              <p:pipe step="preprocess-step" port="result"/>
            </p:iteration-source>
            
            <xd:process-port type="parameterinput">
              <p:with-option name="port-count" select="p:iteration-size()"/>
            </xd:process-port>
          </p:for-each>
        </p:group>
        
        <p:group name="process-output-ports">
          <p:output port="result" sequence="true"/>
          <p:for-each>
            <p:iteration-source select="/*/p:output">
              <p:pipe step="preprocess-step" port="result"/>
            </p:iteration-source>
            
            <xd:process-port type="output">
              <p:with-option name="port-count" select="p:iteration-size()"/>
            </xd:process-port>
          </p:for-each>
        </p:group>
        
        <p:group name="process-options">
          <p:output port="result" sequence="true"/>
          <p:for-each name="for">
            <p:iteration-source select="/*/p:option">
              <p:pipe step="preprocess-step" port="result"/>
            </p:iteration-source>
            
            <xd:process-option>
              <p:input port="context-source">
                <p:pipe step="process-step" port="context-source"/>
              </p:input>
              <p:with-option name="step-type-raw" select="$type-raw"/>
              <p:with-option name="standalone" select="$standalone"/>
            </xd:process-option>
          </p:for-each>
        </p:group>
        
        <p:group name="create-step-element">
          <p:output port="result"/>
          <p:wrap-sequence wrapper="xd:step">
            <p:input port="source">
              <p:pipe step="process-document-input-ports" port="result"/>
              <p:pipe step="process-parameter-input-ports" port="result"/>
              <p:pipe step="process-output-ports" port="result"/>
              <p:pipe step="process-options" port="result"/>
            </p:input>
          </p:wrap-sequence>
          
          <p:add-attribute match="xd:step" attribute-name="local-name">
            <p:with-option name="attribute-value" select="$local-name"/>
          </p:add-attribute>
          <p:add-attribute match="xd:step" attribute-name="namespace-uri">
            <p:with-option name="attribute-value" select="$namespace-uri"/>
          </p:add-attribute>
          <p:add-attribute match="xd:step" attribute-name="prefix">
            <p:with-option name="attribute-value" select="$prefix"/>
          </p:add-attribute>
          <p:add-attribute match="xd:step" attribute-name="xpath-version">
            <p:with-option name="attribute-value" select="$xpath-version"/>
          </p:add-attribute>
          <p:add-attribute match="xd:step" attribute-name="xproc-version">
            <p:with-option name="attribute-value" select="$xproc-version"/>
          </p:add-attribute>
          <p:add-attribute match="xd:step" attribute-name="psvi-required">
            <p:with-option name="attribute-value" select="$psvi-required"/>
          </p:add-attribute>
        </p:group>

        <xd:process-documentation>
          <p:input port="source">
            <p:pipe step="process-step" port="source"/>
          </p:input>
          <p:input port="target">
            <p:pipe step="create-step-element" port="result"/>
          </p:input>
        </xd:process-documentation>
      </p:when>

      <p:otherwise>
        <!-- not standalone and step with no type information,
             or a step that should be excluded -->
        <p:identity>
          <p:input port="source">
            <p:empty/>
          </p:input>
        </p:identity>
      </p:otherwise>
    </p:choose>
  </p:declare-step>
      
  <!-- -->

  <p:declare-step type="xd:process-port" name="process-port">
    <p:input port="source"/>
    <p:output port="result"/>

    <p:option name="type" select="'input'"/> <!-- input | parameterinput | output -->
    <p:option name="port-count"/> <!-- number of ports of the same type in the step -->

    <p:group name="create-port-element">
      <p:output port="result"/>
      <p:choose>
        <p:when test="$type = 'input'">
          <p:identity>
            <p:input port="source">
              <p:inline>
                <xd:input kind="document"/>
              </p:inline>
            </p:input>
          </p:identity>
        </p:when>
        <p:when test="$type = 'parameterinput'">
          <p:identity>
            <p:input port="source">
              <p:inline>
                <xd:input kind="parameter"/>
              </p:inline>
            </p:input>
          </p:identity>
        </p:when>
        <p:when test="$type = 'output'">
          <p:identity>
            <p:input port="source">
              <p:inline>
                <xd:output/>
              </p:inline>
            </p:input>
          </p:identity>
        </p:when>
        <p:otherwise>
          <p:error code="xderr:err0002">
            <p:input port="source">
              <p:inline>
                <message>Unsupported port type. One of 'input', 'parameterinput', or 'output' expected.</message>
              </p:inline>
            </p:input>
          </p:error>
        </p:otherwise>
      </p:choose>

      <p:add-attribute match="/*" attribute-name="port">
        <p:with-option name="attribute-value" select="/*/@port">
          <p:pipe step="process-port" port="source"/>
        </p:with-option>
      </p:add-attribute>
      <p:add-attribute match="/*" attribute-name="sequence">
        <p:with-option name="attribute-value" select="$type='parameterinput' or /*/@sequence='true'">
          <p:pipe step="process-port" port="source"/>
        </p:with-option>
      </p:add-attribute>
      <p:add-attribute match="/*" attribute-name="primary">
        <!-- primary either explicitly set to "true", or not set and there is no other document input port -->
        <p:with-option name="attribute-value" select="/*/@primary='true' or (not(/*/@primary) and $port-count = 1)">
          <p:pipe step="process-port" port="source"/>
        </p:with-option>
      </p:add-attribute>
    </p:group>

    <xd:process-documentation>
      <p:input port="source">
        <p:pipe step="process-port" port="source"/>
      </p:input>
      <p:input port="target">
        <p:pipe step="create-port-element" port="result"/>
      </p:input>
    </xd:process-documentation>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-option" name="process-option">
    <p:input port="source" primary="true"/>
    <p:input port="context-source"/>
    <p:output port="result"/>
    <p:option name="step-type-raw" required="true"/>
    <p:option name="standalone" select="'false'"/>

    <p:variable name="name-raw" select="/*/@name"/>
    <p:variable name="local-name"
                select="concat(substring-after(/*/@name[contains(., ':')], ':'), /*/@name[not(contains(., ':'))])"/>
    <p:variable name="prefix" select="substring-before($name-raw, ':')"/>

    <p:variable name="namespace-uri" select="((/p:declare-step | /p:pipeline)[$prefix != '' and ($standalone = 'true' or @type=$step-type-raw)] | (/p:library/p:declare-step | /p:library/p:pipeline)[$prefix != '' and @type=$step-type-raw])/p:option[@name=$name-raw]/namespace::*[local-name()=$prefix]">
      <p:pipe step="process-option" port="context-source"/>
    </p:variable>

    <p:group name="create-option-element">
      <p:output port="result"/>
      <p:add-attribute match="xd:option" attribute-name="local-name">
        <p:input port="source">
          <p:inline>
            <xd:option/>
          </p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="$local-name"/>
      </p:add-attribute>
      <p:add-attribute match="xd:option" attribute-name="namespace-uri">
        <p:with-option name="attribute-value" select="$namespace-uri"/>
      </p:add-attribute>
      <p:add-attribute match="xd:option" attribute-name="prefix">
        <p:with-option name="attribute-value" select="$prefix"/>
      </p:add-attribute>
      <p:add-attribute match="xd:option" attribute-name="required">
        <p:with-option name="attribute-value" select="/*/@required='true'">
          <p:pipe step="process-option" port="source"/>
        </p:with-option>
      </p:add-attribute>
      <p:add-attribute match="xd:option" attribute-name="select">
        <p:with-option name="attribute-value" select="/*/@select">
          <p:pipe step="process-option" port="source"/>
        </p:with-option>
      </p:add-attribute>
    </p:group>

    <xd:process-documentation>
      <p:input port="source">
        <p:pipe step="process-option" port="source"/>
      </p:input>
      <p:input port="target">
        <p:pipe step="create-option-element" port="result"/>
      </p:input>
    </xd:process-documentation>

  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:process-documentation" name="process-doc">
    <p:input port="source" primary="true"/> <!-- source: where to extract documentation from -->
    <p:input port="target"/> <!-- where to insert documentation to -->
    <p:output port="result"/>

    <p:choose name="create-doc">
      <p:when test="/*/p:documentation[not(preceding-sibling::*)]">
        <!-- the first child element is p:documentation -->
        <p:output port="result" sequence="true"/>
        <p:insert position="first-child">
          <p:input port="source">
            <p:inline>
              <xd:documentation/>
            </p:inline>
          </p:input>
          <p:input port="insertion" select="/*/p:documentation[1]">
            <p:pipe step="process-doc" port="source"/>
          </p:input>
        </p:insert>
        <p:unwrap match="p:documentation"/>
      </p:when>
      <p:otherwise>
        <p:output port="result" sequence="true"/>
        <p:identity>
          <p:input port="source">
            <p:empty/>
          </p:input>
        </p:identity>
      </p:otherwise>
    </p:choose>

    <p:insert match="/*" position="first-child">
      <p:input port="source">
        <p:pipe step="process-doc" port="target"/>
      </p:input>
      <p:input port="insertion">
        <p:pipe step="create-doc" port="result"/>
      </p:input>
    </p:insert>
  </p:declare-step>

  <!-- -->

  <p:declare-step type="xd:summary-to-xhtml" name="summary-to-xhtml">
    <p:input port="source" primary="true"/>
    <p:input port="stylesheet">
      <p:document href="xd2html.xsl"/>
    </p:input>
    <p:input port="parameters" kind="parameter"/>
    <p:output port="result" sequence="true"/>

    <p:xslt version="2.0" name="xslt">
      <p:input port="stylesheet">
        <p:pipe step="summary-to-xhtml" port="stylesheet"/>
      </p:input>
    </p:xslt>
    <p:sink/>

    <p:for-each>
      <p:iteration-source>
        <p:pipe step="xslt" port="secondary"/>
      </p:iteration-source>
      <p:store name="store">
        <p:with-option name="href" select="p:base-uri()"/>
      </p:store>
      <p:identity>
        <p:input port="source">
          <p:pipe step="store" port="result"/>
        </p:input>
      </p:identity>
    </p:for-each>
  </p:declare-step>

  <!-- -->

  <xd:process-sources name="process"/>

  <xd:summary-to-xhtml name="transform">
    <p:with-param name="product" select="$product"/>
    <p:with-param name="input-base-uri" select="$input-base-uri"/>
    <p:with-param name="output-base-uri" select="$output-base-uri"/>
    <p:with-param name="overview-file" select="$overview-file"/>
  </xd:summary-to-xhtml>

</p:declare-step>
