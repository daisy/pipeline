<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                px:input-filesets="obfl"
                px:output-filesets="pef"
                type="px:obfl-to-pef.script"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">OBFL to braille</h1>
        <p px:role="desc" xml:space="preserve">Transforms an Open Braille Formatting Language (OBFL) document into an embosser ready braille document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/obfl-to-pef/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Bert Frees</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
            </dl>
        </address>
    </p:documentation>

    <p:input port="source" px:media-type="application/x-obfl+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input OBFL</h2>
            <p px:role="desc" xml:space="preserve">The OBFL document to convert.

The OBFL document describes how the text is to be formatted and
transcribed to braille. The text may also have been transcribed to
braille already.

A typical use case is when the "OBFL" output of one of the braille
production scripts has been edited, and needs to be transformed to a
formatted braille document.

The following special metadata
([`meta`](https://braillespecs.github.io/obfl/obfl-specification.html#L587))
fields in the "http://www.daisy.org/ns/pipeline/" namespace are
supported:

- `style-type` (supported value: "text/css"): specifies the media type of
  [`style`](https://braillespecs.github.io/obfl/obfl-specification.html#L8491)
  elements. The value "text/css" indicates that the styles (`name`
  attributes) are to be interpreted according to [braille
  CSS](http://braillespecs.github.io/braille-css) rules.
- `css-text-transform-definitions`: zero or more
  `@-daisy-text-transform` at-rules containing custom text
  transformation definitions, available for use in
  [`-daisy-text-transform`](https://braillespecs.github.io/braille-css/#dfn-text-transform)
  properties in CSS styles. May be used e.g. to switch between braille
  codes, or to apply emphasis.
- `css-hyphenation-resource-definitions`: zero or more
  `@-daisy-hyphenation-resource` at-rules containing custom hyphenation
  resource definitions for specific languages.
- `css-counter-style-definitions`: zero or more
  [`@counter-style`](https://braillespecs.github.io/braille-css/#dfn-counter-style)
  at-rules containing custom counter style definitions, available for
  use in `-dotify-counter-style` properties.
- `default-mode`: specifies the overall braille transcription
  mode/policy, i.e. the mode to be used when no
  [`translate`](https://braillespecs.github.io/obfl/obfl-specification.html#L2462)
  attribute has been set, or when it has been set to the empty
  value. Both the `default-mode` meta field and the `translate`
  attribute support the [braille transformer
  query](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/)
  syntax.
- `braille-charset`: specifies the braille character set that is used
  to encode braille present in the OBFL. If not specified, Unicode
  braille is assumed.</p>
        </p:documentation>
    </p:input>

    <!-- defined in ../../../../../../scripts/common-options.xpl -->
    <p:option name="include-preview"/>
    <p:option name="include-pdf"/>
    <p:option name="include-pef"/>
    <p:option name="output-file-format"/>
    <p:option name="preview-table"/>
    <p:option name="result"/>
    <p:option name="pef"/>
    <p:option name="preview"/>
    <p:option name="pdf"/>

    <!-- defined in ../../../../../../scripts/html-to-pef/src/main/resources/css/dotify.params -->
    <p:option name="hyphenation-at-page-breaks"/>
    <p:option name="allow-text-overflow-trimming"/>

    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl">
        <p:documentation>
            px:pef-store
        </p:documentation>
    </p:import>
    <p:import href="obfl-to-pef.xpl">
        <p:documentation>
            px:obfl-to-pef
        </p:documentation>
    </p:import>

    <p:variable name="name" select="replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1')"/>

    <px:obfl-to-pef name="convert" px:message="Converting from OBFL to PEF" px:progress=".90">
        <p:with-param port="parameters" name="hyphenation-at-page-breaks" select="$hyphenation-at-page-breaks"/>
        <p:with-param port="parameters" name="allow-text-overflow-trimming" select="$allow-text-overflow-trimming"/>
    </px:obfl-to-pef>

    <px:pef-store px:progress=".10">
        <p:with-option name="pef-href" select="if ($include-pef='true' and $pef!='')
                                               then concat($pef,'/',$name,'.pef')
                                               else ''"/>
        <p:with-option name="preview-href" select="if ($include-preview='true' and $preview!='')
                                                   then concat($preview,'/',$name,'.pef.html')
                                                   else ''"/>
        <p:with-option name="pdf-href" select="if ($include-pdf='true' and $pdf!='')
                                               then concat($pdf,'/',$name,'.pdf')
                                               else ''"/>
        <p:with-option name="output-dir" select="$result"/>
        <p:with-option name="name-pattern" select="concat($name,'_vol-{}')"/>
        <p:with-option name="single-volume-name" select="$name"/>
        <p:with-option name="file-format" select="concat(($output-file-format,'(format:pef)')[not(.='')][1],
                                                         '(document-locale:',(//pef:meta/dc:language,'und')[1],')')"/>
        <p:with-option name="preview-table" select="if ($preview-table!='') then $preview-table
                                                    else concat('(document-locale:',(//pef:meta/dc:language,'und')[1],')')"/>
    </px:pef-store>

</p:declare-step>
