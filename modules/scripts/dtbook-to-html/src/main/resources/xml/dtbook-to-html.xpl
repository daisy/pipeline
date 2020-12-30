<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:dtbook-to-html.script" name="main"
                px:input-filesets="dtbook"
                px:output-filesets="html"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to HTML</h1>
        <p px:role="desc">Transforms DTBook XML into HTML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-html/">
            Online documentation
        </a>
    </p:documentation>
    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple
                files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">The resulting HTML document.</p>
        </p:documentation>
    </p:option>

    <p:option name="assert-valid" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Assert validity</h2>
            <p px:role="desc">Whether to stop processing and raise an error on validation
                issues.</p>
        </p:documentation>
    </p:option>

    <p:option xmlns:_="dtbook" name="_:chunk-size" select="'-1'">
        <!-- defined in common-options.xpl -->
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="convert.xpl"/>

    <px:normalize-uri name="output-dir-uri">
        <p:with-option name="href" select="concat($output-dir,'/')"/>
    </px:normalize-uri>
    
    <!-- get the HTML filename from the first DTBook -->
    <p:split-sequence name="first-dtbook" test="position()=1" initial-only="true"/>
    <p:sink/>
    
    <p:group>
    <!--<p:variable name="encoded-title" select="encode-for-uri(replace(//dtbook:meta[@name='dc:Title']/@content,'[/\\?%*:|&quot;&lt;&gt;]',''))"/>-->
    <!--<p:variable name="encoded-title" select="'book'"/>-->
        <p:variable name="encoded-title"
            select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:variable>
        <p:variable name="output-dir-uri" select="/c:result/string()">
            <p:pipe step="output-dir-uri" port="normalized"/>
        </p:variable>
        <p:variable name="html-file-uri" select="concat($output-dir,$encoded-title,'.epub')"/>

        <px:dtbook-load name="load">
            <p:input port="source">
                <p:pipe step="main" port="source"/>
            </p:input>
        </px:dtbook-load>

        <px:dtbook-to-html name="convert">
            <p:input port="source.in-memory">
                <p:pipe step="load" port="in-memory.out"/>
            </p:input>
            <p:with-option name="language" select="$language"/>
            <p:with-option name="assert-valid" select="$assert-valid"/>
            <p:with-option name="chunk-size" xmlns:_="dtbook" select="$_:chunk-size"/>
            <p:with-option name="output-dir" select="$output-dir-uri"/>
            <p:with-option name="filename" select="$encoded-title"/>
        </px:dtbook-to-html>

        <px:fileset-store name="store">
            <p:input port="in-memory.in">
                <p:pipe step="convert" port="result.in-memory"/>
            </p:input>
        </px:fileset-store>

    </p:group>

</p:declare-step>
