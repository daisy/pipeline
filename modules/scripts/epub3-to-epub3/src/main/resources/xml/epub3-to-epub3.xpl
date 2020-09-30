<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-epub3.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 Enhancer</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into an EPUB 3 publication with audio and/or a braille rendition.</p>
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
            <p px:role="desc" xml:space="preserve">The EPUB you want to convert.

You may alternatively use the "mimetype" document if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="braille" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translate to braille</h2>
            <p px:role="desc">Whether to produce a braille rendition.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="tts" required="false" px:type="boolean" select="'default'">
        <p:pipeinfo>
            <px:type>
                <choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
                    <value>true</value>
                    <a:documentation xml:lang="en">Yes</a:documentation>
                    <value>false</value>
                    <a:documentation xml:lang="en">No</a:documentation>
                    <value>default</value>
                    <a:documentation xml:lang="en">If publication has no media overlays yet</a:documentation>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Perform text-to-speech</h2>
            <p px:role="desc" xml:space="preserve">Whether to use a speech synthesizer to produce media overlays.

This will remove any existing media overlays in the EPUB.</p>
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
    
    <p:input port="tts-config" primary="false">
        <!-- defined in common-options.xpl -->
        <p:inline><d:config/></p:inline>
    </p:input>
    
    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation>
            <h2 px:role="name">Output EPUB 3</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Directory used for temporary files.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="epub3-to-epub3.load.xpl">
        <p:documentation>
            px:epub3-to-epub3.load
        </p:documentation>
    </p:import>
    <p:import href="epub3-to-epub3.convert.xpl">
        <p:documentation>
            px:epub3-to-epub3
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
            px:fileset-delete
        </p:documentation>
    </p:import>

    <px:epub3-to-epub3.load name="load" px:progress="0.1">
        <p:with-option name="epub" select="$source"/>
    </px:epub3-to-epub3.load>
    
    <px:epub3-to-epub3 name="convert" px:progress="0.8">
        <p:input port="epub.in.in-memory">
            <p:pipe step="load" port="in-memory"/>
        </p:input>
        <p:with-option name="result-base"
                       select="concat($output-dir,'/',replace(replace($source,'(\.epub|/mimetype)$',''),'^.*/([^/]+)$','$1'),'.epub!/')"/>
        <p:with-option name="braille-translator" select="$braille-translator"/>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="apply-document-specific-stylesheets" select="$apply-document-specific-stylesheets"/>
        <p:with-option name="set-default-rendition-to-braille" select="$set-default-rendition-to-braille"/>
        <p:with-option name="braille" select="$braille"/>
        <p:with-option name="tts" select="$tts"/>
        <p:input port="tts-config">
            <p:pipe step="main" port="tts-config"/>
        </p:input>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub3-to-epub3>
    
    <px:fileset-store name="store" px:progress="0.1">
        <p:input port="in-memory.in">
            <p:pipe step="convert" port="epub.out.in-memory"/>
        </p:input>
    </px:fileset-store>
    
    <px:fileset-delete cx:depends-on="store">
        <p:input port="source">
            <p:pipe step="convert" port="temp-audio-files"/>
        </p:input>
    </px:fileset-delete>
    
</p:declare-step>
