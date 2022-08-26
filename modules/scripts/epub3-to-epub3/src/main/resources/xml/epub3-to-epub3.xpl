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
    
    <p:input port="metadata" primary="false" sequence="true" px:media-type="application/xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Metadata</h2>
            <p px:role="desc" xml:space="preserve">Metadata to be included in the EPUB.

If specified, the document must be a single
[`metadata`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-metadata-elem) or
[`package`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-package-elem) element. A
[`prefix`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-prefix-attr) attribute is
allowed on the root element. The metadata will be injected in the EPUB's package document, possibly
overwriting existing metadata. This works as follows:

- All (valid) fields in the provided metadata document end up in the output EPUB. More than one
  field with the same property is allowed. `meta` elements with a `refines` attribute must refine
  elements within the metadata document itself. Elements that refine elements in the EPUB's package
  document will be dropped.
- Any metadata fields in the input EPUB that have matching fields (same property in case of `meta`
  fields, same element name in case of `dc:*` fields) in the provided metadata document are omitted,
  together with any `meta` elements that refine them.
- Metadata fields in the input that do not have any matching fields in the provided metadata
  document are preserved in the output.

There are a number of fields that result in addional changes in the EPUB (apart from an updated
`metadata` section in the package document):

- If the provided metadata document contains one or more
  [`dc:identifier`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dcidentifier)
  fields, the first one without a `refines` attribute will be used to update the
  [`unique-identifier`](https://www.w3.org/publishing/epub3/epub-packages.html#attrdef-package-unique-identifier)
  attribute on the package document. The `dc:identifier` metadata in the content documents can also be
  aligned with it. This behavior can be enabled or disabled with the "Update &lt;meta
  name='dc:identifier'&gt; elements based on EPUB metadata" option.
- If the provided metadata document contains one or more
  [`dc:title`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dctitle) fields, the
  first one can also be used as the `title` element in the content documents. This behavior can be
  enabled or disabled with the "Update &lt;title&gt; elements based on EPUB metadata" option.
- If the provided metadata document contains exactly one
  [`dc:language`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dclanguage) field,
  it can be used to update the `xml:lang` and `lang` attributes of the content documents. This
  behavior can be enabled or disabled with the "Update 'lang' attributes based on metadata" option.

Some fields are ignored:

- The [`dcterms:modified`](https://www.w3.org/publishing/epub3/epub-packages.html#last-modified-date)
  field gets updated whenever Pipeline produces an EPUB. As a consequence, any `dcterms:modified`
  fields in the provided metadata document are ignored.</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:option name="update-lang-attributes" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Update 'lang' attributes based on metadata</h2>
            <p px:role="desc" xml:space="preserve">Whether to update 'lang' and 'xml:lang' attributes of content documents based on metadata in the package document.

If there is exactly one
[`dc:language`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dclanguage) element,
use its value to create `xml:lang` and `lang` attributes on the root elements of all content
documents (overwriting any existing attributes)

If the "Metadata" option is used to inject new metadata into the EPUB, the resulting metadata is
used to generate the attributes.</p>
        </p:documentation>
    </p:option>

    <p:option name="update-identifier-in-content-docs" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Update &lt;meta name='dc:identifier'&gt; elements based on EPUB metadata</h2>
            <p px:role="desc" xml:space="preserve">Whether to update &lt;meta name='dc:identifier'&gt; elements of content documents based on metadata in the package document.

Use the primary identifier (provided by the
[`dc:identifier`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dcidentifier)
element identified by the
[`unique-identifier`](https://www.w3.org/publishing/epub3/epub-packages.html#attrdef-package-unique-identifier)
attribute) to create a `&lt;meta name='dc:identifier'&gt;` element in all content documents
(overwriting any existing elements with the same name).

If the "Metadata" option is used to inject new metadata into the EPUB, the resulting metadata is
used to generate the attributes.</p>
        </p:documentation>
    </p:option>

    <p:option name="update-title-in-content-docs" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Update &lt;title&gt; elements based on EPUB metadata</h2>
            <p px:role="desc" xml:space="preserve">Whether to update &lt;title&gt; elements of content documents based on metadata in the package document.

If there is one or more
[`dc:title`](https://www.w3.org/publishing/epub3/epub-packages.html#sec-opf-dctitle) element, use
the value of the first one to create a `&lt;title&gt;` element in all content documents (overwriting
any existing elements with the same name).

If the "Metadata" option is used to inject new metadata into the EPUB, the resulting metadata is
used to generate the attributes.</p>
        </p:documentation>
    </p:option>

    <p:option name="ensure-pagenum-text" required="false" select="'false'">
        <p:pipeinfo>
            <px:type>
                <choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
                    <value>true</value>
                    <a:documentation xml:lang="en">Yes</a:documentation>
                    <value>false</value>
                    <a:documentation xml:lang="en">No</a:documentation>
                    <value>hidden</value>
                    <a:documentation xml:lang="en">Yes, but not visible</a:documentation>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Ensure text content for page numbers</h2>
            <p px:role="desc" xml:space="preserve">Whether to fix empty page number elements.

Page number elements (elements with a `doc-pagebreak` `role` or `pagebreak` `epub:type`) that have
no child text node can be given one. The text can be generated based on

- the element's `aria-label` attribute,
- the element's `title` attribute, or
- the text used by the corresponding page link in the navigation document.

These options are tried in the listed order. If none of the attributes exist, and the page is linked
from the navigation document, no text is generated.</p>
        </p:documentation>
    </p:option>

    <p:option name="ensure-section-headings" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Ensure headings for all sections</h2>
            <p px:role="desc" xml:space="preserve">Whether to generate a heading element for sections that don't have one.

For sectioning elements that don't have a heading element, one can be created. The headings are
generated based on the section element's [`aria-label`](https://www.w3.org/TR/wai-aria/#aria-label)
attribute. If the `aria-label` attribute is not present, no heading element is generated. When an
`aria-label` is used to generate a heading, it is replaced with a
[`aria-labelledby`](https://www.w3.org/TR/wai-aria/#aria-labelledby) attribute that points to the
new heading. The rank of the generated heading matches the depth of the corresponding TOC item in
the navigation document.</p>
        </p:documentation>
    </p:option>

    <p:option name="braille" required="false" px:type="boolean" select="'false'">
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
    
    <p:option name="sentence-detection" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Perform sentence detection</h2>
            <p px:role="desc" xml:space="preserve">Whether to add markup (span elements) for sentences.

This setting has no effect when text-to-speech is also enabled. In that case sentences are always
marked up.</p>
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
    
    <p:option name="sentence-class" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Sentence class</h2>
            <p px:role="desc" xml:space="preserve">Class attribute to mark sentences with.

When sentence detection is enabled, this option may be used to add a class attribute to the `span`
elements that represent the sentences.</p>
        </p:documentation>
    </p:option>

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
    
    <p:option name="include-tts-log" select="'false'">
        <!-- defined in common-options.xpl -->
    </p:option>
    <p:output port="tts-log" sequence="true">
        <!-- defined in common-options.xpl -->
        <p:pipe step="convert" port="tts-log"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub-load
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

    <px:epub-load version="3" store-to-disk="true" name="load" px:progress="0.1" px:message="Loading EPUB">
        <p:with-option name="href" select="$source"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'load/')"/>
    </px:epub-load>
    
    <px:epub3-to-epub3 name="convert" px:progress="0.8">
        <p:input port="source.in-memory">
            <p:pipe step="load" port="result.in-memory"/>
        </p:input>
        <p:with-option name="result-base"
                       select="concat($output-dir,'/',replace(replace($source,'(\.epub|/mimetype)$',''),'^.*/([^/]+)$','$1'),'.epub!/')"/>
        <p:input port="metadata">
            <p:pipe port="metadata" step="main"/>
        </p:input>
        <p:with-option name="braille-translator" select="$braille-translator"/>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="apply-document-specific-stylesheets" select="$apply-document-specific-stylesheets"/>
        <p:with-option name="set-default-rendition-to-braille" select="$set-default-rendition-to-braille"/>
        <p:with-option name="braille" select="$braille"/>
        <p:with-option name="tts" select="$tts"/>
        <p:with-option name="sentence-detection" select="$sentence-detection"/>
        <p:with-option name="update-lang-attributes" select="$update-lang-attributes"/>
        <p:with-option name="update-identifier-in-content-docs" select="$update-identifier-in-content-docs"/>
        <p:with-option name="update-title-in-content-docs" select="$update-title-in-content-docs"/>
        <p:with-option name="ensure-pagenum-text" select="$ensure-pagenum-text"/>
        <p:with-option name="ensure-section-headings" select="$ensure-section-headings"/>
        <p:with-option name="sentence-class" select="$sentence-class"/>
        <p:with-option name="include-tts-log" select="$include-tts-log"/>
        <p:input port="tts-config">
            <p:pipe step="main" port="tts-config"/>
        </p:input>
        <p:with-option name="temp-dir" select="concat($temp-dir,'convert/')"/>
    </px:epub3-to-epub3>
    
    <px:fileset-store name="store" px:progress="0.1" px:message="Storing EPUB">
        <p:input port="in-memory.in">
            <p:pipe step="convert" port="result.in-memory"/>
        </p:input>
    </px:fileset-store>
    
    <px:fileset-delete cx:depends-on="store">
        <p:input port="source">
            <p:pipe step="convert" port="temp-audio-files"/>
        </p:input>
    </px:fileset-delete>
    
</p:declare-step>
