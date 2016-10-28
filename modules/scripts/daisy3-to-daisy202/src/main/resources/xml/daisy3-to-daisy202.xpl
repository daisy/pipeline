<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
    type="px:daisy3-to-daisy202" version="1.0" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 3 to DAISY 2.02</h1>
        <p px:role="desc">Transforms an audio-only DAISY 3 DTB into an audio-only DAISY 2.02
            DTB.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/daisy3-to-daisy202">
            Online documentation
        </a>
    </p:documentation>


    <!--=========================================================================-->
    <!-- STEP SIGNATURE                                                          -->
    <!--=========================================================================-->

    <p:input port="source" primary="true" sequence="false"
        px:media-type="application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">OPF</h2>
            <p px:role="desc">The package file of the input DAISY 3 DTB.</p>
        </p:documentation>
    </p:input>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DAISY 2.02</h2>
            <p px:role="desc">The produced DAISY 2.02 DTB.</p>
        </p:documentation>
    </p:option>


    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="internal/convert.xpl"/>


    <!--=========================================================================-->
    <!-- GLOBAL VARIABLES                                                        -->
    <!--=========================================================================-->
    <p:variable name="output-dir-checked"
        select="resolve-uri(replace($output-dir,'(.+?)/?$','$1/'))"/>
    <p:variable name="type" xmlns:opf="http://openebook.org/namespaces/oeb-package/1.0/"
        select="/opf:package/opf:metadata/opf:x-metadata/
                 opf:meta[@name='dtb:multimediaType'][1]/@content"/>

    <!--=========================================================================-->
    <!-- CHECK THE DTB TYPE                                                      -->
    <!--=========================================================================-->
    <px:assert error-code="RUNTIME_ERROR" message="The input DTB type ('$1') is not supported.">
        <p:with-option name="test" select="$type='audioNCX'"/>
        <p:with-option name="param1" select="$type"/>
    </px:assert>

    <!--=========================================================================-->
    <!-- LOAD THE DAISY 3 FILESET                                                -->
    <!--=========================================================================-->
    <px:daisy3-load name="load"/>

    <!--=========================================================================-->
    <!-- RUN THE MAIN CONVERSION STEP                                            -->
    <!--=========================================================================-->
    <px:daisy3-to-daisy202-convert name="convert">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
        <p:with-option name="output-dir" select="$output-dir-checked"/>
    </px:daisy3-to-daisy202-convert>

    <!--=========================================================================-->
    <!-- FINALIZE AND STORE THE CONTAINER                                        -->
    <!--=========================================================================-->
    <px:fileset-store>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="convert"/>
        </p:input>
    </px:fileset-store>

</p:declare-step>
