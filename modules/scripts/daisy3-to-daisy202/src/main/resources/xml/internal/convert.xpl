<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
    type="px:daisy3-to-daisy202-convert" version="1.0" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 3 to DAISY 2.02</h1>
        <p px:role="desc">Transforms an audio-only DAISY 3 DTB into an audio-only DAISY 2.02
            DTB.</p>
    </p:documentation>


    <!--=========================================================================-->
    <!-- STEP SIGNATURE                                                          -->
    <!--=========================================================================-->

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="result-fileset"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="result-docs"/>
    </p:output>

    <p:option name="output-dir" required="true"/>

    <p:serialization port="fileset.out" indent="true"/>
    <p:serialization port="in-memory.out" indent="true"/>

    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <!--    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl"/>-->
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <!--    <p:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/library.xpl"/>-->
    <p:import href="convert-smils.xpl"/>

    <!--=========================================================================-->
    <!-- GLOBAL VARIABLES                                                        -->
    <!--=========================================================================-->
    <p:variable name="input-base" select="base-uri(/*)"/>

    <!--=========================================================================-->
    <!-- LOAD THE DAISY 3 FILESET                                                -->
    <!--=========================================================================-->
    <px:fileset-load media-types="application/oebps-package+xml" name="opf">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <px:fileset-load media-types="application/x-dtbncx+xml" name="ncx">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <px:fileset-load media-types="application/smil" name="input-smils">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>


    <!--=========================================================================-->
    <!-- CONVERT METADATA                                                        -->
    <!--=========================================================================-->
    <p:documentation>Convert OPF metadata to NCC metadata</p:documentation>
    <p:xslt name="metadata">
        <p:input port="source">
            <p:pipe port="result" step="opf"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="opf-to-metadata.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>


    <!--=========================================================================-->
    <!-- CONVERT NCX TO NCC                                                      -->
    <!--=========================================================================-->
    <p:documentation>Generate the NCC from the NCX</p:documentation>
    <p:group name="ncc">
        <p:output port="fileset" primary="true">
            <p:pipe port="result" step="ncc.fileset"/>
        </p:output>
        <p:output port="doc">
            <p:pipe port="result" step="ncc.doc"/>
        </p:output>

        <p:variable name="ncc-uri" select="concat($output-dir,'ncc.html')">
            <p:empty/>
        </p:variable>

        <p:xslt name="ncx-to-ncc">
            <p:input port="source">
                <p:pipe port="result" step="ncx"/>
                <p:pipe port="result" step="metadata"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncx-to-ncc.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="$ncc-uri"/>
        </p:add-attribute>
        <p:delete match="/*/@xml:base" name="ncc.doc"/>

        <p:group name="ncc.fileset">
            <p:output port="result"/>
            <px:fileset-create>
                <p:with-option name="base" select="$output-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/xhtml+xml">
                <p:with-option name="href" select="$ncc-uri"/>
            </px:fileset-add-entry>
            <px:message message="NCC document created."/>
        </p:group>
    </p:group>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT SMILS                                                           -->
    <!--=========================================================================-->

    <pxi:daisy3-to-daisy202-smils name="smils">
        <p:input port="smils">
            <p:pipe port="result" step="input-smils"/>
        </p:input>
        <p:input port="ncx">
            <p:pipe port="result" step="ncx"/>
        </p:input>
        <p:with-option name="input-dir" select="$input-base"/>
        <p:with-option name="output-dir" select="$output-dir"/>
    </pxi:daisy3-to-daisy202-smils>


    <!--=========================================================================-->
    <!-- METADATA                                                                -->
    <!--=========================================================================-->

    <!--<p:documentation>Extract metadata from the DAISY 3 OPF document</p:documentation>
            <p:group name="metadata">
                <p:output port="result"/>
                <p:xslt>
                    <p:input port="source">
                        <p:pipe port="source" step="main"/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="../internal/opf-to-metadata.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
            </p:group>
            <p:sink/>-->

    <!--=========================================================================-->
    <!-- CONSOLIDATE THE OUTPUT                                                  -->
    <!--=========================================================================-->

    <px:fileset-join name="result-fileset">
        <p:input port="source">
            <p:pipe port="fileset" step="ncc"/>
            <p:pipe port="fileset" step="smils"/>
        </p:input>
    </px:fileset-join>
    <p:identity name="result-docs">
        <p:input port="source">
            <p:pipe port="doc" step="ncc"/>
            <p:pipe port="docs" step="smils"/>
        </p:input>
    </p:identity>

</p:declare-step>
