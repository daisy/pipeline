<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:ocf="urn:oasis:names:tc:opendocument:xmlns:container"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:opf="http://www.idpf.org/2007/opf"
                exclude-inline-prefixes="#all"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	    <h1 px:role="name">EPUB 3 to braille</h1>
        <p px:role="desc" xml:space="preserve">Transforms a EPUB 3 publication into an embosser ready braille document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub3-to-pef/">
            Online documentation
        </a>
    </p:documentation>

    <p:option name="source" required="true" px:type="anyFileURI" px:sequence="false" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input EPUB 3</h2>
            <p px:role="desc" xml:space="preserve">The EPUB you want to convert to braille.

You may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="preamble" required="false" select="''" px:type="anyFileURI" px:sequence="false" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Preamble HTML</h2>
            <p px:role="desc">An HTML file to be prepended to the EPUB spine.</p>
        </p:documentation>
    </p:option>
    
    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- when `include-obfl` is set to true, the conversion may fail but still output a document
             on the "obfl" port -->
        <p:pipe step="convert" port="status"/>
    </p:output>

    <p:option name="stylesheet">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
          <p px:role="desc" xml:space="preserve" px:inherit="prepend">

A number of [partials](https://sass-lang.com/documentation/at-rules/import#partials) (helper style
sheet modules) are available for use in Sass style sheets:

- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_generate-toc.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/_generate-toc.scss/):
  for generating a table of content
- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_tables.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/tables):
  for styling tables
- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_definition-lists.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/definition-lists):
  for styling definition lists
- [http://www.daisy.org/pipeline/modules/braille/html-to-pef/_legacy.scss](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/resources/css/_legacy.scss/):
  collection of styles that used to be included by default
</p>
        </p:documentation>
    </p:option>
    
    <p:option name="apply-document-specific-stylesheets" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply document-specific CSS</h2>
            <p px:role="desc" xml:space="preserve">If this option is enabled, any pre-existing CSS in the EPUB with media "embossed" (or "all") will be used.

The input EPUB may already contain CSS that applies to embossed media.  Such document-specific CSS
takes precedence over any CSS attached when running this script.

For instance, if the EPUB already contains the rule `p { padding-left: 2; }`,
and using this script the rule `p#docauthor { padding-left: 4; }` is provided, then the
`padding-left` property will get the value `2` because that's what was defined in the EPUB,
even though the provided CSS is more specific.
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

    <!-- defined in ../../../../../../html-to-pef/src/main/resources/css/page-layout.params -->
    <p:option name="page-width"/>
    <p:option name="page-height"/>
    <p:option name="duplex"/>

    <!-- defined in ../../../../../../html-to-pef/src/main/resources/css/dotify.params -->
    <p:option name="hyphenation-at-page-breaks"/>
    <p:option name="allow-text-overflow-trimming"/>

    <!-- defined in ../../../../../../common-options.xpl -->
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
            px:epub3-to-pef.load
            px:epub3-to-pef
            px:epub3-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:delete-parameters
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
        </p:documentation>
    </p:import>
    
    <!-- ================================================= -->
    <!-- Create a <c:param-set/> of the options            -->
    <!-- ================================================= -->
    <!-- ...for easy piping so we won't have to explicitly -->
    <!-- pass all the variables all the time.              -->
    <!-- ================================================= -->
    <p:in-scope-names name="in-scope-names"/>
    <px:delete-parameters name="input-options" px:message="Collecting parameters" px:progress=".01"
                          parameter-names="epub
                                           preamble
                                           stylesheet
                                           stylesheet-parameters
                                           apply-document-specific-stylesheets
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
    
    <!-- ============================= -->
    <!-- LOAD EPUB 3 and PREAMBLE HTML -->
    <!-- ============================= -->
    <px:epub3-to-pef.load name="load" px:message="Loading EPUB" px:progress=".04">
        <p:with-option name="epub" select="$source"/>
        <p:with-option name="preamble" select="$preamble"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'load/')"/>
    </px:epub3-to-pef.load>
    <px:fileset-load name="opf" media-types="application/oebps-package+xml">
        <p:input port="in-memory">
            <p:pipe step="load" port="in-memory.out"/>
        </p:input>
    </px:fileset-load>
    <p:sink/>
    
    <!-- ============= -->
    <!-- EPUB 3 TO PEF -->
    <!-- ============= -->
    <p:identity>
        <p:input port="source">
            <p:pipe port="fileset.out" step="load"/>
        </p:input>
    </p:identity>
    <px:epub3-to-pef name="convert" px:message="Converting from EPUB to PEF" px:progress=".90">
        <p:with-option name="epub" select="$source"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat($temp-dir,'convert/')"/>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="stylesheet-parameters" select="$stylesheet-parameters"/>
        <p:with-option name="apply-document-specific-stylesheets" select="$apply-document-specific-stylesheets"/>
        <p:with-option name="transform"
                       select="concat($braille-code,($transform,'(translator:liblouis)(formatter:dotify)')[not(.='')][1])"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
        <p:input port="parameters">
            <p:pipe port="result" step="input-options"/>
        </p:input>
    </px:epub3-to-pef>
    <p:sink/>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <p:identity>
        <p:input port="source">
            <p:pipe step="convert" port="in-memory.out"/>
        </p:input>
    </p:identity>
    <p:for-each>
        <p:delete match="/*/@xml:base"/>
    </p:for-each>
    <px:epub3-to-pef.store px:message="Storing results" px:progress=".05">
        <p:with-option name="epub" select="$source"/>
        <p:input port="opf">
            <p:pipe step="opf" port="result"/>
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
    </px:epub3-to-pef.store>
    
</p:declare-step>
