<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xml-to-pef" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                exclude-inline-prefixes="#all"
                name="main">

    <!-- ============ -->
    <!-- Main options -->
    <!-- ============ -->
    <p:option name="stylesheet" required="false" px:type="string" select="''" px:sequence="true" px:media-type="text/css application/xslt+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Style sheets</h2>
            <p px:role="desc" xml:space="preserve">A list of XSLT or CSS/SASS style sheets to apply.

Must be a space separated list of URIs, absolute or relative to the input.

Style sheets can also be associated with the source in other ways: linked (using an
['xml-stylesheet' processing instruction](https://www.w3.org/TR/xml-stylesheet) or a ['link'
element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using '[style'
attributes](https://www.w3.org/TR/css-style-attr/)).

Style sheets are applied to the document in the following way: XSLT style sheets are applied before
CSS/SASS style sheets. XSLT style sheets are applied one by one, first the ones specified through
this option, then the ones associated with the source document, in the order in which they are
specified.

All CSS/SASS style sheets are applied at once, but the order in which they are specified (first the
ones specified through this option, then the ones associated with the source document) has an
influence on the [cascading order](https://www.w3.org/TR/CSS2/cascade.html#cascading-order).

CSS/SASS style sheets are interpreted according to [braille
CSS](http://braillespecs.github.io/braille-css) rules.

For info on how to use SASS (Syntactically Awesome StyleSheets) see the [SASS
manual](http://sass-lang.com/documentation/file.SASS_REFERENCE.html).</p>
        </p:documentation>
    </p:option>
    <p:option name="transform" required="false" px:data-type="transform-query" select="'(translator:liblouis)(formatter:dotify)'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Transformer query</h2>
            <p px:role="desc">The transformer query.</p>
        </p:documentation>
    </p:option>
    <p:option name="include-preview" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include preview</h2>
            <p px:role="desc" xml:space="preserve">Whether or not to include a preview of the PEF in HTML.</p>
        </p:documentation>
    </p:option>
    <p:option name="include-brf" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include plain text file (BRF)</h2>
            <p px:role="desc" xml:space="preserve">Whether or not to include a plain text ASCII version of the PEF.</p>
        </p:documentation>
    </p:option>
    <p:option name="include-obfl" required="false" px:type="boolean" select="'false'">
      <p:documentation>
        <h2 px:role="name">Include OBFL</h2>
        <p px:role="desc" xml:space="preserve">Keeps the intermediary OBFL-file for debugging.</p>
      </p:documentation>
    </p:option>
    <p:option name="ascii-file-format" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Plain text (BRF) file format</h2>
            <p px:role="desc" xml:space="preserve">The file format to store the plain text version.</p>
        </p:documentation>
    </p:option>
    <p:option name="ascii-table" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ASCII braille table</h2>
            <p px:role="desc" xml:space="preserve">The ASCII braille table, used to render the PEF preview and, if no plain text file format was specified, the plain text version.</p>
        </p:documentation>
    </p:option>
    
    <!-- =========== -->
    <!-- Page layout -->
    <!-- =========== -->
    <p:option name="page-width" required="false" px:type="integer" select="'40'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page layout: Page width</h2>
            <p px:role="desc" xml:space="preserve">The number of columns available for printing.

Makes the variable `$page-width` available in style sheets and includes the following rule by default:

~~~css
@page {
  size: $page-width $page-height;
}
~~~

See the CSS specification for more info:

- the [`@page`](http://braillespecs.github.io/braille-css/#h4_the-page-rule) rule
- the [`size`](http://braillespecs.github.io/braille-css/#the-size-property) property
</p>
        </p:documentation>
    </p:option>
    <p:option name="page-height" required="false" px:type="integer" select="'25'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page layout: Page height</h2>
            <p px:role="desc" xml:space="preserve">The number of rows available for printing.

Makes the variable `$page-height` available in style sheets and includes the following rule by default:

~~~css
@page {
  size: $page-width $page-height;
}
~~~

See the CSS specification for more info:

- the [`@page`](http://braillespecs.github.io/braille-css/#h4_the-page-rule) rule
- the [`size`](http://braillespecs.github.io/braille-css/#the-size-property) property
</p>
        </p:documentation>
    </p:option>
    <p:option name="left-margin" required="false" px:type="integer" select="'0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page layout: Left margin</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="duplex" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page layout: Duplex</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will print on both sides of the paper.</p>
        </p:documentation>
    </p:option>
    
    <!-- =============== -->
    <!-- Headers/footers -->
    <!-- =============== -->
    <p:option name="levels-in-footer" required="false" px:type="integer" select="'6'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Headers/footers: Levels in footer</h2>
            <p px:role="desc" xml:space="preserve">Specify which headings are rendered in the footer.
              
Makes the variable `$levels-in-footer` available in style sheets and includes the following rule by
default:

~~~css
@for $level from 1 through 6 {
  @if $levels-in-footer >= $level {
    h#{$level} {
      string-set: footer content();
    }
  }
}
~~~

In other words, the `footer` string is updated each time a heading with a level smaller than or
equal to `levels-in-footer` is encountered. In order to use the `footer` string include a rule like
the following in your custom style sheet:

~~~css
@page {
  @bottom-center {
    content: string(footer);
  }
}
~~~

See the CSS specification for more info:

- the [`string-set`](http://braillespecs.github.io/braille-css/#h4_the-string-set-property) property
- the [`@page`](http://braillespecs.github.io/braille-css/#h4_the-page-rule) rule
- the [`string()`](http://braillespecs.github.io/braille-css/#h4_the-string-function) function
</p>
        </p:documentation>
    </p:option>
    
    <!-- ============================== -->
    <!-- Translation/formatting of text -->
    <!-- ============================== -->
    <p:option name="main-document-language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Main document language</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="hyphenation" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Hyphenation</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will automatically hyphenate text.

Makes the variable `$hyphenation` available in style sheets and includes the following rule by
default:

~~~css
@if $hyphenation {
  :root {
    hyphens: auto;
  }
}
~~~

See the CSS specification for more info:

- the [`hyphens`](http://braillespecs.github.io/braille-css/#the-hyphens-property) property
</p>
        </p:documentation>
    </p:option>
    <p:option name="line-spacing" required="false" select="'single'">
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <value>single</value>
                    <documentation xml:lang="en">Single</documentation>
                    <value>double</value>
                    <documentation xml:lang="en">Double</documentation>
                </choice>
            </px:data-type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Line spacing</h2>
            <p px:role="desc" xml:space="preserve">Single or double line spacing.

Makes the variable `$line-spacing` available in style sheets and includes the following rule by
default:

~~~css
@if $line-spacing == double {
  :root {
    line-height: 2;
  }
}
~~~

See the CSS specification for more info:

- the [`line-height`](http://braillespecs.github.io/braille-css/#h3_the-line-height-property)
  property
</p>
        </p:documentation>
    </p:option>
    <p:option name="tab-width" required="false" px:type="integer" select="'4'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Tab width</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="capital-letters" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Capital letters</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will indicate capital letters.

Makes the variable `$capital-letters` available in style sheets and includes the following rule by
default:

~~~css
@if $capital-letters != true {
  :root {
    text-transform: lowercase;
  }
}
~~~</p>
        </p:documentation>
    </p:option>
    <p:option name="accented-letters" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Accented letters</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="polite-forms" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Polite forms</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="downshift-ordinal-numbers" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Translation/formatting of text: Downshift ordinal numbers</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    
    <!-- ============== -->
    <!-- Block elements -->
    <!-- ============== -->
    <p:option name="include-captions" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Block elements: Include captions</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will include captions for images, tables, and so on.

Makes the variable `$include-captions` available in style sheets and includes the following rule by
default:

~~~css
caption {
  display: if($include-captions, block, none);
}
~~~</p>
        </p:documentation>
    </p:option>
    <p:option name="include-images" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Block elements: Include images</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will include the alt text of the images. When disabled, the images will be completely removed.

Makes the variable `$include-images` available in style sheets and includes the following rule by
default:

~~~css
@if $include-images {
  img::after {
    content: attr(alt);
  }
}
~~~</p>
        </p:documentation>
    </p:option>
    <p:option name="include-image-groups" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Block elements: Include image groups</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="include-line-groups" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Block elements: Include line groups</h2>
            <p px:role="desc" xml:space="preserve">When disabled, lines in a linegroup are joined together to form one block.

Makes the variable `$include-line-groups` available in style sheets and includes the following rule
by default:

~~~css
linegroup line {
  display: if($include-line-groups, block, inline);
}
~~~</p>
        </p:documentation>
    </p:option>
    
    <!-- =============== -->
    <!-- Inline elements -->
    <!-- =============== -->
    <p:option name="text-level-formatting" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Inline elements: Text-level formatting (emphasis, strong)</h2>
            <p px:role="desc" xml:space="preserve">When enabled, text that is in bold or italics in the print version will be rendered in bold or italics in the braille version as well. **Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="include-note-references" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Inline elements: Include note references</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="include-production-notes" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Inline elements: Include production notes</h2>
            <p px:role="desc" xml:space="preserve">When enabled, production notes are included in the content.

Makes the variable `$include-production-notes` available in style sheets and includes the following
rule by default:

~~~css
prodnote {
  display: if($include-production-notes, block, none);
}
~~~</p>
        </p:documentation>
    </p:option>
    
    <!-- ============ -->
    <!-- Page numbers -->
    <!-- ============ -->
    <p:option name="show-braille-page-numbers" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page numbers: Show braille page numbers</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will number braille pages.

Makes the variable `$show-braille-page-numbers` available in style sheets. In order to use the
variable include a rule like the following in your custom style sheet:

~~~css
@if $show-braille-page-numbers {
  @page {
    @top-right {
      content: counter(page);
    }
  }
}
~~~

This will create a page number in the top right corner of every braille page.

See the CSS specification for more info:

- the [`@page`](http://braillespecs.github.io/braille-css/#h4_the-page-rule) rule
- the
  [`counter()`](http://braillespecs.github.io/braille-css/#printing-counters-the-counter-function)
  function
</p>
        </p:documentation>
    </p:option>
    <p:option name="show-print-page-numbers" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page numbers: Show print page numbers</h2>
            <p px:role="desc" xml:space="preserve">When enabled, will indicate original page numbers.

Makes the variable `$show-print-page-numbers` available in style sheets and includes the following
rule by default:

~~~css
@if $show-print-page-numbers {
  pagenum {
    string-set: print-page content();
  }
}
~~~

In order to use the `print-page` string include a rule like the following in your custom style
sheet:

~~~css
@page {
  @bottom-right {
    content: string(print-page);
  }
}
~~~

See the CSS specification for more info:

- the [`string-set`](http://braillespecs.github.io/braille-css/#h4_the-string-set-property) property
- the [`@page`](http://braillespecs.github.io/braille-css/#h4_the-page-rule) rule
- the [`string()`](http://braillespecs.github.io/braille-css/#h4_the-string-function) function
</p>
        </p:documentation>
    </p:option>
    <p:option name="force-braille-page-break" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Page numbers: Force braille page break</h2>
            <p px:role="desc" xml:space="preserve">Force braille page breaks at print page breaks.

Makes the variable `$force-braille-page-break` available in style sheets and includes the following
rule by default:

~~~css
@if $force-braille-page-break {
  pagenum {
    page-break-before: always;
  }
}
~~~

See the CSS specification for more info:

- the [`page-break-before`](http://braillespecs.github.io/braille-css/#h4_controlling-page-breaks)
  property
</p>
        </p:documentation>
    </p:option>
    
    <!-- ================= -->
    <!-- Table of contents -->
    <!-- ================= -->
    <p:option name="toc-depth" required="false" px:type="integer" select="'0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Table of contents: Table of contents depth</h2>
            <p px:role="desc" xml:space="preserve">The depth of the table of contents hierarchy to include. '0' means no table of contents.

A table of contents will be generated from the heading elements present in the document: from `h1`
elements if the specified value for "depth" is 1, from `h1` and `h2` elements if the specified value
is 2, etc. The resulting table of contents has the following nested structure:

~~~xml
&lt;list id="generated-document-toc"&gt;
  &lt;li&gt;
      &lt;a href="#ch_1"&gt;Chapter 1&lt;/a&gt;
      &lt;list&gt;
          &lt;li&gt;
              &lt;a href="#ch_1_1"&gt;1.1&lt;/a&gt;
              ...
          &lt;/li&gt;
          &lt;li&gt;
              &lt;a href="#ch_1_2"&gt;1.2&lt;/a&gt;
              ...
          &lt;/li&gt;
          ...
      &lt;/list&gt;
  &lt;/li&gt;
  ...
&lt;/list&gt;
~~~

Another one of these is generated but with ID `generated-volume-toc`. `ch_1`, `ch_1_2` etc. are the
IDs of the heading elements from which the list was constructed, and the content of the links are
exact copies of the content of the heading elements. By default the list is not rendered. The list
should be styled and positioned with CSS. The following rules are included by default:

~~~css
#generated-document-toc {
  flow: document-toc;
  display: -obfl-toc;
  -obfl-toc-range: document;
}

#generated-volume-toc {
  flow: volume-toc;
  display: -obfl-toc;
  -obfl-toc-range: volume;
}
~~~

This means that a document range table of contents is added to the named flow called "document-toc",
and a volume range table of contents is added to the named flow called "volume-toc". In order to
consume these named flows use the function `flow()`. For example, to position the document range
table of contents at the beginning of the first volume, and to repeat the volume range table of
content at the beginning of every other volume, include the following additional rules:

~~~css
@volume {
  @begin {
    content: flow(volume-toc);
  }
}

@volume:first {
  @begin {
    content: flow(document-toc);
  }
}
~~~

See the CSS specification for more info:

- the [`display:
  obfl-toc`](http://braillespecs.github.io/braille-css/obfl#extending-the-display-property-with--obfl-toc)
  value
- the [`flow`](http://braillespecs.github.io/braille-css/#the-flow-property) property
- the [`flow()`](http://braillespecs.github.io/braille-css/#h4_the-flow-function) function
- the [`@volume`](http://braillespecs.github.io/braille-css/#h3_the-volume-rule) rule
- the [`@begin`](http://braillespecs.github.io/braille-css/#h3_the-begin-and-end-rules) rule
</p>
        </p:documentation>
    </p:option>
    <!-- ==================== -->
    <!-- Placement of content -->
    <!-- ==================== -->
    <p:option name="footnotes-placement" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Placement of content: Footnotes placement</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="colophon-metadata-placement" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Placement of content: Colophon/metadata placement</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="rear-cover-placement" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Placement of content: Rear cover placement</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    
    <!-- ======= -->
    <!-- Volumes -->
    <!-- ======= -->
    <p:option name="number-of-sheets" required="false" px:type="integer" select="'50'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Volumes: Number of sheets</h2>
            <p px:role="desc" xml:space="preserve">**Not implemented**</p>
        </p:documentation>
    </p:option>
    <p:option name="maximum-number-of-sheets" required="false" px:type="integer" select="'70'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Volumes: Maximum number of sheets</h2>
            <p px:role="desc" xml:space="preserve">The maximum number of sheets in a volume.

Makes the variable `$maximum-number-of-sheets` available in style sheets and includes the following
rule by default:

~~~css
@volume {
  max-length: $maximum-number-of-sheets;
}
~~~

See the CSS specification for more info:

- the [`@volume`](http://braillespecs.github.io/braille-css/#h3_the-volume-rule) rule
- the [`max-length`](http://braillespecs.github.io/braille-css/#h3_the-length-properties) property
</p>
        </p:documentation>
    </p:option>
    <p:option name="minimum-number-of-sheets" required="false" px:type="integer" select="'30'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Volumes: Minimum number of sheets</h2>
            <p px:role="desc" xml:space="preserve">The minimum number of sheets in a volume. **Not implemented**</p>
        </p:documentation>
    </p:option>
    
    <!-- ======= -->
    <!-- Outputs -->
    <!-- ======= -->
    <p:option name="pef-output-dir" required="true" px:output="result" px:type="anyDirURI" px:media-type="application/x-pef+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">PEF</h2>
            <p px:role="desc">The PEF.</p>
        </p:documentation>
    </p:option>
    <p:option name="brf-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="text" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">BRF</h2>
            <p px:role="desc">A plain text ASCII version of the PEF.</p>
        </p:documentation>
    </p:option>
    <p:option name="preview-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Preview</h2>
            <p px:role="desc">An HTML preview of the PEF.</p>
        </p:documentation>
    </p:option>
    <p:option name="temp-dir" required="false" px:output="temp" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Directory for storing temporary files during conversion.</p>
        </p:documentation>
    </p:option>
    
    <!-- ======================================================================== -->
    <!-- Do nothing; this script is only intended to be extended by other scripts -->
    <!-- ======================================================================== -->
    <p:sink>
        <p:input port="source">
            <p:empty/>
        </p:input>
    </p:sink>
    
</p:declare-step>
