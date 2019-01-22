<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3"
                type="px:daisy3-to-epub3" version="1.0" name="main">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="convert" port="result.in-memory"/>
    </p:output>

    <p:option name="mediaoverlays" required="true"/>
    <p:option name="assert-valid" required="true"/>
    <p:option name="chunk-size" required="false" select="'-1'"/>
    <p:option name="temp-dir" required="true"/>


    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/library.xpl"/>
    <p:import href="../internal/ncx-to-nav.xpl"/>
    <p:import href="../internal/dtbook-to-html.xpl"/>
    <p:import href="../internal/list-audio-clips.xpl"/>


    <!--=========================================================================-->
    <!-- LOAD THE DAISY 3 FILESET                                                -->
    <!--=========================================================================-->
    <px:fileset-load media-types="application/oebps-package+xml" name="opf">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:fileset-load media-types="application/smil" name="smils">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbooks">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:fileset-load media-types="application/x-dtbncx+xml" name="ncx">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>

    <!--
        Make sure that the base uri of the fileset is the directory containing the OPF. This should
        normally eliminate any relative hrefs starting with "..", which is required for this step
        to work.
    -->
    <px:fileset-rebase>
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:with-option name="new-base" select="resolve-uri('.',base-uri(/*))">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
    </px:fileset-rebase>
    <p:identity name="source.fileset"/>


    <p:group name="convert">
    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="finalize" port="in-memory.out"/>
    </p:output>

    <!--=========================================================================-->
    <!-- GLOBAL VARIABLES                                                        -->
    <!--=========================================================================-->
    <!--FIXME check options-->
    <p:variable name="temp-dir-checked" select="resolve-uri(replace($temp-dir,'(.+?)/?$','$1/'))"/>
    <p:variable name="epub-dir" select="concat($temp-dir-checked,'epub/')"/>
    <p:variable name="content-dir" select="concat($temp-dir-checked,'epub/EPUB/')"/>
    <p:variable name="type" xmlns:opf="http://openebook.org/namespaces/oeb-package/1.0/"
        select="for $daisy-type in /opf:package/opf:metadata/opf:x-metadata/opf:meta[@name='dtb:multimediaType']/@content
                return
                    if ($daisy-type=('audioFullText','audioPartText','textPartAudio')) then 'text+mo'
                    else if ($daisy-type='textNCX') then 'text'
                    else ''">
        <p:pipe step="opf" port="result"/>
    </p:variable>

    <!--=========================================================================-->
    <!-- CHECK THE DTB TYPE                                                      -->
    <!--=========================================================================-->

    <!--TODO fail if DTB type is not supported (e.g. audio-only books) -->


    <!--=========================================================================-->
    <!-- CREATE A MAP OF CONTENT IDs TO AUDIO FRAGMENTS                          -->
    <!--=========================================================================-->

    <!--TODO conditionally, if the MO option is set-->
    <p:choose name="audio-clips">
        <p:when test="$mediaoverlays and $type='text+mo'">
            <p:output port="fileset.out" primary="true"/>
            <p:output port="audio-clips" sequence="true">
                <p:pipe port="audio-clips" step="audio-clips.inner"/>
            </p:output>
            <pxi:list-audio-clips name="audio-clips.inner">
                <p:input port="fileset.in">
                    <p:pipe step="source.fileset" port="result"/>
                </p:input>
                <p:input port="dtbooks">
                    <p:pipe port="result" step="dtbooks"/>
                </p:input>
                <p:input port="smils">
                    <p:pipe port="result" step="smils"/>
                </p:input>
                <p:with-option name="content-dir" select="$content-dir"/>
            </pxi:list-audio-clips>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="audio-clips" sequence="true">
                <p:empty/>
            </p:output>
            <p:identity>
                <p:input port="source"><p:empty/></p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT DTBOOK TO XHTML                                                 -->
    <!--=========================================================================-->

    <pxi:dtbook-to-html name="content-docs">
        <p:input port="fileset.in">
            <p:pipe step="source.fileset" port="result"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="result" step="dtbooks"/>
        </p:input>
        <p:with-option name="output-dir" select="$content-dir"/>
        <p:with-option name="assert-valid" select="$assert-valid"/>
        <p:with-option name="chunk-size" select="$chunk-size"/>
        <p:with-option name="filename"
                       select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
    </pxi:dtbook-to-html>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT NCX TO NAVIGATION DOCUMENT                                      -->
    <!--=========================================================================-->
    <pxi:ncx-to-nav name="nav-doc">
        <p:input port="source">
            <p:pipe port="result" step="ncx"/>
        </p:input>
        <p:input port="smils">
            <p:pipe port="result" step="smils"/>
        </p:input>
        <p:input port="dtbooks">
            <p:pipe port="result" step="dtbooks"/>
        </p:input>
        <p:input port="htmls">
            <p:pipe port="in-memory.out" step="content-docs"/>
        </p:input>
        <p:with-option name="result-uri" select="concat($content-dir,'nav.xhtml')"/>
        <!--TODO make sure that the name is unused-->
    </pxi:ncx-to-nav>
    <p:sink/>

    <!--=========================================================================-->
    <!-- GENERATE MEDIA OVERLAYS                                                 -->
    <!--=========================================================================-->

    <!--TODO conditionally, if the MO option is set-->
    <p:choose name="media-overlays">
        <p:when test="$mediaoverlays and $type='text+mo'">
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="in-memory.out" step="media-overlays.inner"/>
            </p:output>
            <px:create-mediaoverlays name="media-overlays.inner">
                <p:input port="content-docs">
                    <p:pipe port="in-memory.out" step="content-docs"/>
                </p:input>
                <p:input port="audio-map">
                    <p:pipe port="audio-clips" step="audio-clips"/>
                </p:input>
                <p:with-option name="content-dir" select="$content-dir"/>
            </px:create-mediaoverlays>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:empty/>
            </p:output>
            <p:identity>
                <p:input port="source"><p:empty/></p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:sink/>

    <!--=========================================================================-->
    <!-- METADATA                                                                -->
    <!--=========================================================================-->

    <p:documentation>Extract metadata from the DAISY 3 OPF document</p:documentation>
    <p:group name="metadata">
        <p:output port="result"/>
        <p:xslt>
            <p:input port="source">
                <p:pipe port="result" step="opf"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../internal/opf-to-metadata.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:group>
    <p:sink/>


    <!--=========================================================================-->
    <!-- CREATE THE PACKAGE DOCUMENT                                             -->
    <!--=========================================================================-->

    <p:group name="package-doc">
        <p:output port="fileset.out" primary="true"/>
        <p:output port="in-memory.out">
            <p:pipe port="result" step="package-doc.create"/>
        </p:output>

        <p:variable name="opf-uri" select="concat($content-dir,'package.opf')"/>

        <px:fileset-join name="package-doc.join-filesets">
            <p:input port="source">
                <p:pipe port="fileset.out" step="content-docs"/>
                <p:pipe port="fileset.out" step="nav-doc"/>
                <p:pipe port="fileset.out" step="media-overlays"/>
                <p:pipe port="fileset.out" step="audio-clips"/>
            </p:input>
        </px:fileset-join>
        <p:sink/>

        <px:epub3-pub-create-package-doc name="package-doc.create">
            <p:input port="spine-filesets">
                <p:pipe port="fileset.out" step="content-docs"/>
            </p:input>
            <p:input port="publication-resources">
                <p:pipe port="result" step="package-doc.join-filesets"/>
            </p:input>
            <p:input port="metadata">
                <p:pipe port="result" step="metadata"/>
            </p:input>
            <p:input port="content-docs">
                <p:pipe port="in-memory.out" step="nav-doc"/>
                <p:pipe port="in-memory.out" step="content-docs"/>
            </p:input>
            <p:input port="mediaoverlays">
                <p:pipe port="in-memory.out" step="media-overlays"/>
            </p:input>
            <p:with-option name="result-uri" select="$opf-uri"/>
            <p:with-option name="compatibility-mode" select="'false'"/>
            <!--TODO configurability for other META-INF files ?-->
        </px:epub3-pub-create-package-doc>

        <px:fileset-add-entry media-type="application/oebps-package+xml">
            <p:input port="source">
                <p:pipe port="result" step="package-doc.join-filesets"/>
            </p:input>
            <p:with-option name="href" select="$opf-uri"/>
        </px:fileset-add-entry>

        <px:message message="Package Document Created."/>
    </p:group>
    <p:sink/>

    <!--=========================================================================-->
    <!-- FINALIZE AND STORE THE CONTAINER                                        -->
    <!--=========================================================================-->

    <p:group name="finalize">
        <p:output port="fileset.out" primary="true">
            <p:pipe port="result" step="finalize.ocf"/>
        </p:output>
        <p:output port="in-memory.out" sequence="true">
            <p:pipe port="result" step="finalize.in-memory"/>
        </p:output>

        <px:fileset-create name="fileset.with-epub-base">
            <p:with-option name="base" select="$epub-dir"/>
        </px:fileset-create>
        <px:fileset-join name="fileset.without-ocf">
            <p:input port="source">
                <p:pipe port="result" step="fileset.with-epub-base"/>
                <p:pipe port="fileset.out" step="package-doc"/>
            </p:input>
        </px:fileset-join>
        <p:sink/>
        <px:epub3-ocf-finalize name="finalize.ocf">
            <p:input port="source">
                <p:pipe port="result" step="fileset.without-ocf"/>
            </p:input>
        </px:epub3-ocf-finalize>

        <p:identity name="finalize.in-memory">
            <p:input port="source">
                <p:pipe port="in-memory.out" step="nav-doc"/>
                <p:pipe port="in-memory.out" step="content-docs"/>
                <p:pipe port="in-memory.out" step="package-doc"/>
                <p:pipe port="in-memory.out" step="media-overlays"/>
                <p:pipe port="in-memory.out" step="finalize.ocf"/>
            </p:input>
        </p:identity>
        <p:sink/>
    </p:group>
    </p:group>

</p:declare-step>
