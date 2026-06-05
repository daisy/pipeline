<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:daisy3-to-daisy202" version="1.0" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DAISY 3 to DAISY 2.02</h1>
        <p>Transforms an audio-only DAISY 3 DTB into an audio-only DAISY 2.02
            DTB.</p>
    </p:documentation>


    <!--=========================================================================-->
    <!-- STEP SIGNATURE                                                          -->
    <!--=========================================================================-->

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="ensure-core-media" port="in-memory"/>
    </p:output>

    <p:option name="date" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Date of publication of the result DAISY 2.02 DTB.</p>
            <p>Must be a ISO8601 date - recommended format is YYYY-MM-DD.</p>
            <p>Defaults to the current date.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true"/>

    <p:option name="temp-dir" required="true" cx:as="xs:anyURI">
        <p:documentation>
            <p>Empty directory dedicated to this step.</p>
        </p:documentation>
    </p:option>

    <p:option name="ensure-core-media" cx:as="xs:boolean" select="false()">
        <p:documentation>
            <p>Ensure that the output DAISY 2.02 uses allowed file formats only.</p>
        </p:documentation>
    </p:option>

    <p:serialization port="fileset.out" indent="true"/>
    <p:serialization port="in-memory.out" indent="true"/>

    <!--=========================================================================-->
    <!-- IMPORTS                                                                 -->
    <!--=========================================================================-->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-rebase
            px:fileset-create
            px:fileset-add-entry
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl">
        <p:documentation>
            px:daisy3-upgrade
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl">
        <p:documentation>
            px:daisy202-audio-transcode
        </p:documentation>
    </p:import>
    <p:import href="convert-smils.xpl">
        <p:documentation>
            pxi:daisy3-to-daisy202-smils
        </p:documentation>
    </p:import>
    <p:import href="oebps-to-ncc-metadata.xpl">
        <p:documentation>
            px:oebps-to-ncc-metadata
        </p:documentation>
    </p:import>

    <!--=========================================================================-->
    <!-- UPGRADE to 2005 STANDARD IF NEEDED                                      -->
    <!--=========================================================================-->
    <px:daisy3-upgrade name="upgrade">
        <p:input port="source.in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:daisy3-upgrade>
    
    <!--=========================================================================-->
    <!-- LOAD THE DAISY 3 FILESET                                                -->
    <!--=========================================================================-->
    <px:fileset-load media-types="application/oebps-package+xml" name="opf">
        <p:input port="in-memory">
            <p:pipe step="upgrade" port="result.in-memory"/>
        </p:input>
    </px:fileset-load>

    <!--
        Make sure that the base uri of the fileset is the directory containing the OPF. This should
        normally eliminate any relative hrefs starting with "..", which is required for this step
        to work.
    -->
    <px:fileset-rebase>
        <p:input port="source">
            <p:pipe step="upgrade" port="result.fileset"/>
        </p:input>
        <p:with-option name="new-base" select="resolve-uri('.',base-uri(/*))">
            <p:pipe step="opf" port="result"/>
        </p:with-option>
    </px:fileset-rebase>
    <p:identity name="fileset.in"/>


    <!--=========================================================================-->
    <!-- CONVERT METADATA                                                        -->
    <!--=========================================================================-->
    <p:documentation>Convert OPF metadata to NCC metadata</p:documentation>
    <px:oebps-to-ncc-metadata name="metadata">
        <p:input port="source">
            <p:pipe port="result" step="opf"/>
        </p:input>
    </px:oebps-to-ncc-metadata>


    <!--=========================================================================-->
    <!-- CONVERT NCX TO NCC                                                      -->
    <!--=========================================================================-->
    <p:documentation>Generate the NCC from the NCX</p:documentation>
    <p:group name="ncc" px:message="Generating NCC document">
        <p:output port="fileset" primary="true">
            <p:pipe port="result" step="ncc.fileset"/>
        </p:output>
        <p:output port="in-memory">
            <p:pipe port="result" step="ncc.doc"/>
        </p:output>

        <px:fileset-load media-types="application/x-dtbncx+xml">
            <p:input port="fileset">
                <p:pipe step="fileset.in" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="upgrade" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:assert error-code="XXXX" test-count-min="1" test-count-max="1" name="ncx"
                   message="The input DTB must contain exactly one NCX file (media-type 'application/x-dtbncx+xml')"/>
        <p:sink/>
        <p:xslt name="ncx-to-ncc">
            <p:input port="source">
                <p:pipe port="result" step="ncx"/>
                <p:pipe port="result" step="metadata"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncx-to-ncc.xsl"/>
            </p:input>
            <p:with-param name="date" select="$date"/>
        </p:xslt>
        <px:set-base-uri name="ncc.doc">
            <p:with-option name="base-uri" select="concat($output-dir,'ncc.html')"/>
        </px:set-base-uri>

        <p:group name="ncc.fileset">
            <p:output port="result"/>
            <px:fileset-create>
                <p:with-option name="base" select="$output-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/xhtml+xml">
                <p:input port="entry">
                    <p:pipe step="ncc.doc" port="result"/>
                </p:input>
            </px:fileset-add-entry>
        </p:group>
    </p:group>
    <p:sink/>

    <!--=========================================================================-->
    <!-- CONVERT SMILS                                                           -->
    <!--=========================================================================-->

    <pxi:daisy3-to-daisy202-smils name="smils" px:message="Downgrading SMIL documents">
        <p:input port="source.fileset">
            <p:pipe step="fileset.in" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="upgrade" port="result.in-memory"/>
        </p:input>
        <p:with-option name="input-dir" select="base-uri(/*)">
            <p:pipe step="fileset.in" port="result"/>
        </p:with-option>
        <p:with-option name="output-dir" select="$output-dir"/>
    </pxi:daisy3-to-daisy202-smils>


    <!--=========================================================================-->
    <!-- CONSOLIDATE THE OUTPUT                                                  -->
    <!--=========================================================================-->

    <px:fileset-join name="result-fileset">
        <p:input port="source">
            <p:pipe step="ncc" port="fileset"/>
            <p:pipe step="smils" port="result.fileset"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>
    <p:identity name="result-docs">
        <p:input port="source">
            <p:pipe step="ncc" port="in-memory"/>
            <p:pipe step="smils" port="result.in-memory"/>
        </p:input>
    </p:identity>
    <p:sink/>

    <p:choose name="ensure-core-media">
        <p:when test="$ensure-core-media">
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="transcode" port="result.in-memory"/>
            </p:output>
            <px:daisy202-audio-transcode new-audio-file-type="audio/mpeg" name="transcode">
                <p:input port="source.fileset">
                    <p:pipe port="result" step="result-fileset"/>
                </p:input>
                <p:input port="source.in-memory">
                    <p:pipe step="result-docs" port="result"/>
                </p:input>
                <p:with-option name="temp-dir" select="$temp-dir"/>
            </px:daisy202-audio-transcode>
        </p:when>
        <p:otherwise>
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="result-docs" port="result"/>
            </p:output>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="result-fileset"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
