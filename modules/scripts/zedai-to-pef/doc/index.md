<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/zedai-to-pef/"/>
<link rev="dp2:doc" href="../src/main/resources/xml/zedai-to-pef.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# ZedAI to PEF

The "ZedAI to PEF" script will convert a ZedAI document (NISO Z39.98-2012 - Authoring and Interchange Framework for Adaptive XML Publishing) into a PEF (Portable Embosser Format) file. PEF is a format for representing paged braille documents in an unambiguous way. It builds on XML, Unicode and Dublin Core. Tools are available for embossing PEF files or converting them into other braille formats. See also [the PEF Format website](http://pef-format.org/).

## Table of contents

{{>toc}}

## Synopsis

{{>synopsis}}

## Example running from command line

    $ ./dp2 zedai-to-pef \
    	--i-source samples/zedai/alice.xml \
    	--x-include-preview true \
    	--x-include-brf true \
    	--x-output ~/Desktop/out

output:

    $ ls ~/Desktop/out
    alice.pef
    alice.pef.html
    alice.brf

More advanded example:

    $ ./dp2 zedai-to-pef \
            --i-source samples/zedai/alice.xml \
            --x-include-preview true \
            --x-default-stylesheet bana.css \
            --x-transform "(formatter:dotify)" \
            --x-output ~/Desktop/out

## Getting a preview

To emboss a PEF file or display it on screen you need special software (such as the [Easy Embosser Utility](http://code.google.com/p/e2u/) developed at TPB). Opening a PEF with an XML editor is not very useful because you only get to see Unicode braille (dot patterns). If you don't have access to a special PEF tool, or you just want to quickly review the document first, you should set the `include-preview` option to `true`. In addition to the actual PEF file, an HTML page will then be generated that you can open in your browser. You will be able to quickly navigate pages, and switch between a 'dot patterns' view and a more readable ASCII view. To properly render this HTML page, you need the [odt2braille font](http://sourceforge.net/projects/odt2braille/files/ttf/odt2braille-6dot-v1.1.ttf).

## Advanced Configuration

### Translation

By default, text is translated to braille with a simple generic [liblouis](http://liblouis.org) based translator. All the translation tables distributed by liblouis (about 80 languages) are included in the DAISY Pipeline. The translator will automatically look for the table that matches the `xml:lang` attribute best. The `transform` option gives you more control over translator selection. For example:

    --x-transform "(locale:fr)"

will force the document to be translated to French braille. Or, you might want to select a specific liblouis table, for example US English Grade 1:

    --x-transform "(liblouis-table:'en-us-g1.ctb')"

If you are running DAISY Pipeline 2 in local mode, and you have your own liblouis tables installed somewhere, you can use them by specifying the full path:

    --x-transform "(liblouis-table:'file:/home/me/liblouis-tables/my_en-us.ctb')"

Liblouis is the main translation engine used in DAISY Pipeline 2, but other libraries are available. The following translator is backed by [Dotify](https://code.google.com/p/dotify/), the braille translation library developed at MTM:

    --x-transform "(translator:dotify)"

More examples are provided below. If your use case requires non-trivial features and the supplied generic translators won't do, you can write your own translator, which might be a combination of existing and new components. If you have idea's about features that should be supported by default, please share them with us.

### Hyphenation

The default transformer automatically hyphenates text with CSS property “hyphens:auto” (see [Styling with Braille CSS](#styling-with-braille-css) below). A hyphenator will be selected based on the `xml:lang` attribute. If liblouis isn't able to hyphenate the text itself, then [Hyphen](http://hunspell.sourceforge.net/), the hyphenation engine from [OpenOffice](http://www.openoffice.org/) and [LibreOffice](http://www.libreoffice.org/), is used. To force the use of Hyphen, specify:

    --x-transform "(hyphenator:hyphen)"

and to select a specific Hyphen table, specify:

    --x-transform "(libhyphen-table:'hyph_en_US.dic')"

### Text Emphasis

In case the input document has styling attached for media 'print', the default translator uses this information to mark emphasised text (font-style, font-weight, etc.) with braille indicators.

### Math

Translation of [MathML](http://www.w3.org/Math/) is supported by default. The supported braille math codes are _Nemeth_ (used in the US), _Marburg_ (Germany), _Woluwe_ (Belgium) and _UKMaths_ (Great Britain). The right translator is selected based on the `xml:lang` attribute.

### Styling with Braille CSS

The formatting of documents into a paged braille layout is specified with CSS (Cascading Style Sheets). We use a flavor of CSS specially designed for this use case. For people that already know CSS, it will feel very natural. In fact, it is almost a subset of CSS3, but with certain restrictions and some braille specific features. The most obvious difference is that margins are expressed in units of cell width and line height instead of pixels, ems and percentages. Extensive documentation of braille CSS can be found in the [Braille CSS specification draft](http://braillespecs.github.io/braille-css).

In addition to braille CSS, which is normative, certain parts of 'print' CSS can be taken into account as well. An example is text emphasis. (See also [Braille CSS - Media Types](http://braillespecs.github.io/braille-css#media-types).)

The actual rendering of documents based on CSS is done with Dotify. There is a also second implementation based on [liblouisutdml](http://liblouis.org).

Attaching style information to a document can be done either by linking to an external CSS stylesheet, by including an internal stylesheet, by inserting inline styles, or a hibrid of these three.

1. External stylesheet:

   ~~~xml
   <document>
       <head>
           <link rel="stylesheet" media="embossed" type="text/css" href="external_stylesheet.css" />
           ...
   ~~~

2. Internal stylesheet:

   ~~~xml
   <document>
       <head>
           <style media="embossed" type="text/css">
               head {
                   display: none;
               }
               h {
                   display: block; text-align: center;
               }
               ...
           </style>
           ...
   ~~~

3. Inline styles:

   ~~~xml
   <document>
       <head style="display:none">
           ...
       </head>
       <body>
           <frontmatter>
               <section>
                   <h style="display:block;text-align:center">...</h>
                   ...
   ~~~

Keep in mind that a simple default stylesheet (`default.css`) is always applied first. If you don't want this default CSS, include `reset.css` in your stylesheet:

~~~css
@import url('http://www.daisy.org/pipeline/modules/braille/zedai-to-pef/css/reset.css');
~~~

To change which default CSS is applied, use the `default-stylesheet` option as in the [advanced example](#example-running-from-command-line) above. The idea is to eventually have stylesheets for every braille formatting standard. We are hoping for contributions from people who are more familiar with the standards.

#### Example 1: Sections and headings

~~~css
section {
    display: block;
    page-break-before: always;
}
~~~

By putting this code in your stylesheet, each section will be guaranteed to start on a new braille page.

~~~css
h {
    display:block;
    text-align:center;
    margin-top: 1;
    margin-bottom: 1;
    margin-left: 4;
    margin-right: 4;
    page-break-after: avoid;
}
h + * {
    orphans: 2;
}
~~~

Headings are centered on the page, with at least one line left blank both above and below. The left and right margins are increased enough so that long headings can not be confused with normal paragraphs. To make sure headings are not rendered at the bottom of a page, no page breaks are allowed right after a heading, and any element immediately following a heading must have at least two lines (orphans) on the current page.

#### Example 2: Ordered and unordered lists

~~~css
list {
    display: block;
}
list > item {
    display: list-item;
}
list[type="unordered"] {
    list-style-type: ⠶;
}
list[type="ordered"].decimal {
    list-style-type: decimal;
}
list[type="ordered"].alpha {
    list-style-type: lower-alpha;
}
list[type="ordered"].roman {
    list-style-type: lower-roman;
}
~~~

This will format lists of type "unordered" as bulleted lists, with each item preceded by the braille character (bullet) '⠶'. Lists of type "ordered" will be numbered: "1. 2. 3. ..." for class "decimal", "a. b. c. ..." for class "alpha" and "i. ii. iii. ..." for class "roman".

#### Example 3: Tables in linear versus stairstep format

~~~css
table {
    display: block;
    border-top: ⠶;
    border-bottom: ⠛;
}
thead {
    display: block;
    border-bottom: ⠒;
}
tr {
    display: block;
    page-break-inside: avoid;
}
~~~

This code will give tables an upper and lower border (lines of characters '⠶' and '⠛' respectively). If there is a table header, a '⠒' border is printed between the header and the body. Table rows are protected from being split across braille pages.

~~~css
.linear th::after,
.linear td::after {
    content: "⠰ "
}
~~~

Tables of class "linear" (`<table class="linear">`) will have table cells separated by the character '⠰' and a space.

~~~css
table.stairstep td,
table.stairstep th {
    display: block;
}
table.stairstep th:first-child,
table.stairstep td:first-child {
    margin-left: 0;
}
table.stairstep th:nth-child(2),
table.stairstep td:nth-child(2) {
    margin-left: 3;
}
table.stairstep th:nth-child(3),
table.stairstep td:nth-child(3) {
    margin-left: 6;
}
...
~~~

In tables of class "stairstep" (`<table class="stairstep">`), each table cell will start on a new line, with a left margin that is the same for cells in the same column.

#### Example 4: Print page numbers

~~~css
pagebreak {
    display: none;
    string-set: print-page attr(value);
}
~~~

This snippet will update the print page number whenever a `<pagebreak value="..."/>` is encountered. In order to render print page numbers, the `@page` rule must be used (see [example 6](#example-6-page-layout) below).

When `display:none` is changed to `display:page-break`, a _page break indicator_ (a line of braille dots '36') is generated wherever a print page break occurs:

~~~css
pagebreak {
    display: page-break;
    string-set: print-page attr(value);
}
~~~

Note that in the current implementation, `display:page-break` only works in combination with `string-set:print-page`, and won't have any effect unless it is used for *all* print pages.

#### Example 5: Table of contents

~~~xml
<toc>
    <h>Contents</h>
    <entry><ref ref="chapter_1">I. Down the Rabbit-Hole</ref></entry>
    <entry><ref ref="chapter_2">II. The Pool of Tears</ref></entry>
    <entry><ref ref="chapter_3">III. A Caucus-Race and a Long Tale</ref></entry>
</toc>
...
    <h xml:id="chapter_1">Chapter I. Down the Rabbit-Hole</h>
...
~~~

A table of contents can be rendered in a typical layout with page numbers on the right and leaders between entries and page numbers. A precondition is that headings in the text are referenced within the table of contents (e.g. with an attribute). If print page numbers and/or braille page numbers are present in the braille output, they can be included in the table of contents too. The pattern of leaders can be configured. Let's look at the CSS:

~~~css
toc > h {
    display: block;
    text-align: center;
    margin-top: 1;
    margin-bottom: 1;
}
toc > entry > ref {
    display: block;
    margin-left: 2;
    text-indent: -2;
}
toc > entry > ref::after {
    content: ' ' leader('⠤')
             ' ' target-string(attr(ref), print-page)
             ' ' target-counter(attr(ref), page);
}
~~~

With the content property we are generating content and appending it to the `ref` element.

#### Example 6: Page layout

The dimensions of the page, the position of page numbers, and the running header and/or footer (see [example 7](#example-7-running-headerfooter)), can be configured with the `@page` rule:

~~~css
@page {
    size: 40 25;
    @top-right {
        content: string(print-page);
    }
    @bottom-right {
        content: counter(page);
    }
    @bottom-center {
        content: string(running-footer);
    }
}
~~~

The only valid positions for page numbers are currently `@top-right` and `@bottom-right`. It is also possible to put both page numbers at the top or at the bottom, like this: `@top-right{content:string(print-page) counter(page);}`

As showed in the next example, pages can be named. This can be useful to switch page layout in the middle of a document. The following example will start a new page with double width for each wide table, so that it can be printed across facing pages.

~~~css
@page double {
    size: 80 25;
}

table.wide {
    page: double;
}
~~~

#### Example 7: Running header/footer

As seen in [example 6](#example-6-page-layout) above, pages can have running headers and/or footers. To update the value of the footer whenever a new heading appears, include the following:

~~~css
h {
    string-set: running-footer content();
}
~~~

#### Example 8: Braille page numbers

CSS also has some tools to customized the braille page numbering. For example, to distinguished the frontmatter from the bodymatter, they often have different page numbering formats. This snippet gives the frontmatter lowercase Roman page numbers, and restarts the numbering after the frontmatter:

~~~css
@page front {
    @bottom-right {
        content: counter(page, lower-roman);
    }
}

frontmatter {
    page: front;
}

bodymatter {
    counter-reset: page 1;
}
~~~
