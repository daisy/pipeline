<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-to-ebraille.script"
                px:input-filesets="dtbook"
                px:output-filesets="ebraille"
                exclude-inline-prefixes="#all"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to eBraille</h1>
        <p px:role="desc">Converts a DTBook to an eBraille publication</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-ebraille/">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The 2005-3 DTBook or NIMAS file to be transformed.</p>
        </p:documentation>
    </p:input>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">eBraille</h2>
            <p px:role="desc">The resulting eBraille publication.</p>
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

    <p:input port="metadata" sequence="true" px:media-type="application/xml">
        <p:empty/>
    </p:input>

    <p:option name="attach-stylesheet" required="false" px:type="anyFileURI" select="''" px:sequence="true" px:separator=" "
              px:media-type="text/css text/x-scss">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Attach CSS style sheets</h2>
            <p px:role="desc" xml:space="preserve">CSS style sheet(s) to be attached with the HTML documents of the eBraille publication.

The style sheets are associated with each HTML file through `link`
elements. This script does not allow specifying `media` attributes on
the `link` elements. If media queries are needed, they must be
specified in the CSS itself, through `@media` and `@import` rules.

The style sheets are included as-is, and should therefore apply to
HTML, not DTBook.</p>
        </p:documentation>
    </p:option>

    <p:option name="braille-code" select="''">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <!--<p:option name="braille-translator" required="false" px:type="transform-query" select="''"/>-->

    <p:option name="braille-translator-stylesheet" required="false" px:type="anyURI" select="''" px:sequence="true" px:separator=" "
              px:media-type="text/css text/x-scss">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Braille transcription style sheets</h2>
            <p px:role="desc" xml:space="preserve">A list of CSS/Sass style sheets to take into account for braille transcription.

Must be a space separated list of URIs, absolute or relative to the input.

Note that any CSS provided through this option will not end up in the eBraille publication. The
"Attach CSS style sheets" option should be used for that purpose.

Style sheets specified through this option are called "[user style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". Style sheets can also be attached to the
source document. These are referred to as "[author style
sheets](https://www.w3.org/TR/CSS2/cascade.html#cascade)". They can be linked (using an
['xml-stylesheet' processing instruction](https://www.w3.org/TR/xml-stylesheet) or a ['link'
element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using '[style'
attributes](https://www.w3.org/TR/css-style-attr/)). Only author styles that apply to "braille"
media are taken into account. Styles that do not influence the braille transcription, are ignored.

All style sheets are applied at once, but the order in which they are specified has an influence on
the [cascading order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order). Author styles take
precedence over user styles.

Style sheets are interpreted according to [braille
CSS](http://braillespecs.github.io/braille-css) rules.

For info on how to use Sass (Syntactically Awesome StyleSheets) see the [Sass
manual](http://sass-lang.com/documentation/file.SASS_REFERENCE.html).</p>
        </p:documentation>
    </p:option>

    <p:option name="braille-translator-stylesheet-parameters" required="false" px:type="stylesheet-parameters" select="'()'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Braille transcription style sheet parameters</h2>
            <p px:role="desc" xml:space="preserve">A list of parameters passed to the braille transcription style sheets.

Style sheets, whether they're user style sheets (specified with the "Braille transcription style
sheets" option) or author style sheets (associated with the source), may have parameters (Sass
variables). This option, which takes a comma-separated list of key-value pairs enclosed in
parenthesis, can be used to set these variables.

For example, if a style sheet uses the Sass variable "foo":

~~~sass
@if $foo {
   /* some style that should only be enabled when "foo" is truthy */
}
~~~

you can control that variable with the following parameters list: `(foo:true)`.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:option name="validation" select="'report'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:output port="validation-report" sequence="true">
        <!-- defined in ../../../../../common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml" primary="true">
      <!-- whether the conversion was aborted due to validation errors -->
    </p:output>

    <p:option name="nimas" select="'false'">
      <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="include-original-text" cx:as="xs:boolean" select="false()">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h2 px:role="name">Include original text</h2>
        <p px:role="desc">Include the original text as a secondary rendition.</p>
      </p:documentation>
    </p:option>

    <p:import href="dtbook-to-ebraille.xpl">
        <p:documentation>
            px:dtbook-to-ebraille
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
      <p:documentation>
        px:tokenize
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-add-entry
        px:fileset-add-entries
        px:fileset-filter
        px:fileset-copy
        px:fileset-join
        px:fileset-store
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
      <p:documentation>
        px:dtbook-load
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
      <p:documentation>
        px:css-to-fileset
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
      <p:documentation>
        px:epub3-store
      </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
      <p:documentation>
        pf:normalize-uri
      </p:documentation>
    </cx:import>

    <p:sink/>
    <px:fileset-add-entries media-type="application/x-dtbook+xml" name="dtbook">
      <p:input port="entries">
        <p:pipe step="main" port="source"/>
      </p:input>
    </px:fileset-add-entries>
    <px:dtbook-load name="load" px:progress=".1" px:message="Loading DTBook">
      <p:input port="source.in-memory">
        <p:pipe step="dtbook" port="result.in-memory"/>
      </p:input>
      <p:with-option name="validation" select="not($validation='off')"/>
      <p:with-option name="nimas" select="$nimas='true'"/>
      <!-- assume MathML 3.0 -->
    </px:dtbook-load>

    <p:identity>
      <p:input port="source">
        <p:pipe step="load" port="validation-status"/>
      </p:input>
    </p:identity>
    <p:choose>
      <p:when test="/d:validation-status[@result='error']">
        <p:choose>
          <p:when test="$validation='abort'">
            <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
                        px:message-severity="ERROR"/>
          </p:when>
          <p:otherwise>
            <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
                        px:message-severity="WARN"/>
          </p:otherwise>
        </p:choose>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
    <p:choose px:progress=".9">
      <p:when test="/d:validation-status[@result='error'] and $validation='abort'">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <p:variable name="dtbook-is-valid" cx:as="xs:boolean"
                    select="not($validation='off') and exists(/d:validation-status[@result='ok'])"/>
        <p:sink/>
        <!-- fileset of CSS with resources -->
        <p:group name="css">
          <p:output port="result"/>
          <px:tokenize regex="\s+">
            <p:with-option name="string" select="normalize-space($attach-stylesheet)"/>
          </px:tokenize>
          <p:for-each>
            <p:variable name="href" select="string(.)"/>
            <px:fileset-create>
              <p:with-option name="base" select="resolve-uri('./',$href)"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="text/css">
              <p:with-option name="href" select="$href"/>
              <p:with-param port="file-attributes" name="role" select="'stylesheet'"/>
            </px:fileset-add-entry>
            <px:css-to-fileset>
                <!-- warns about missing (including remote) resources -->
            </px:css-to-fileset>
            <!-- move all style sheets to a common place -->
            <!-- this will raise an error if some referenced resources fall outside
                 the directory that contains the style sheet -->
            <px:fileset-copy>
              <p:with-option name="target" select="$temp-dir"/>
            </px:fileset-copy>
          </p:for-each>
          <px:fileset-join/>
        </p:group>
        <p:sink/>
        <p:identity>
          <p:input port="source">
            <p:pipe step="main" port="source"/>
          </p:input>
        </p:identity>
        <p:group px:progress="1">
          <!-- get the eBraille file or directory name from the DTBook file name -->
          <p:variable name="dtbook-uri" select="base-uri(/)"/>
          <p:variable name="output-name" select="replace(replace($dtbook-uri,'^.*/([^/]+)$','$1'),'\.[^\.]*$','')"/>
          <p:variable name="output-dir-uri" select="pf:normalize-uri(concat($result,'/'))"/>
          <p:sink/>

          <px:dtbook-to-ebraille name="convert" px:progress="8/9">
            <p:input port="source.fileset">
              <p:pipe step="load" port="result.fileset"/>
            </p:input>
            <p:input port="source.in-memory">
              <p:pipe step="load" port="result.in-memory"/>
            </p:input>
            <p:input port="css.fileset">
              <p:pipe step="css" port="result"/>
            </p:input>
            <p:input port="css.in-memory">
              <p:empty/>
            </p:input>
            <p:with-option name="braille-translator" select="$braille-code"/>
            <p:with-option name="braille-translator-stylesheet"
                           select="string-join(
                                     for $s in tokenize($braille-translator-stylesheet,'\s+')[not(.='')] return
                                       resolve-uri($s,$dtbook-uri),
                                     ' ')"/>
            <p:with-option name="braille-translator-stylesheet-parameters"
                           select="$braille-translator-stylesheet-parameters"/>
            <p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
            <p:with-option name="nimas" select="$nimas='true'"/>
            <p:with-option name="include-original-text" select="$include-original-text"/>
            <p:with-option name="output-dir" select="if ($epub-package)
                                                     then concat($temp-dir,'ebraille-unzipped/')
                                                     else concat($output-dir-uri,'/')"/>
            <p:with-option name="temp-dir" select="if ($epub-package)
                                                   then concat($temp-dir,'temp/')
                                                   else $temp-dir"/>
          </px:dtbook-to-ebraille>

          <p:choose name="store" px:progress="1/9">
            <p:when test="$epub-package" px:message="Storing in EPUB 3 package">
              <px:epub3-store px:progress="1">
                <p:input port="in-memory.in">
                  <p:pipe step="convert" port="result.in-memory"/>
                </p:input>
                <p:with-option name="href" select="concat($output-dir-uri,$output-name,'.ebrl')"/>
              </px:epub3-store>
            </p:when>
            <p:otherwise px:message="Storing">
              <p:documentation>
                mimetype and META-INF/container.xml may be omitted when the eBraille
                publication is not packaged in an EPUB container.
              </p:documentation>
              <px:fileset-filter name="filter-mimetype" href="mimetype">
                <!-- assumes $output-dir-uri is the fileset base -->
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

          <p:identity cx:depends-on="store">
            <p:input port="source">
              <p:pipe step="convert" port="status"/>
            </p:input>
          </p:identity>
        </p:group>
      </p:otherwise>
    </p:choose>

</p:declare-step>
