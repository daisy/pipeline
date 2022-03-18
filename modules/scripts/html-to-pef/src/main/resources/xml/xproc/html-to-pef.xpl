<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:html-to-pef.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">HTML to braille</h1>
        <p px:role="desc">Transforms a HTML document into an embosser ready braille document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/html-to-pef/">
            Online documentation
        </a>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Jostein Austvik Jacobsen</dd>
            <dt>Organization:</dt>
            <dd px:role="organization" href="http://www.nlb.no/">NLB</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a></dd>
        </dl>
    </p:documentation>
    
    <p:option name="html" required="true" px:type="anyFileURI" px:sequence="false" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input HTML</h2>
            <p px:role="desc" xml:space="preserve">The HTML you want to convert to braille.</p>
        </p:documentation>
    </p:option>
    
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Status</h2>
            <p px:role="desc" xml:space="preserve">Whether or not the conversion was successful.

When `include-obfl` is set to true, the conversion may fail but still output a document on the
"obfl" port.</p>
        </p:documentation>
        <p:pipe step="convert" port="status"/>
    </p:output>
    
    <p:option name="stylesheet" px:sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
          <p px:role="desc" xml:space="preserve" px:inherit="prepend">

A number of [partials](https://sass-lang.com/documentation/at-rules/import#partials) (helper style
sheet modules) are available for use in Sass style sheets:

- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_tables.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/tables):
  for styling tables
- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_definition-lists.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/definition-lists):
  for styling definition lists
</p>
        </p:documentation>
        <p:pipeinfo>
            <px:type>
                <choice>
                    <data type="anyFileURI" datatypeLibrary="http://www.daisy.org/ns/pipeline/xproc">
                        <documentation xml:lang="en">File path relative to input HTML.</documentation>
                    </data>
                    <data type="anyURI">
                        <documentation xml:lang="en">Any other absolute URI</documentation>
                    </data>
                </choice>
            </px:type>
        </p:pipeinfo>
    </p:option>
    
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
    <p:option name="output-dir"/>
    <p:option name="pef-output-dir"/>
    <p:option name="preview-output-dir"/>
    <p:option name="obfl-output-dir"/>
    <p:option name="temp-dir"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:delete-parameters
            px:parse-query
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/html-to-pef/library.xpl">
        <!-- FIXME: we cannot use a relative url to import px:html-to-pef directly here because this
             script uses px:extends-script in the XML catalog which changes the base URI of the
             script at build time. -->
        <p:documentation>
            px:html-to-pef
            px:html-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:tempdir
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    
    <!-- ================================================= -->
    <!-- Create a <c:param-set/> of the options            -->
    <!-- ================================================= -->
    <!-- ...for easy piping so we won't have to explicitly -->
    <!-- pass all the variables all the time.              -->
    <!-- ================================================= -->
    <p:in-scope-names name="in-scope-names"/>
    <px:delete-parameters name="input-options" px:message="Collecting parameters" px:message-severity="DEBUG" px:progress=".01"
                          parameter-names="html
                                           stylesheet
                                           stylesheet-parameters
                                           transform
                                           braille-code
                                           output-file-format
                                           include-pef
                                           include-preview
                                           include-obfl
                                           output-dir
                                           pef-output-dir
                                           preview-output-dir
                                           obfl-output-dir
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
    
    <!-- =============== -->
    <!-- CREATE TEMP DIR -->
    <!-- =============== -->
    <px:tempdir name="temp-dir" px:message="Creating temporary directory" px:message-severity="DEBUG" px:progress=".01">
        <p:with-option name="href" select="if ($temp-dir!='') then $temp-dir else $output-dir"/>
    </px:tempdir>
    
    <!-- ========= -->
    <!-- LOAD HTML -->
    <!-- ========= -->
    <px:fileset-add-entry media-type="application/xhtml+xml">
        <p:input port="source">
          <p:inline><d:fileset/></p:inline>
        </p:input>
        <p:with-option name="href" select="$html"/>
    </px:fileset-add-entry>
    <px:html-load name="html" px:message="Loading HTML" px:progress=".03"/>
    
    <!-- ============ -->
    <!-- HTML TO PEF -->
    <!-- ============ -->
    <px:html-to-pef name="convert" px:message="Converting from HTML to PEF" px:progress=".90">
        <p:input port="source.in-memory">
            <p:pipe step="html" port="result.in-memory"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat(string(/c:result),'convert/')">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="transform"
                       select="concat($braille-code,($transform,'(translator:liblouis)(formatter:dotify)')[not(.='')][1])"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
        <p:input port="parameters">
            <p:pipe port="result" step="input-options"/>
            <p:pipe port="result" step="stylesheet-parameters"/>
        </p:input>
    </px:html-to-pef>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <px:html-to-pef.store px:message="Storing results" px:progress=".05">
        <p:input port="obfl">
            <p:pipe step="convert" port="obfl"/>
        </p:input>
        <p:input port="html">
            <p:pipe step="html" port="result.in-memory"/>
        </p:input>
        <p:with-option name="include-pef" select="$include-pef"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="output-file-format" select="$output-file-format"/>
        <p:with-option name="preview-table" select="$preview-table"/>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="obfl-output-dir" select="$obfl-output-dir"/>
    </px:html-to-pef.store>
    
</p:declare-step>
