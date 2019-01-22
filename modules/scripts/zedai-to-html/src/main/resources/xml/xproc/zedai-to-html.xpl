<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                px:input-filesets="zedai"
                px:output-filesets="html"
                type="px:zedai-to-html.script" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">ZedAI to HTML</h1>
        <p px:role="desc">Transforms ZedAI XML (ANSI/NISO Z39.98-2012 Authoring and Interchange) into HTML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/zedai-to-html">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" px:name="source" px:media-type="application/z3998-auth+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI document</h2>
            <p px:role="desc">Input ZedAI.</p>
        </p:documentation>
    </p:input>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">The resulting HTML file.</p>
        </p:documentation>
    </p:option>

    <p:import href="zedai-to-html.convert.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <p:variable name="input-uri" select="base-uri(/)"/>
    
    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    <p:sink/>

    <p:group>
        <p:variable name="output-dir-uri" select="/c:result/string()">
            <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>

        <px:zedai-load name="load">
            <p:input port="source">
                <p:pipe port="source" step="main"/>
            </p:input>
        </px:zedai-load>

        <px:zedai-to-html name="convert">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="load"/>
            </p:input>
            <p:with-option name="output-dir" select="$output-dir-uri"/>
        </px:zedai-to-html>

        <px:fileset-store name="store">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="convert"/>
            </p:input>
        </px:fileset-store>
    </p:group>

</p:declare-step>
