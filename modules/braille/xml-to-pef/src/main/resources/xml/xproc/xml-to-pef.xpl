<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

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
    <p:option name="transform" required="false" px:type="transform-query" select="'(translator:liblouis)(formatter:dotify)'">
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
    <p:option name="ascii-file-format" required="false" px:type="transform-query" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Plain text (BRF) file format</h2>
            <p px:role="desc" xml:space="preserve">The file format to store the plain text version.

If left blank, the locale information in the input document will be used to select a suitable file format.</p>
        </p:documentation>
    </p:option>
    <p:option name="ascii-table" required="false" px:type="transform-query" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ASCII braille table</h2>
            <p px:role="desc" xml:space="preserve">The ASCII braille table, used to render the PEF preview and, if no plain text file format was specified, the plain text version.

If left blank, the locale information in the input document will be used to select a suitable table.</p>
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
    <p:option name="obfl-output-dir" required="false" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">OBFL</h2>
            <p px:role="desc">The intermediary OBFL-file (for debugging).</p>
        </p:documentation>
    </p:option>
    <p:option name="temp-dir" required="false" px:output="temp" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Directory for storing temporary files during conversion.</p>
        </p:documentation>
    </p:option>
    
</p:declare-step>
