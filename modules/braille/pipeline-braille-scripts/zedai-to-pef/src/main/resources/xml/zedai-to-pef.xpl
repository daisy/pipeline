<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:zedai-to-pef" version="1.0"
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
        <a px:role="homepage" href="http://code.google.com/p/daisy-pipeline/wiki/ZedAIToPEFDoc">
            http://code.google.com/p/daisy-pipeline/wiki/ZedAIToPEFDoc
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
            <h2 px:role="name">source</h2>
            <p px:role="desc">Input ZedAI.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="stylesheet" required="false" px:type="string" select="''">
        <p:documentation>
            <h2 px:role="name">stylesheet</h2>
            <p px:role="desc">CSS style sheets to apply. Space separated list of absolute or relative URIs. Applied prior to any style sheets linked from or embedded in the source document.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="transform" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">transform</h2>
            <p px:role="desc">A transformer query.</p>
            <pre><code class="default">(translator:liblouis)(formatter:dotify)</code></pre>
        </p:documentation>
    </p:option>
    
    <p:option name="ascii-table" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ascii-table</h2>
            <p px:role="desc">The ASCII braille table, used for example to render BRF files.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="include-preview" required="false" px:type="boolean" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">include-preview</h2>
            <p px:role="desc">Whether or not to include a preview of the PEF in HTML.</p>
            <pre><code class="default">false</code></pre>
        </p:documentation>
    </p:option>
    
    <p:option name="include-brf" required="false" px:type="boolean" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">include-brf</h2>
            <p px:role="desc">Whether or not to include an ASCII version of the PEF.</p>
            <pre><code class="default">false</code></pre>
        </p:documentation>
    </p:option>
    
    <p:option name="pef-output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">PEF</h2>
            <h2 px:role="desc">Output directory for the PEF</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="brf-output-dir" px:output="result" px:type="anyDirURI" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">BRF</h2>
            <h2 px:role="desc">Output directory for the BRF</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="preview-output-dir" px:output="result" px:type="anyDirURI" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Preview</h2>
            <h2 px:role="desc">Output directory for the HTML preview</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="false" px:output="temp" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">temp-dir</h2>
            <p px:role="desc">Directory for storing temporary files.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="zedai-to-pef.convert.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    
    <!-- =============== -->
    <!-- CREATE TEMP DIR -->
    <!-- =============== -->
    
    <px:tempdir name="temp-dir">
        <p:with-option name="href" select="if ($temp-dir!='') then $temp-dir else $pef-output-dir"/>
    </px:tempdir>
    <p:sink/>
    
    <!-- ============ -->
    <!-- ZEDAI TO PEF -->
    <!-- ============ -->
    
    <px:zedai-to-pef.convert default-stylesheet="http://www.daisy.org/pipeline/modules/braille/zedai-to-pef/css/default.css">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="transform" select="if ($transform!='') then $transform
                                                else '(translator:liblouis)(formatter:dotify)'"/>
        <p:with-option name="temp-dir" select="string(/c:result)">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
    </px:zedai-to-pef.convert>
    
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
