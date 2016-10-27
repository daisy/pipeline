<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="main" type="px:dtbook-to-html"
    px:input-filesets="dtbook"
    px:output-filesets="html"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:z="http://www.daisy.org/ns/z3986/authoring/"
    xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to HTML</h1>
        <p px:role="desc">Transforms DTBook XML into HTML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/dtbook-to-html">
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
            <h2 px:role="name">Output directory</h2>
            <p px:role="desc">Directory where the resulting HTML document is stored.</p>
        </p:documentation>
    </p:option>

    <p:option name="assert-valid" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Assert validity</h2>
            <p px:role="desc">Whether to stop processing and raise an error on validation
                issues.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import
        href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    
    <p:split-sequence name="first-dtbook" test="position()=1" initial-only="true"/>
    <p:sink/>
    

    <p:xslt name="output-dir-uri">
        <p:with-param name="href" select="concat($output-dir,'/')">
            <p:empty/>
        </p:with-param>
        <p:input port="source">
            <p:inline>
                <d:file/>
            </p:inline>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                    <xsl:import
                        href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                    <xsl:param name="href" required="yes"/>
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:attribute name="href" select="pf:normalize-uri($href)"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:sink/>

    
    <p:group>
    <!--<p:variable name="encoded-title" select="encode-for-uri(replace(//dtbook:meta[@name='dc:Title']/@content,'[/\\?%*:|&quot;&lt;&gt;]',''))"/>-->
    <!--<p:variable name="encoded-title" select="'book'"/>-->
        <p:variable name="encoded-title"
            select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:variable>
        <p:variable name="output-dir-uri" select="/*/@href">
            <p:pipe port="result" step="output-dir-uri"/>
        </p:variable>
        <p:variable name="html-file-uri" select="concat($output-dir,$encoded-title,'.epub')"/>

        <px:dtbook-load name="load">
            <p:input port="source">
                <p:pipe step="main" port="source"/>
            </p:input>
        </px:dtbook-load>
        

        <px:message message="Converting to ZedAI..."/>
        <px:dtbook-to-zedai-convert name="to-zedai">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="load"/>
            </p:input>
            <p:with-option name="opt-output-dir" select="concat($output-dir-uri,'zedai/')"/>
            <p:with-option name="opt-zedai-filename" select="concat($encoded-title,'.xml')"/>
            <p:with-option name="opt-lang" select="$language"/>
            <p:with-option name="opt-assert-valid" select="$assert-valid"/>
        </px:dtbook-to-zedai-convert>

        <px:message message="Converting to XHTML5..."/>
        <px:zedai-to-html-convert name="to-html">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="to-zedai"/>
            </p:input>
            <p:with-option name="output-dir" select="$output-dir-uri"/>
        </px:zedai-to-html-convert>

        <px:fileset-store name="store">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="to-html"/>
            </p:input>
        </px:fileset-store>

    </p:group>

</p:declare-step>
