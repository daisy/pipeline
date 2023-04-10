<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
                type="px:daisy3-to-daisy202.script"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 3 to DAISY 2.02</h1>
        <p px:role="desc">Transforms an audio-only DAISY 3 DTB into an audio-only DAISY 2.02
            DTB.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy3-to-daisy202/">
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

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:option xmlns:_="daisy202" name="_:ensure-core-media" select="'false'">
        <!-- defined in ../../../../../scripts/common-options.xpl -->
    </p:option>

    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
        <p:documentation>
            px:daisy3-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="internal/convert.xpl">
        <p:documentation>
            px:daisy3-to-daisy202
        </p:documentation>
    </p:import>


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
    <px:fileset-create>
        <p:with-option name="base" select="resolve-uri('./',base-uri(/*))"/>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/oebps-package+xml">
        <p:input port="entry">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:fileset-add-entry>
    <px:daisy3-load name="load" px:message="Loading DAISY 3"/>

    <!--=========================================================================-->
    <!-- RUN THE MAIN CONVERSION STEP                                            -->
    <!--=========================================================================-->
    <px:daisy3-to-daisy202 name="convert" px:message="Downgrading DAISY 3 to DAISY 2.02">
        <p:input port="in-memory.in">
            <p:pipe step="load" port="result.in-memory"/>
        </p:input>
        <p:with-option name="output-dir" select="$output-dir-checked"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
        <p:with-option name="ensure-core-media" xmlns:_="daisy202" select="$_:ensure-core-media='true'"/>
    </px:daisy3-to-daisy202>

    <!--=========================================================================-->
    <!-- FINALIZE AND STORE THE CONTAINER                                        -->
    <!--=========================================================================-->
    <px:fileset-store px:message="Storing DAISY 2.02">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="convert"/>
        </p:input>
    </px:fileset-store>

</p:declare-step>
