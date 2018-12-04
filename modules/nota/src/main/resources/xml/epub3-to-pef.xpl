<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="nota:epub3-to-pef" version="1.0"
                xmlns:nota="http://www.nota.dk"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:ocf="urn:oasis:names:tc:opendocument:xmlns:container"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:opf="http://www.idpf.org/2007/opf"
                exclude-inline-prefixes="#all"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 to PEF (Nota)</h1>
        <p px:role="desc">Transforms a EPUB 3 publication into a PEF.</p>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:sequence="false"
              px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input EPUB 3</h2>
            <p px:role="desc" xml:space="preserve">The EPUB you want to convert to braille. You may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="pef-output-dir"/>
    <p:option name="brf-output-dir"/>
    <p:option name="preview-output-dir"/>
    <p:option name="temp-dir"/>
    <p:option name="stylesheet"/>
    <p:option name="contraction-grade"/>
    <p:option name="ascii-table"/>
    <p:option name="ascii-file-format"/>
    <p:option name="include-preview"/>
    <p:option name="include-brf"/>
    <p:option name="page-width"/>
    <p:option name="page-height"/>
    <p:option name="left-margin"/>
    <p:option name="duplex"/>
    <p:option name="levels-in-footer"/>
    <p:option name="main-document-language"/>
    <p:option name="hyphenation"/>
    <p:option name="line-spacing"/>
    <p:option name="tab-width"/>
    <p:option name="capital-letters"/>
    <p:option name="accented-letters"/>
    <p:option name="polite-forms"/>
    <p:option name="downshift-ordinal-numbers"/>
    <p:option name="include-captions"/>
    <p:option name="include-images"/>
    <p:option name="include-image-groups"/>
    <p:option name="include-line-groups"/>
    <p:option name="text-level-formatting"/>
    <p:option name="include-note-references"/>
    <p:option name="include-production-notes"/>
    <p:option name="show-braille-page-numbers"/>
    <p:option name="show-print-page-numbers"/>
    <p:option name="force-braille-page-break"/>
    <p:option name="toc-depth"/>
    <p:option name="footnotes-placement"/>
    <p:option name="colophon-metadata-placement"/>
    <p:option name="rear-cover-placement"/>
    <p:option name="number-of-sheets"/>
    <p:option name="maximum-number-of-sheets"/>
    <p:option name="minimum-number-of-sheets"/>
    
    <!-- ======= -->
    <!-- Imports -->
    <!-- ======= -->
    <p:import href="http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/library.xpl">
        <p:documentation>
            px:epub3-to-pef.load
            px:epub3-to-pef.convert
            px:epub3-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:tempdir
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:merge-parameters
            px:delete-parameters
        </p:documentation>
    </p:import>
    
    <p:in-scope-names name="in-scope-names"/>
    <px:merge-parameters>
        <p:input port="source">
            <p:pipe port="result" step="in-scope-names"/>
        </p:input>
    </px:merge-parameters>
    <px:delete-parameters parameter-names="stylesheet
                                           ascii-table
                                           include-brf
                                           include-preview
                                           pef-output-dir
                                           brf-output-dir
                                           preview-output-dir
                                           temp-dir"/>
    <p:identity name="input-options"/>
    <p:sink/>
    
    <!-- =============== -->
    <!-- CREATE TEMP DIR -->
    <!-- =============== -->
    <px:tempdir name="temp-dir">
        <p:with-option name="href" select="if ($temp-dir!='') then $temp-dir else $pef-output-dir"/>
    </px:tempdir>
    
    <!-- =========== -->
    <!-- LOAD EPUB 3 -->
    <!-- =========== -->
    <px:message message="Loading EPUB"/>
    <px:epub3-to-pef.load name="load">
        <p:with-option name="epub" select="$epub"/>
        <p:with-option name="temp-dir" select="concat(string(/c:result),'load/')">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
    </px:epub3-to-pef.load>
    
    <!-- ============= -->
    <!-- EPUB 3 TO PEF -->
    <!-- ============= -->
    <p:identity>
        <p:input port="source">
            <p:pipe port="fileset.out" step="load"/>
        </p:input>
    </p:identity>
    <px:message message="Done loading EPUB, starting conversion to PEF"/>
    <px:epub3-to-pef.convert default-stylesheet="http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/css/default.css"
                             apply-document-specific-stylesheets="false"
                             include-obfl="false"
                             name="convert">
        <p:with-option name="epub" select="$epub"/>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat(string(/c:result),'convert/')">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
        <p:with-option name="stylesheet" select="string-join((
                                                  'http://www.nota.dk/pipeline/modules/braille/internal/insert-titlepage.xsl',
                                                  'http://www.nota.dk/pipeline/modules/braille/internal/duplicate-tables.xsl',
                                                  'http://www.nota.dk/pipeline/modules/braille/internal/mark-noterefs.xsl',
                                                  for $s in tokenize($stylesheet,'\s+')[not(.='')]
                                                    return resolve-uri($s,$epub)),' ')"/>
        <p:with-option name="transform" select="concat('(formatter:dotify)(translator:nota)(grade:',$contraction-grade,')')"/>
        <p:input port="parameters">
            <p:pipe port="result" step="input-options"/>
        </p:input>
    </px:epub3-to-pef.convert>
    <p:sink/>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <p:identity>
        <p:input port="source">
            <p:pipe step="convert" port="in-memory.out"/>
        </p:input>
    </p:identity>
    <px:message message="Storing PEF"/>
    <p:delete match="/*/@xml:base"/>
    <px:epub3-to-pef.store>
        <p:with-option name="epub" select="$epub"/>
        <p:input port="opf">
            <p:pipe step="load" port="opf"/>
        </p:input>
        <p:input port="obfl">
            <p:pipe step="convert" port="obfl"/>
        </p:input>
        <p:with-option name="include-brf" select="$include-brf"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="ascii-file-format" select="$ascii-file-format"/>
        <p:with-option name="ascii-table" select="$ascii-table"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="brf-output-dir" select="$brf-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
    </px:epub3-to-pef.store>
    
</p:declare-step>
