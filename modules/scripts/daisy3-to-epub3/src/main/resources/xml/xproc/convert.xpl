<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:daisy3-to-epub3" version="1.0" name="main">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="convert" port="result.in-memory"/>
    </p:output>

    <p:option name="mediaoverlays" cx:as="xs:boolean" required="true"/>
    <p:option name="validation" cx:type="off|report|abort" select="'off'">
        <p:documentation>
            <p>Whether to stop processing and raise an error on validation issues (abort), only
            report them (report), or to ignore any validation issues (off).</p>
        </p:documentation>
    </p:option>
    <p:option name="chunk-size" required="false" select="'-1'"/>
    <p:option name="temp-dir" required="true"/>

    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-to-html/library.xpl">
        <p:documentation>
            px:dtbook-to-html
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-create-mediaoverlays
            px:epub3-create-package-doc
            px:epub3-ocf-finalize
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-join
            px:fileset-load
            px:fileset-rebase
            px:fileset-create
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-audio-clips
            px:audio-clips-update-files
        </p:documentation>
    </p:import>
    <p:import href="../internal/ncx-to-nav.xpl">
        <p:documentation>
            pxi:ncx-to-nav
        </p:documentation>
    </p:import>
    <p:import href="../internal/oebps-to-opf-metadata.xpl">
        <p:documentation>
            px:oebps-to-opf-metadata
        </p:documentation>
    </p:import>


    <!--=========================================================================-->
    <!-- LOAD THE DAISY 3 FILESET                                                -->
    <!--=========================================================================-->
    <p:group name="opf">
        <p:output port="result"/>
        <px:fileset-load media-types="application/oebps-package+xml">
            <p:input port="fileset">
                <p:pipe step="main" port="source.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:assert test-count-min="1" test-count-max="1" error-code="XXXX"
                   message="The input fileset must contain exactly one OPF file"/>
    </p:group>
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
    <p:group name="ncx">
        <p:output port="result"/>
        <px:fileset-load media-types="application/x-dtbncx+xml">
            <p:input port="fileset">
                <p:pipe step="main" port="source.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:assert test-count-min="1" test-count-max="1" error-code="XXXX"
                   message="The input fileset must contain exactly one NCX file"/>
    </p:group>

    <!--
        Make sure that the base uri of the fileset is the directory containing the OPF. This should
        normally eliminate any relative hrefs starting with "..", which is required for this step
        to work.
    -->
    <px:fileset-rebase px:progress="1/20">
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:with-option name="new-base" select="resolve-uri('.',base-uri(/*))">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
    </px:fileset-rebase>
    <p:identity name="source.fileset"/>


    <p:group name="convert" px:progress="19/20">
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
    <p:choose px:progress="1/19">
        <p:when test="$mediaoverlays and $type='text+mo'">
            <px:smil-to-audio-clips px:progress="1">
                <p:input port="source">
                    <p:pipe step="smils" port="result"/>
                </p:input>
                <p:with-option name="output-base-uri" select="base-uri(/*)">
                    <p:pipe step="source.fileset" port="result"/>
                </p:with-option>
            </px:smil-to-audio-clips>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:inline><d:audio-clips/></p:inline>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <px:audio-clips-update-files name="audio-clips">
        <p:input port="mapping">
            <p:pipe step="content-docs" port="mapping"/>
        </p:input>
        <p:with-option name="output-base-uri" select="base-uri(/*)"/>
    </px:audio-clips-update-files>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT DTBOOK TO XHTML                                                 -->
    <!--=========================================================================-->

    <px:dtbook-to-html name="content-docs" chunk="true" px:progress="4/19" px:message="Converting DTBook to XHTML">
        <p:input port="source.fileset">
            <p:pipe step="source.fileset" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe port="result" step="dtbooks"/>
        </p:input>
        <p:with-option name="output-dir" select="$content-dir"/>
        <p:with-option name="temp-dir" select="concat($temp-dir-checked,'dtbook-to-html/')"/>
        <p:with-option name="validation" select="$validation"/>
        <p:with-option name="chunk-size" select="$chunk-size"/>
        <p:with-option name="filename"
                       select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
    </px:dtbook-to-html>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT NCX TO NAVIGATION DOCUMENT                                      -->
    <!--=========================================================================-->
    <pxi:ncx-to-nav name="nav-doc" px:progress="1/19" px:message="Converting NCX to EPUB navigation document">
        <p:input port="source">
            <p:pipe port="result" step="ncx"/>
        </p:input>
        <p:input port="smils">
            <p:pipe port="result" step="smils"/>
        </p:input>
        <p:input port="dtbooks">
            <p:pipe port="result" step="dtbooks"/>
        </p:input>
        <p:input port="dtbook-html-mapping">
            <p:pipe step="content-docs" port="mapping"/>
        </p:input>
        <p:with-option name="result-uri" select="concat($content-dir,'nav.xhtml')"/>
        <!--TODO make sure that the name is unused-->
    </pxi:ncx-to-nav>
    <p:sink/>

    <!--=========================================================================-->
    <!-- GENERATE MEDIA OVERLAYS                                                 -->
    <!--=========================================================================-->

    <p:choose name="media-overlays" px:progress="3/19">
        <p:when test="$mediaoverlays and $type='text+mo'" px:message="Creating EPUB media overlay documents">
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result.in-memory" step="media-overlays.inner"/>
            </p:output>
            <px:epub3-create-mediaoverlays flatten="false" name="media-overlays.inner" px:progress="1">
                <p:input port="source.fileset">
                    <p:pipe step="content-docs" port="result.fileset"/>
                </p:input>
                <p:input port="source.in-memory">
                    <p:pipe step="content-docs" port="result.in-memory"/>
                </p:input>
                <p:input port="audio-map">
                    <p:pipe step="audio-clips" port="result"/>
                </p:input>
                <p:with-option name="audio-dir" select="concat($content-dir,'audio/')"/>
                <p:with-option name="mediaoverlay-dir" select="concat($content-dir,'mo/')"/>
            </px:epub3-create-mediaoverlays>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:empty/>
            </p:output>
            <p:identity>
                <p:input port="source">
                    <p:inline><d:fileset/></p:inline>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:sink/>

    <!--=========================================================================-->
    <!-- METADATA                                                                -->
    <!--=========================================================================-->

    <p:documentation>Extract metadata from the DAISY 3 OPF document</p:documentation>
    <px:oebps-to-opf-metadata name="metadata" px:progress="1/19">
        <p:input port="source">
            <p:pipe step="opf" port="result"/>
        </p:input>
    </px:oebps-to-opf-metadata>
    <p:sink/>


    <!--=========================================================================-->
    <!-- CREATE THE PACKAGE DOCUMENT                                             -->
    <!--=========================================================================-->

    <p:group name="package-doc" px:progress="8/19" px:message="Creating EPUB package document">
        <p:output port="fileset.out" primary="true"/>
        <p:output port="in-memory.out">
            <p:pipe port="result" step="package-doc.create"/>
        </p:output>

        <p:variable name="opf-uri" select="concat($content-dir,'package.opf')"/>

        <px:fileset-join name="package-doc.join-filesets">
            <p:input port="source">
                <p:pipe step="content-docs" port="result.fileset"/>
                <p:pipe step="nav-doc" port="fileset.out"/>
                <p:pipe step="media-overlays" port="fileset.out"/>
            </p:input>
        </px:fileset-join>
        <p:sink/>

        <px:epub3-create-package-doc name="package-doc.create" px:progress="1">
            <p:input port="source.fileset">
                <p:pipe step="package-doc.join-filesets" port="result"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="nav-doc" port="in-memory.out"/>
                <p:pipe step="content-docs" port="result.in-memory"/>
                <p:pipe step="media-overlays" port="in-memory.out"/>
            </p:input>
            <p:input port="spine">
                <p:pipe step="content-docs" port="result.fileset"/>
            </p:input>
            <p:input port="metadata">
                <p:pipe step="metadata" port="result"/>
            </p:input>
            <p:with-option name="output-base-uri" select="$opf-uri"/>
            <p:with-option name="compatibility-mode" select="'false'"/>
            <!--TODO configurability for other META-INF files ?-->
        </px:epub3-create-package-doc>
        <p:sink/>

        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="package-doc.join-filesets"/>
                <p:pipe step="package-doc.create" port="result.fileset"/>
            </p:input>
        </px:fileset-join>
    </p:group>
    <p:sink/>

    <!--=========================================================================-->
    <!-- FINALIZE AND STORE THE CONTAINER                                        -->
    <!--=========================================================================-->

    <p:group name="finalize" px:progress="1/19">
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
        <px:epub3-ocf-finalize name="finalize.ocf" px:progress="1">
            <p:input port="source">
                <p:pipe port="result" step="fileset.without-ocf"/>
            </p:input>
        </px:epub3-ocf-finalize>

        <p:identity name="finalize.in-memory">
            <p:input port="source">
                <p:pipe port="in-memory.out" step="nav-doc"/>
                <p:pipe port="result.in-memory" step="content-docs"/>
                <p:pipe port="in-memory.out" step="package-doc"/>
                <p:pipe port="in-memory.out" step="media-overlays"/>
                <p:pipe port="in-memory.out" step="finalize.ocf"/>
            </p:input>
        </p:identity>
        <p:sink/>
    </p:group>
    </p:group>

</p:declare-step>
