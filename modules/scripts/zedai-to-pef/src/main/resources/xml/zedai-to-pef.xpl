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
        <h1 px:role="name">ZedAI to braille</h1>
        <p px:role="desc">Transforms a ZedAI (DAISY 4 XML) document into an embosser ready braille document.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/zedai-to-pef/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Bert Frees</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization" href="http://www.sbs-online.ch/">SBS</dd>
            </dl>
        </address>
    </p:documentation>
    
    <p:input port="source" primary="true" px:media-type="application/z3998-auth+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input ZedAI</h2>
            <p px:role="desc">The ZedAI you want to convert to braille.</p>
        </p:documentation>
    </p:input>
    
    <!-- defined in ../../../../../common-options.xpl -->
    <p:option name="stylesheet"/>
    <p:option name="transform"/>
    <p:option name="result"/>
    <p:option name="output-file-format"/>
    <p:option name="include-preview"/>
    <p:option name="preview-table"/>
    <p:option name="preview"/>
    <p:option name="include-pef"/>
    <p:option name="pef"/>
    
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>
    
    <p:import href="zedai-to-pef.convert.xpl">
        <p:documentation>
            px:zedai-to-pef
        </p:documentation>
    </p:import>
    <p:import href="xml-to-pef.store.xpl">
        <p:documentation>
            px:xml-to-pef.store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl">
        <p:documentation>
            px:zedai-load
        </p:documentation>
    </p:import>
    
    <!-- ========== -->
    <!-- LOAD ZEDAI -->
    <!-- ========== -->
    
    <px:zedai-load name="load"/>
    
    <!-- ============ -->
    <!-- ZEDAI TO PEF -->
    <!-- ============ -->
    
    <px:zedai-to-pef>
        <p:input port="source.in-memory">
            <p:pipe step="load" port="in-memory.out"/>
        </p:input>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="transform" select="$transform"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
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
        <p:with-option name="include-pef" select="$include-pef"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="output-file-format" select="$output-file-format"/>
        <p:with-option name="preview-table" select="$preview-table"/>
        <p:with-option name="output-dir" select="$result"/>
        <p:with-option name="pef-output-dir" select="$pef"/>
        <p:with-option name="preview-output-dir" select="$preview"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
