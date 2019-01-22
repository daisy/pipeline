<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-epub3.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Braille in EPUB 3</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into an EPUB 3 publication with a braille rendition.</p>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Bert Frees</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
        </dl>
    </p:documentation>
    
    <p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/epub+zip text/plain">
        <p:documentation>
            <h2 px:role="name">Input EPUB 3</h2>
            <p px:role="desc" xml:space="preserve">The EPUB you want to convert. You may alternatively use the "mimetype" document if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="braille-translator" required="false" px:type="transform-query" select="'(translator:liblouis)'">
        <p:documentation>
            <h2 px:role="name">Braille translator query</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="stylesheet" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Style sheets</h2>
            <p px:role="desc" xml:space="preserve">CSS style sheets to apply. A space separated list of URIs, absolute or relative to source.

All CSS style sheets are applied at once, but the order in which they are specified has an influence
on the cascading order.

If the "Apply document-specific CSS" option is enabled, the document-specific style sheets will be
applied before the ones specified through this option (see below).
</p>
        </p:documentation>
    </p:option>
    
    <p:option name="apply-document-specific-stylesheets" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply document-specific CSS</h2>
            <p px:role="desc" xml:space="preserve">If this option is enabled, any pre-existing CSS in the EPUB for medium "embossed" will be taken into account for the translation, or preserved in the result EPUB.

The HTML files inside the source EPUB may already contain CSS that applies to embossed media. Style
sheets can be associated with an HTML file in several ways: linked (using an 'xml-stylesheet'
processing instruction or a 'link' element), embedded (using a 'style' element) and/or inlined
(using 'style' attributes).

Document-specific CSS takes precedence over any CSS provided through the "Style sheets" option. For
instance, if the EPUB already contains the rule `p { padding-left: 2; }`, and using this script the
rule `p#docauthor { padding-left: 4; }` is provided, then the `padding-left` property will get the
value `2` because that's what was defined in the EPUB, even though the provided CSS is more
specific.
</p>
        </p:documentation>
    </p:option>
    
    <p:option name="set-default-rendition-to-braille" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Set default rendition to braille.</h2>
            <p px:role="desc">Make the generated braille rendition the default rendition.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation>
            <h2 px:role="name">Output EPUB 3</h2>
        </p:documentation>
    </p:option>
    
    <p:import href="epub3-to-epub3.load.xpl"/>
    <p:import href="epub3-to-epub3.convert.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <px:epub3-to-epub3.load name="load">
        <p:with-option name="epub" select="$source"/>
    </px:epub3-to-epub3.load>
    
    <px:epub3-to-epub3 name="convert">
        <p:input port="epub.in.fileset">
            <p:pipe step="load" port="fileset"/>
        </p:input>
        <p:input port="epub.in.in-memory">
            <p:pipe step="load" port="in-memory"/>
        </p:input>
        <p:with-option name="result-base"
                       select="concat($output-dir,'/',replace(replace($source,'(\.epub|/mimetype)$',''),'^.*/([^/]+)$','$1'),'.epub!/')"/>
        <p:with-option name="braille-translator" select="$braille-translator"/>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="apply-document-specific-stylesheets" select="$apply-document-specific-stylesheets"/>
        <p:with-option name="set-default-rendition-to-braille" select="$set-default-rendition-to-braille"/>
    </px:epub3-to-epub3>
    
    <px:fileset-store>
        <p:input port="fileset.in">
            <p:pipe step="convert" port="epub.out.fileset"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe step="convert" port="epub.out.in-memory"/>
        </p:input>
    </px:fileset-store>
    
</p:declare-step>
