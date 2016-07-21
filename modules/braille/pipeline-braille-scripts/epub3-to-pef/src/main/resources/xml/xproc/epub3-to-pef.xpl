<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef" version="1.0"
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
        <h1 px:role="name">EPUB 3 to PEF</h1>
        <p px:role="desc" xml:space="preserve">Transforms a EPUB 3 publication into a PEF.</p>
        <p>Extends <a href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/xml-to-pef.xpl">XML to PEF</a>.</p>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:sequence="false" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input EPUB 3</h2>
            <p px:role="desc" xml:space="preserve">The EPUB you want to convert to braille. You may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="stylesheet"/>
    
    <p:option name="apply-document-specific-stylesheets" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply document-specific CSS</h2>
            <p px:role="desc" xml:space="preserve">If this option is enabled, any pre-existing CSS in the EPUB with `media="embossed"` will be used.

The input EPUB may already contain CSS that applies to embossed media (using media="embossed").
Such document-specific CSS takes precedence over any CSS attached when running this script.

For instance, if the EPUB already contains the rule `p { padding-left: 2; }`,
and using this script the rule `p#docauthor { padding-left: 4; }` is provided, then the
`padding-left` property will get the value `2` because that's what was defined in the EPUB,
even though the provided CSS is more specific.
            </p>
        </p:documentation>
    </p:option>
    
    <p:option name="transform"/>
    <p:option name="include-preview"/>
    <p:option name="include-brf"/>
    <p:option name="include-obfl"/>
    <p:option name="ascii-file-format"/>
    <p:option name="ascii-table"/>
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
    <p:option name="pef-output-dir"/>
    <p:option name="brf-output-dir"/>
    <p:option name="preview-output-dir"/>
    <p:option name="temp-dir"/>
    
    <!-- ======= -->
    <!-- Imports -->
    <!-- ======= -->
    <p:import href="http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <!-- ================================================= -->
    <!-- Create a <c:param-set/> of the options            -->
    <!-- ================================================= -->
    <!-- ...for easy piping so we won't have to explicitly -->
    <!-- pass all the variables all the time.              -->
    <!-- ================================================= -->
    <p:in-scope-names name="in-scope-names"/>
    <px:delete-parameters name="input-options"
                          parameter-names="stylesheet
                                           apply-document-specific-stylesheets
                                           transform
                                           ascii-table
                                           ascii-file-format
                                           include-brf
                                           include-preview
                                           include-obfl
                                           pef-output-dir
                                           brf-output-dir
                                           preview-output-dir
                                           temp-dir">
        <p:input port="source">
            <p:pipe port="result" step="in-scope-names"/>
        </p:input>
    </px:delete-parameters>
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
    
    <!-- Get the OPF so that we can use the metadata in options -->
    <p:identity>
        <p:input port="source">
            <p:pipe port="fileset.out" step="load"/>
        </p:input>
    </p:identity>
    <px:message message="Getting the OPF"/>
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="in-memory">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="opf"/>
    <p:sink/>
    
    <!-- ============= -->
    <!-- EPUB 3 TO PEF -->
    <!-- ============= -->
    <p:identity>
        <p:input port="source">
            <p:pipe port="fileset.out" step="load"/>
        </p:input>
    </p:identity>
    <px:message message="Done loading EPUB, starting conversion to PEF"/>
    <px:epub3-to-pef.convert default-stylesheet="http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/css/default.css" name="convert">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat(string(/c:result),'convert/')">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
        <p:with-option name="stylesheet" select="string-join(for $s in tokenize($stylesheet,'\s+')[not(.='')]
                                                 return resolve-uri($s,$epub),' ')"/>
        <p:with-option name="apply-document-specific-stylesheets" select="$apply-document-specific-stylesheets"/>
        <p:with-option name="transform" select="$transform"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
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
    <px:xml-to-pef.store>
        <p:input port="obfl">
            <p:pipe step="convert" port="obfl"/>
        </p:input>
        <p:with-option name="name" select="if (ends-with(lower-case($epub),'.epub')) then replace($epub,'^.*/([^/]*)\.[^/\.]*$','$1')
                                           else (/opf:package/opf:metadata/dc:identifier[not(@refines)], 'unknown-identifier')[1]">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
        <p:with-option name="include-brf" select="$include-brf"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
        <p:with-option name="ascii-file-format" select="$ascii-file-format"/>
        <p:with-option name="ascii-table" select="$ascii-table"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="brf-output-dir" select="$brf-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
