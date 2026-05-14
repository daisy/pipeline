<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all"
                type="px:epub3-to-ebraille.script"
                name="main"
                px:input-filesets="epub3"
                px:output-filesets="ebraille">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 to eBraille</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into an eBraille publication.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub3-to-ebraille/">
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

    <p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/epub+zip">
        <p:documentation>
            <h2 px:role="name">Input EPUB 3</h2>
            <p px:role="desc" xml:space="preserve">The EPUB to be transformed.</p>
        </p:documentation>
    </p:option>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">eBraille</h2>
            <p px:role="desc">The resulting eBraille publication.</p>
        </p:documentation>
    </p:option>

    <p:option name="ebraille-stylesheet" required="false" px:type="anyFileURI" select="''" px:sequence="true" px:separator=" "
              px:reusable="true" px:media-type="text/css text/x-scss">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">eBraille style sheets</h2>
            <p px:role="desc" xml:space="preserve">CSS style sheet(s) to be attached to the HTML documents of the eBraille publication.

The style sheets are associated with each HTML file through `link` elements. No media query (`media`
attribute) is specified on the `link` elements. If media queries are needed, they must be specified
in the CSS itself, through `@media` and `@import` rules.

The style sheets are included as-is, and should therefore conform to the [eBraille standard for
CSS](https://daisy.github.io/ebraille/#html-css).</p>
        </p:documentation>
    </p:option>

    <p:option name="braille-code" select="''">
        <!-- defined in ../../../../../../common-options.xpl -->
    </p:option>

    <!--<p:option name="braille-translator" required="false" px:type="transform-query" select="''"/>-->

    <p:option name="stylesheet" required="false" px:type="anyURI" select="''" px:sequence="true" px:separator=" "
              px:reusable="true" px:media-type="text/css text/x-scss">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Braille transcription style sheets</h2>
            <p px:role="desc" xml:space="preserve">A list of CSS/Sass style sheets to take into account for braille transcription.

Must be a space separated list of URIs, absolute or relative to the input.

Note that styles that do not influence the braille transcription, are ignored, and CSS provided
through this option will not end up in the eBraille publication. The "eBraille style sheets" option
should be used for that purpose.

All style sheets are applied at once, but the order in which they are specified has an influence on
the [cascading order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order).

Style sheets are interpreted according to [braille CSS](http://braillespecs.github.io/braille-css)
rules.

For info on how to use Sass (Syntactically Awesome StyleSheets) see the [Sass
manual](http://sass-lang.com/documentation/file.SASS_REFERENCE.html).</p>
        </p:documentation>
    </p:option>

    <p:option name="stylesheet-parameters" required="false" px:type="stylesheet-parameters" select="'()'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Braille transcription style sheet parameters</h2>
            <p px:role="desc" xml:space="preserve">A list of parameters passed to the braille transcription style sheets.

Style sheets may have parameters (Sass variables). This option, which takes a comma-separated list
of key-value pairs enclosed in parenthesis, can be used to set these variables.

For example, if a style sheet uses the Sass variable "foo":

~~~sass
@if $foo {
   /* some style that should only be enabled when "foo" is truthy */
}
~~~

you can control that variable with the following parameters list: `(foo:true)`.</p>
        </p:documentation>
    </p:option>

    <p:option name="epub-package" required="false" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Package in EPUB container</h2>
            <p px:role="desc" xml:space="preserve">Whether to package the eBraille publication in an EPUB 3-compatible container, or store it unzipped.

When packaged in an EPUB container, the output is a single file with
file extension `.ebrl`. When stored unzipped, the eBraille file set is
contained in a directory.</p>
        </p:documentation>
    </p:option>

    <p:option name="include-original-text" cx:as="xs:boolean" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include original text</h2>
            <p px:role="desc">Include the original text as a secondary rendition.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:import href="epub3-to-epub3.xpl">
        <p:documentation>
            px:epub3-to-epub3
        </p:documentation>
    </p:import>
    <p:import href="../ocf/load.xpl">
        <p:documentation>
            px:epub-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
            px:fileset-filter
        </p:documentation>
    </p:import>

    <px:epub-load version="3" store-to-disk="true" name="load" px:message="Loading EPUB">
        <p:with-option name="href" select="$source"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'load/')"/>
    </px:epub-load>

    <px:epub3-to-epub3 name="convert" px:progress="0.9">
        <p:input port="source.in-memory">
            <p:pipe step="load" port="result.in-memory"/>
        </p:input>
        <p:with-option name="braille-translator" select="$braille-code"/>
        <p:with-option name="stylesheet" select="string-join(
                                                   for $s in tokenize($stylesheet,'\s+')[not(.='')] return
                                                     resolve-uri($s,$source),
                                                   ' ')"/>
        <p:with-option name="stylesheet-parameters" select="$stylesheet-parameters"/>
        <p:with-option name="apply-document-specific-stylesheets" select="'false'"/>
        <p:with-option name="braille" select="'true'"/>
        <p:with-option name="ebraille-compatibility" select="'strict'"/>
        <p:with-option name="delete-original-rendition" select="not($include-original-text)"/>
        <p:with-option name="ebraille-stylesheet" select="tokenize($ebraille-stylesheet,'\s+')[not(.='')]"/>
        <p:with-option name="tts" select="'false'"/>
        <p:with-option name="result-base"
                       select="if ($epub-package)
                               then concat($result,'/',
                                           replace(replace($source,'\.epub$',''),'^.*/([^/]+)$','$1'),
                                           '.ebrl!/')
                               else $result"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'convert/')"/>
    </px:epub3-to-epub3>

    <p:choose px:progress="0.1">
        <p:when test="$epub-package" px:message="Storing in EPUB 3 package">
            <px:fileset-store px:progress="1">
                <p:input port="in-memory.in">
                    <p:pipe step="convert" port="result.in-memory"/>
                </p:input>
            </px:fileset-store>
        </p:when>
        <p:otherwise px:message="Storing">
            <p:documentation>
                META-INF/container.xml and mimetype may be omitted when the eBraille publication is
                not packaged in an EPUB container.
            </p:documentation>
            <px:fileset-filter name="filter-mimetype" href="mimetype">
                <!-- assumes $result-base is the fileset base -->
                <p:input port="source.in-memory">
                    <p:pipe step="convert" port="result.in-memory"/>
                </p:input>
            </px:fileset-filter>
            <p:choose name="filter-container">
                <p:when test="$include-original-text">
                    <p:output port="fileset" primary="true">
                        <p:pipe step="filter-mimetype" port="not-matched"/>
                    </p:output>
                    <p:output port="in-memory" sequence="true">
                        <p:pipe step="filter-mimetype" port="not-matched.in-memory"/>
                    </p:output>
                    <p:sink/>
                </p:when>
                <p:otherwise>
                    <!-- FIXME: actually we can only do this if the input does not contain
                         multiple renditions -->
                    <p:output port="fileset" primary="true">
                        <p:pipe step="filter" port="not-matched"/>
                    </p:output>
                    <p:output port="in-memory" sequence="true">
                        <p:pipe step="filter" port="not-matched.in-memory"/>
                    </p:output>
                    <p:sink/>
                    <px:fileset-filter name="filter" href="META-INF/container.xml">
                        <p:input port="source">
                            <p:pipe step="filter-mimetype" port="not-matched"/>
                        </p:input>
                        <p:input port="source.in-memory">
                            <p:pipe step="filter-mimetype" port="not-matched.in-memory"/>
                        </p:input>
                    </px:fileset-filter>
                    <p:sink/>
                </p:otherwise>
            </p:choose>
            <px:fileset-store>
                <p:input port="in-memory.in">
                    <p:pipe step="filter-container" port="in-memory"/>
                </p:input>
            </px:fileset-store>
        </p:otherwise>
    </p:choose>

</p:declare-step>
