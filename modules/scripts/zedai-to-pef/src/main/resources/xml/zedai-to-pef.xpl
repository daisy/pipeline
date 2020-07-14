<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:zedai-to-pef.script" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">ZedAI to PEF</h1>
        <p px:role="desc">Transforms a ZedAI (DAISY 4 XML) document into a PEF.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/braille/zedai-to-pef">
            Online documentation
        </a>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Bert Frees</dd>
            <dt>Organization:</dt>
            <dd px:role="organization" href="http://www.sbs-online.ch/">SBS</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
        </dl>
    </p:documentation>
    
    <p:input port="source" primary="true" px:name="source" px:media-type="application/z3998-auth+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input ZedAI</h2>
            <p px:role="desc">The ZedAI you want to convert to braille.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="stylesheet" required="false" px:sequence="true" select="''" px:media-type="text/css application/xslt+xml">
        <p:pipeinfo>
            <px:type>
                <choice>
                    <data type="anyFileURI" datatypeLibrary="http://www.daisy.org/ns/pipeline/xproc">
                        <documentation xml:lang="en">File path relative to input ZedAI.</documentation>
                    </data>
                    <data type="anyURI">
                        <documentation xml:lang="en">Any other absolute URI</documentation>
                    </data>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation>
            <h2 px:role="name">Style sheets</h2>
            <p px:role="desc" xml:space="preserve">A list of CSS/SASS style sheets to apply.

Must be a space separated list of URIs, absolute or relative to source.

Style sheets can also be associated with the source in other ways: linked (using a
['link' element](https://www.w3.org/Style/styling-XML#External)), embedded (using a ['style'
element](https://www.w3.org/Style/styling-XML#Embedded)) and/or inlined (using
'[style' attributes](https://www.w3.org/TR/css-style-attr/)).

All style sheets are applied at once, but the order in which they are specified (first the ones
specified through this option, then the ones associated with the source document) has an influence
on the cascading order.

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
    
    <p:option name="ascii-table" required="false" px:type="transform-query" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ASCII braille table</h2>
            <p px:role="desc">The ASCII braille table, used to render the PEF preview and the plain text version.

If left blank, the locale information in the input document will be used to select a suitable table.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="include-preview" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include preview</h2>
            <p px:role="desc">Whether or not to include a preview of the PEF in HTML.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="include-brf" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include plain text file (BRF)</h2>
            <p px:role="desc">Whether or not to include a plain text ASCII version of the PEF.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="pef-output-dir" required="true" px:output="result" px:type="anyDirURI" px:media-type="application/x-pef+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">PEF</h2>
            <p px:role="desc">The PEF.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="brf-output-dir" px:output="result" px:type="anyDirURI" px:media-type="text" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">BRF</h2>
            <p px:role="desc">A plain text ASCII version of the PEF.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="preview-output-dir" px:output="result" px:type="anyDirURI" px:media-type="text/html" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Preview</h2>
            <p px:role="desc">An HTML preview of the PEF.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="false" px:output="temp" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Directory for storing temporary files.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="zedai-to-pef.convert.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl">
        <p:documentation>
            px:xml-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:tempdir
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl">
        <p:documentation>
            px:zedai-load
        </p:documentation>
    </p:import>
    
    <!-- =============== -->
    <!-- CREATE TEMP DIR -->
    <!-- =============== -->
    
    <px:tempdir name="temp-dir">
        <p:with-option name="href" select="if ($temp-dir!='') then $temp-dir else $pef-output-dir"/>
    </px:tempdir>
    <p:sink/>
    
    <!-- ========== -->
    <!-- LOAD ZEDAI -->
    <!-- ========== -->
    
    <px:zedai-load name="load">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:zedai-load>
    
    <!-- ============ -->
    <!-- ZEDAI TO PEF -->
    <!-- ============ -->
    
    <px:zedai-to-pef>
        <p:input port="source.in-memory">
            <p:pipe step="load" port="in-memory.out"/>
        </p:input>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="transform" select="$transform"/>
        <p:with-option name="temp-dir" select="string(/c:result)">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
    </px:zedai-to-pef>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <px:xml-to-pef.store>
        <p:input port="obfl">
            <p:empty/>
        </p:input>
        <p:with-option name="name" select="replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1')">
            <p:pipe step="main" port="source"/>
        </p:with-option>
        <p:with-option name="include-brf" select="$include-brf"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="ascii-table" select="$ascii-table"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="brf-output-dir" select="$brf-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
