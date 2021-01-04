<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:xd="http://github.com/vojtechtoman/xprocdoc"
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

  <p:import href="process-sources.xpl"/>
  <p:import href="summary-to-xhtml.xpl"/>

  <xd:process-sources name="process"/>

  <xd:summary-to-xhtml name="transform">
    <p:with-param name="product" select="$product"/>
    <p:with-param name="input-base-uri" select="$input-base-uri"/>
    <p:with-param name="output-base-uri" select="$output-base-uri"/>
    <p:with-param name="overview-file" select="$overview-file"/>
  </xd:summary-to-xhtml>

</p:declare-step>
