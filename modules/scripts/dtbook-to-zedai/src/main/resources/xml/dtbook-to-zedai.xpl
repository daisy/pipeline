<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:dtbook-to-zedai.script"
                px:input-filesets="dtbook"
                px:output-filesets="zedai"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to ZedAI</h1>
        <p px:role="desc">Transforms DTBook XML into ZedAI XML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-zedai/">
            Online documentation
        </a>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI</h2>
            <p px:role="desc">The resulting ZedAI XML file.</p>
        </p:documentation>
    </p:option>
    <p:option name="zedai-filename" required="false" px:type="string" select="'zedai.xml'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI filename</h2>
            <p px:role="desc">Filename for the generated ZedAI file</p>
        </p:documentation>
    </p:option>
    <p:option name="validation" select="'abort'">
        <!-- defined in common-options.xpl -->
    </p:option>
    <p:option name="mods-filename" required="false" px:type="string" select="'zedai-mods.xml'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">MODS filename</h2>
            <p px:role="desc">Filename for the generated MODS file</p>
        </p:documentation>
    </p:option>
    <p:option name="css-filename" required="false" px:type="string" select="'zedai-css.css'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">CSS filename</h2>
            <p px:role="desc">Filename for the generated CSS file</p>
        </p:documentation>
    </p:option>
    <p:option name="lang" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>
    <p:option name="copy-external-resources" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Copy external resources</h2>
            <p px:role="desc">Include any referenced external resources like images and CSS-files to the output.</p>
        </p:documentation>
    </p:option>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="dtbook-to-zedai.convert.xpl">
        <p:documentation>
            px:dtbook-to-zedai
        </p:documentation>
    </p:import>

    <px:dtbook-load name="load"/>

    <px:dtbook-to-zedai name="convert">
        <p:input port="source.in-memory">
            <p:pipe step="load" port="in-memory.out"/>
        </p:input>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="zedai-filename" select="$zedai-filename"/>
        <p:with-option name="mods-filename" select="$mods-filename"/>
        <p:with-option name="css-filename" select="$css-filename"/>
        <p:with-option name="lang" select="$lang"/>
        <p:with-option name="validation" select="$validation"/>
        <p:with-option name="copy-external-resources" select="$copy-external-resources='true'"/>
    </px:dtbook-to-zedai>

    <px:fileset-store name="fileset-store">
        <p:input port="in-memory.in">
            <p:pipe step="convert" port="result.in-memory"/>
        </p:input>
    </px:fileset-store>

</p:declare-step>
