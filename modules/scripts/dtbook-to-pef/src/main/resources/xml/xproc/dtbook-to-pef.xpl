<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dtbook-to-pef.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to braille</h1>
        <p px:role="desc" xml:space="preserve">Transforms a DTBook (DAISY 3 XML) document into an embosser ready braille document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-pef/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Bert Frees</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization" href="http://www.sbs-online.ch/">SBS</dd>
            </dl>
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Jostein Austvik Jacobsen</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization" href="http://www.nlb.no/">NLB</dd>
            </dl>
        </address>
    </p:documentation>

    <p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input DTBook</h2>
        </p:documentation>
    </p:input>
    
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- when `include-obfl` is set to true, the conversion may fail but still output a document
             on the "obfl" port -->
        <p:pipe step="convert" port="status"/>
    </p:output>
    
    <p:option name="stylesheet">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
          <p px:role="desc" xml:space="preserve" px:inherit="prepend">

A number of [partials](https://sass-lang.com/documentation/at-rules/import#partials) (helper style
sheet modules) are available for use in Sass style sheets:

- [http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/_tables.scss](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef/src/main/resources/css/tables):
  for styling tables
- [http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/_definition-lists.scss](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef/src/main/resources/css/definition-lists):
  for styling definition lists
</p>
        </p:documentation>
    </p:option>

    <!-- defined in ../../../../../../common-options.xpl -->
    <p:option name="stylesheet-parameters"/>
    <p:option name="braille-code"/>
    <p:option name="transform"/>
    <p:option name="include-preview"/>
    <p:option name="include-pef"/>
    <p:option name="include-obfl"/>
    <p:option name="output-file-format"/>
    <p:option name="preview-table"/>
    <p:option name="page-width"/>
    <p:option name="page-height"/>
    <p:option name="duplex"/>
    <p:option name="levels-in-footer"/>
    <p:option name="hyphenation"/>
    <p:option name="hyphenation-at-page-breaks"/>
    <p:option name="line-spacing"/>
    <p:option name="capital-letters"/>
    <p:option name="include-captions"/>
    <p:option name="include-images"/>
    <p:option name="include-line-groups"/>
    <p:option name="include-production-notes"/>
    <p:option name="show-braille-page-numbers"/>
    <p:option name="show-print-page-numbers"/>
    <p:option name="force-braille-page-break"/>
    <p:option name="toc-depth"/>
    <p:option name="toc-exclude-class"/>
    <p:option name="maximum-number-of-sheets"/>
    <p:option name="allow-volume-break-inside-leaf-section-factor"/>
    <p:option name="prefer-volume-break-before-higher-level-factor"/>
    <p:option name="notes-placement"/>
    <p:option name="result"/>
    <p:option name="pef"/>
    <p:option name="preview"/>
    <p:option name="obfl"/>
    
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>
    
    <!-- ======= -->
    <!-- Imports -->
    <!-- ======= -->
    <p:import href="library.xpl">
        <p:documentation>
            px:dtbook-to-pef
            px:dtbook-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:delete-parameters
            px:parse-query
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    
    <!-- ================================================= -->
    <!-- Create a <c:param-set/> of the options            -->
    <!-- ================================================= -->
    <!-- ...for easy piping so we won't have to explicitly -->
    <!-- pass all the variables all the time.              -->
    <!-- ================================================= -->
    <p:in-scope-names name="in-scope-names"/>
    <px:delete-parameters name="input-options" px:progress=".01"
                          parameter-names="stylesheet
                                           stylesheet-parameters
                                           transform
                                           braille-code
                                           output-file-format
                                           include-pef
                                           include-preview
                                           include-obfl
                                           result
                                           pef
                                           preview
                                           obfl
                                           temp-dir">
        <p:input port="source">
            <p:pipe port="result" step="in-scope-names"/>
        </p:input>
    </px:delete-parameters>
    <p:sink/>
    <px:parse-query name="stylesheet-parameters">
        <p:with-option name="query" select="$stylesheet-parameters"/>
    </px:parse-query>
    <p:sink/>
    
    <!-- ======= -->
    <!-- LOAD -->
    <!-- ======= -->
    <px:dtbook-load name="load" px:progress=".01">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:dtbook-load>
    
    <!-- ======= -->
    <!-- CONVERT -->
    <!-- ======= -->
    <px:dtbook-to-pef name="convert" px:message="Transforming from DTBook to PEF" px:progress=".92">
        <p:input port="source.in-memory">
            <p:pipe step="load" port="result.in-memory"/>
        </p:input>
        <p:with-option name="temp-dir" select="$temp-dir"/>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="transform"
                       select="concat($braille-code,($transform,'(translator:liblouis)(formatter:dotify)')[not(.='')][1])"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
        <p:input port="parameters">
            <p:pipe port="result" step="input-options"/>
            <p:pipe port="result" step="stylesheet-parameters"/>
        </p:input>
    </px:dtbook-to-pef>
    
    <!-- ===== -->
    <!-- STORE -->
    <!-- ===== -->
    <px:dtbook-to-pef.store px:message="Storing results" px:progress=".06">
        <p:input port="dtbook">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:input port="obfl">
            <p:pipe step="convert" port="obfl"/>
        </p:input>
        <p:with-option name="include-pef" select="$include-pef"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="output-file-format" select="$output-file-format"/>
        <p:with-option name="preview-table" select="$preview-table"/>
        <p:with-option name="output-dir" select="$result"/>
        <p:with-option name="pef-output-dir" select="$pef"/>
        <p:with-option name="preview-output-dir" select="$preview"/>
        <p:with-option name="obfl-output-dir" select="$obfl"/>
    </px:dtbook-to-pef.store>
    
</p:declare-step>
